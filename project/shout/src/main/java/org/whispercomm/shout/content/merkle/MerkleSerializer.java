
package org.whispercomm.shout.content.merkle;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.errors.InvalidFormatException;
import org.whispercomm.shout.network.ObjectType;
import org.whispercomm.shout.network.UnsupportedVersionException;

public final class MerkleSerializer {

	public static final ObjectType TYPE = ObjectType.MerkleNode;

	public static byte[] serialize(InnerNode innerNode) {
		ByteBuffer data = ByteBuffer.allocate(1 + 2 + 1 + 32 + 32);
		serialize(data, innerNode);
		return data.array();
	}

	/**
	 * Serializes the node into the specified byte buffer, if the byte buffer
	 * has room. If it does not fit, no modification is made to the buffer.
	 * 
	 * @param buffer the buffer in which to serialize the node
	 * @param innerNode the node to serialize
	 * @return {@code true} if the node was serialized or {@code false} if it
	 *         would not fit.
	 */
	public static boolean serialize(ByteBuffer buffer, InnerNode innerNode) {
		if (buffer.remaining() < 1 + 2 + 1 + 32 + 32)
			return false;
		buffer.put(TYPE.getIdAsByte());
		buffer.putShort((short) 65);
		buffer.put(InnerNode.TYPE_BYTE);
		buffer.put(innerNode.getChildren()[0].getHash().toByteArray());
		buffer.put(innerNode.getChildren()[1].getHash().toByteArray());
		return true;
	}

	public static byte[] serialize(DataBlockNode dataNode) {
		ByteBuffer data = ByteBuffer.allocate(1 + 2 + 1 + 2 + dataNode.size());
		serialize(data, dataNode);
		return data.array();
	}

	/**
	 * Serializes the node into the specified byte buffer, if the byte buffer
	 * has room. If it does not fit, no modification is made to the buffer.
	 * 
	 * @param buffer the buffer in which to serialize the node
	 * @param innerNode the node to serialize
	 * @return {@code true} if the node was serialized or {@code false} if it
	 *         would not fit.
	 */
	public static boolean serialize(ByteBuffer buffer, DataBlockNode dataNode) {
		if (buffer.remaining() < 1 + 2 + 1 + 2 + dataNode.size())
			return false;
		buffer.put(TYPE.getIdAsByte());
		buffer.putShort((short) (1 + 2 + dataNode.size()));
		buffer.put(DataBlockNode.TYPE_BYTE);
		buffer.putShort((short) dataNode.size());
		dataNode.getData(buffer);
		return true;
	}

	/**
	 * Constructs a {@code MerkleNode} from the serialized form in the provider
	 * {@code ByteBuffer}. The buffer's position should be set to the start of
	 * serialized object and the limit should be set just past the end.
	 * 
	 * @param buffer
	 * @return
	 * @throws UnsupportedVersionException
	 */
	public static MerkleNode deserialize(ByteBuffer buffer) throws UnsupportedVersionException,
			InvalidFormatException {
		try {
			int typeId = 0xFF & buffer.get();
			if (typeId != TYPE.getId())
				throw new InvalidFormatException(String.format(
						"Incorrect object type for merkle node.  Got %d. Expected %d.", typeId,
						TYPE.getId()));

			int contentLength = buffer.getShort();

			byte type = buffer.get();
			switch (type) {
				case InnerNode.TYPE_BYTE:
					if (contentLength != 65)
						throw new InvalidFormatException(String.format(
								"Invalid content length for inner node. Got %d. Expected %d.",
								contentLength, 65));

					byte[] left = new byte[32];
					byte[] right = new byte[32];
					buffer.get(left);
					buffer.get(right);
					return new InnerNode(new Hash(left), new Hash(right));
				case DataBlockNode.TYPE_BYTE:
					int len = buffer.getShort();

					if (len > DataBlockNode.MAX_LEN)
						throw new InvalidFormatException("Invalid data block length.");
					if (contentLength != 1 + 2 + len)
						throw new InvalidFormatException(String.format(
								"Invalid content length for data node. Got %d. Expected %d.",
								contentLength, 1 + 2 + len));

					byte[] data = new byte[len];
					buffer.get(data);
					return new DataBlockNode(data);
				default:
					throw new UnsupportedVersionException(String.format(
							"Unrecognized merkle node version: %02X", type));
			}
		} catch (BufferUnderflowException e) {
			throw new InvalidFormatException("Data too short.");
		}

	}

	public static MerkleNode deserialize(byte[] data, int offset)
			throws UnsupportedVersionException, InvalidFormatException {
		return deserialize(ByteBuffer.wrap(data, offset, data.length - offset));
	}

	public static MerkleNode deserialize(byte[] data) throws UnsupportedVersionException,
			InvalidFormatException {
		return deserialize(data, 0);
	}

	private MerkleSerializer() {
	}
}
