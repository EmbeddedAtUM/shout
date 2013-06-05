
package org.whispercomm.shout.network.content;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.whispercomm.manes.client.maclib.ManesFrameTooLargeException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.content.descriptor.ContentDescriptor;
import org.whispercomm.shout.content.descriptor.ContentDescriptorSerializer;
import org.whispercomm.shout.content.descriptor.ContentDescriptorStore;
import org.whispercomm.shout.content.merkle.MerkleNode;
import org.whispercomm.shout.content.merkle.MerkleNodeReference;
import org.whispercomm.shout.content.merkle.MerkleSerializer;
import org.whispercomm.shout.content.merkle.MerkleStore;
import org.whispercomm.shout.content.request.ContentRequest;
import org.whispercomm.shout.content.request.ContentRequestSerializer;
import org.whispercomm.shout.content.storage.ObjectStorage;
import org.whispercomm.shout.errors.InvalidFormatException;
import org.whispercomm.shout.errors.NotFoundException;
import org.whispercomm.shout.network.ObjectProtocol;
import org.whispercomm.shout.network.ObjectType;
import org.whispercomm.shout.network.PacketProtocol;
import org.whispercomm.shout.network.UnsupportedVersionException;

import android.util.Log;

public class ContentProtocol implements ObjectProtocol {
	private static final String TAG = ContentProtocol.class.getSimpleName();

	private PacketProtocol packetProtocol;
	private ObjectStorage objectStorage;
	private ContentDescriptorStore descriptorStore;
	private MerkleStore merkleStore;

	private ContentRequestHandler contentRequestHandler;

	public ContentProtocol(PacketProtocol packetProtocol, ContentManager contentManager) {
		this.packetProtocol = packetProtocol;
		this.objectStorage = contentManager.getObjectStorage();
		this.descriptorStore = contentManager.getDescriptorStore();
		this.merkleStore = contentManager.getMerkleStore();
	}

	public void setContentRequestHandler(ContentRequestHandler handler) {
		this.contentRequestHandler = handler;
	}

	public void send(Hash hash) throws IOException, NotFoundException,
			ManesFrameTooLargeException, ManesNotRegisteredException {
		if (!objectStorage.exists(hash))
			throw new NotFoundException();

		byte[] data = objectStorage.retrieve(hash);
		int id = 0xFF & data[0];
		try {
			if (id == ObjectType.ContentDescriptor.getId())
				send(ContentDescriptorSerializer.deserialize(data));
			else if (id == ObjectType.MerkleNode.getId())
				send(MerkleSerializer.deserialize(data));
			else
				Log.w(TAG, "Dropping send request for unknown object type: " + data[0]);
		} catch (InvalidFormatException e) {
			// TODO: Figure out how to handle this. Delete from disk?
			Log.w(TAG, "Unable to decode content block from disk", e);
		} catch (UnsupportedVersionException e) {
			// TODO: Figure out how to handle this. Delete from disk?
			Log.w(TAG, "Unable to decode content block from disk", e);
		}
	}

	@Override
	public void receive(ObjectType type, ByteBuffer data) {
		switch (type) {
			case ContentDescriptor:
				receiveContentDescriptor(data);
				break;
			case MerkleNode:
				receiveMerkleNode(data);
				break;
			case ContentRequest:
				receiveContentRequest(data);
				break;
			default:
				break;
		}
	}

	private void receiveContentDescriptor(ByteBuffer data) {
		ContentDescriptor descriptor = null;
		try {
			descriptor = ContentDescriptorSerializer.deserialize(data);
		} catch (UnsupportedVersionException e) {
			Log.v(TAG, "Dropping content descriptor with unsupported version", e);
			return;
		} catch (InvalidFormatException e) {
			Log.v(TAG, "Dropping content descriptor with invalid format", e);
			return;
		}

		contentRequestHandler.receiveContentDescriptor(descriptor);

		try {
			descriptorStore.store(descriptor);
		} catch (IOException e) {
			Log.w(TAG, "Dropping content descriptor after failture to store it", e);
		}
	}

	private void receiveMerkleNode(ByteBuffer data) {
		MerkleNode node = null;
		try {
			node = MerkleSerializer.deserialize(data);
		} catch (UnsupportedVersionException e) {
			Log.v(TAG, "Dropping merkle node with unsupported version", e);
			return;
		} catch (InvalidFormatException e) {
			Log.v(TAG, "Dropping merkle node with invalid format", e);
			return;
		}

		contentRequestHandler.receiveMerkleNode(node);

		try {
			merkleStore.putNode(node);
		} catch (IOException e) {
			Log.w(TAG, "Dropping merkle node after failure to store it", e);
		}
	}

	private void receiveContentRequest(ByteBuffer data) {
		ContentRequest request = null;
		try {
			request = ContentRequestSerializer.deserialize(data);
		} catch (UnsupportedVersionException e) {
			Log.v(TAG, "Dropping content request with unsupported version", e);
			return;
		} catch (InvalidFormatException e) {
			Log.v(TAG, "Dropping content request with invalid format", e);
		}

		contentRequestHandler.receiveRequest(request);
	}

	public void send(ContentDescriptor descriptor) throws IOException, ManesFrameTooLargeException,
			ManesNotRegisteredException {
		// Add the descriptor
		ByteBuffer buffer = PacketProtocol.createPacket();
		while (!ContentDescriptorSerializer.serialize(buffer, descriptor)) {
			packetProtocol.send(buffer);
			buffer = PacketProtocol.createPacket();
		}

		// Send the merkle nodes
		send(new MerkleNodeReference(descriptor.getObjectRoot()), buffer);
	}

	public void send(MerkleNode node) throws IOException, ManesFrameTooLargeException,
			ManesNotRegisteredException {
		send(node.getRef(), PacketProtocol.createPacket());
	}

	public void send(MerkleNodeReference nodeRef, ByteBuffer buffer) throws IOException,
			ManesFrameTooLargeException, ManesNotRegisteredException {
		merkleStore.growTree(nodeRef);

		// Serialize the merkle nodes into buffers
		MerkleNodePacketBuilder builder = new MerkleNodePacketBuilder(buffer);
		if (nodeRef.isAvailable())
			nodeRef.get().depthFirstTraversal(builder);

		// Send the buffers
		for (ByteBuffer buf : builder.getBuffers()) {
			packetProtocol.send(buf);
		}
	}

	private static class MerkleNodePacketBuilder implements MerkleNode.Visitor {

		private List<ByteBuffer> buffers;
		private ByteBuffer current;

		public MerkleNodePacketBuilder(ByteBuffer buffer) {
			this.current = buffer;
			this.buffers = new ArrayList<ByteBuffer>(10);
			this.buffers.add(current);
		}

		@Override
		public void visit(MerkleNodeReference ref) {
			if (!ref.isAvailable())
				return;

			MerkleNode node = ref.get();
			if (!node.serialize(current)) {
				createBuffer();
				node.serialize(current);
			}
		}

		private void createBuffer() {
			current = PacketProtocol.createPacket();
			buffers.add(current);
		}

		public List<ByteBuffer> getBuffers() {
			return buffers;
		}

	}
}
