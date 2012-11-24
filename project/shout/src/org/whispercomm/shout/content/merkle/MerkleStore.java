
package org.whispercomm.shout.content.merkle;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.content.storage.ObjectStorage;
import org.whispercomm.shout.errors.InvalidFormatException;
import org.whispercomm.shout.errors.NotFoundException;
import org.whispercomm.shout.network.UnsupportedVersionException;

public class MerkleStore {

	private final ObjectStorage storage;

	public MerkleStore(ObjectStorage storage) {
		this.storage = storage;
	}

	public MerkleNodeReference putObject(byte[] data) throws IOException {
		return putObject(data, 0, data.length);
	}

	public MerkleNodeReference putObject(byte[] data, int offset, int len) throws IOException {
		MerkleNodeReference root = MerkleBuilder.build(data, offset, len).getRef();
		putTree(root);
		return root;
	}

	public void putTree(MerkleNodeReference root) throws IOException {
		if (root.isAvailable()) {
			putNode(root);
			for (MerkleNodeReference child : root.get().getChildren()) {
				putTree(child);
			}
		}
	}

	public void putNode(MerkleNodeReference ref) throws IOException {
		if (ref.isAvailable())
			putNode(ref.get());
	}

	public void putNode(MerkleNode node) throws IOException {
		if (!storage.exists(node.getHash()))
			storage.store(node.serialize());
	}

	public byte[] getObject(MerkleNodeReference ref) throws NotFoundException, IOException {
		growTree(ref);
		if (!ref.isAvailable())
			throw new NotFoundException("The root node is missing.");

		MerkleNode root = ref.get();
		try {
			ByteBuffer data = ByteBuffer.allocate(root.size());
			root.getData(data);
			return data.array();
		} catch (IncompleteTreeException e) {
			throw new NotFoundException("Some data blocks are missing.", e);
		}
	}

	public byte[] getObject(Hash hash) throws NotFoundException, IOException {
		return getObject(new MerkleNodeReference(hash));
	}

	public void growTree(MerkleNodeReference ref) throws
			IOException {
		if (setReference(ref)) {
			for (MerkleNodeReference child : ref.get().getChildren()) {
				growTree(child);
			}
		}
	}

	public boolean setReference(MerkleNodeReference ref) throws IOException {
		if (ref.isAvailable())
			return true;

		try {
			byte[] encoded = storage.retrieve(ref.getHash());
			ref.set(MerkleSerializer.deserialize(encoded));
			return true;
		} catch (UnsupportedVersionException e) {
			return false;
		} catch (InvalidFormatException e) {
			return false;
		} catch (NotFoundException e) {
			return false;
		}
	}
}
