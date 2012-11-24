
package org.whispercomm.shout.content.merkle;

import java.nio.ByteBuffer;
import java.util.List;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.whispercomm.shout.Hash;

public class InnerNode extends AbstractMerkleNode {

	static final byte TYPE_BYTE = 0x00;

	private final MerkleNodeReference left;

	private final MerkleNodeReference right;

	public InnerNode(MerkleNodeReference left, MerkleNodeReference right) {
		super(computeHash(left, right));
		this.left = left;
		this.right = right;
	}

	public InnerNode(Hash left, Hash right) {
		this(new MerkleNodeReference(left), new MerkleNodeReference(right));
	}

	public InnerNode(MerkleNode left, MerkleNode right) {
		this(new MerkleNodeReference(left), new MerkleNodeReference(right));
	}

	private static Hash computeHash(MerkleNodeReference left, MerkleNodeReference right) {
		SHA256Digest digest = new SHA256Digest();
		digest.update(MerkleSerializer.TYPE.getIdAsByte());
		digest.update((byte) 0);
		digest.update((byte) 65);
		digest.update(TYPE_BYTE);
		digest.update(left.getHash().toByteArray(), 0, Hash.LENGTH);
		digest.update(right.getHash().toByteArray(), 0, Hash.LENGTH);

		byte[] hash = new byte[Hash.LENGTH];
		digest.doFinal(hash, 0);

		return new Hash(hash);
	}

	private void ensureChildrenAvailable() throws IncompleteTreeException {
		if (!left.isAvailable() || !right.isAvailable())
			throw new IncompleteTreeException();
	}

	@Override
	public boolean isComplete() {
		if (!left.isAvailable() || !right.isAvailable())
			return false;

		return left.get().isComplete() && right.get().isComplete();
	}

	@Override
	public int size() throws IncompleteTreeException {
		ensureChildrenAvailable();

		return left.get().size() + right.get().size();
	}

	@Override
	public void getData(ByteBuffer buffer) throws IncompleteTreeException {
		ensureChildrenAvailable();

		left.get().getData(buffer);
		right.get().getData(buffer);
	}

	@Override
	public void getMissing(List<MerkleNodeReference> list) {
		if (left.isAvailable())
			left.get().getMissing(list);
		else
			list.add(left);

		if (right.isAvailable())
			right.get().getMissing(list);
		else
			list.add(right);
	}

	@Override
	public void depthFirstTraversal(Visitor visitor) {
		visitor.visit(this.getRef());
		for (MerkleNodeReference child : getChildren()) {
			if (child.isAvailable())
				child.get().depthFirstTraversal(visitor);
			else
				visitor.visit(child);
		}
	}

	@Override
	public byte[] serialize() {
		return MerkleSerializer.serialize(this);
	}

	@Override
	public boolean serialize(ByteBuffer buffer) {
		return MerkleSerializer.serialize(buffer, this);
	}

	@Override
	public MerkleNodeReference[] getChildren() {
		return new MerkleNodeReference[] {
				left, right
		};
	}

}
