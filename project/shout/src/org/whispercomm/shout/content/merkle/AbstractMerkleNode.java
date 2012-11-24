
package org.whispercomm.shout.content.merkle;

import org.whispercomm.shout.Hash;

public abstract class AbstractMerkleNode implements MerkleNode {

	private final MerkleNodeReference ref;

	private final Hash hash;

	public AbstractMerkleNode(Hash hash) {
		this.hash = hash;
		this.ref = new MerkleNodeReference(this);
	}

	@Override
	public Hash getHash() {
		return this.hash;
	}

	@Override
	public MerkleNodeReference getRef() {
		return this.ref;
	}

}
