
package org.whispercomm.shout.content.merkle;

import java.nio.ByteBuffer;
import java.util.List;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.whispercomm.shout.Hash;

public class DataBlockNode extends AbstractMerkleNode {

	private static final MerkleNodeReference[] CHILDREN = new MerkleNodeReference[0];

	static final byte TYPE_BYTE = 0x10;

	/**
	 * Maximum size of the data block in bytes.
	 */
	static final short MAX_LEN = 1450;

	private final byte[] data;

	private final int offset;

	private final int count;

	public DataBlockNode(byte[] data, int offset, int count) {
		super(computeHash(data, offset, count));

		if (count > MAX_LEN)
			throw new IllegalArgumentException(String.format(
					"Data block length cannot exceed %d. Got %d.", MAX_LEN, data.length));

		this.data = data;
		this.offset = offset;
		this.count = count;
	}

	public DataBlockNode(byte[] data) {
		this(data, 0, data.length);
	}

	private static Hash computeHash(byte[] data, int offset, int count) {
		SHA256Digest digest = new SHA256Digest();
		digest.update(MerkleSerializer.TYPE.getIdAsByte());
		digest.update(ByteBuffer.allocate(2).putShort((short) (1 + 2 + count)).array(), 0, 2);
		digest.update(TYPE_BYTE);
		digest.update(ByteBuffer.allocate(2).putShort((short) count).array(), 0, 2);
		digest.update(data, offset, count);

		byte[] hash = new byte[32];
		digest.doFinal(hash, 0);

		return new Hash(hash);
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public void getMissing(List<MerkleNodeReference> list) {
		return;
	}

	@Override
	public void depthFirstTraversal(Visitor visitor) {
		visitor.visit(this.getRef());
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public void getData(ByteBuffer buffer) {
		buffer.put(data, offset, count);
	}

	@Override
	public MerkleNodeReference[] getChildren() {
		return CHILDREN;
	}

	@Override
	public byte[] serialize() {
		return MerkleSerializer.serialize(this);
	}

	@Override
	public boolean serialize(ByteBuffer buffer) {
		return MerkleSerializer.serialize(buffer, this);
	}

}
