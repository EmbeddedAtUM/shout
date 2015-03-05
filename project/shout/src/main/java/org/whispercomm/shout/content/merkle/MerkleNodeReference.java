
package org.whispercomm.shout.content.merkle;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.SimpleHashReference;

public class MerkleNodeReference extends SimpleHashReference<MerkleNode> {

	public MerkleNodeReference(Hash hash) {
		super(hash);
	}

	public MerkleNodeReference(MerkleNode node) {
		super(node.getHash(), node);
	}

	@Override
	public void set(MerkleNode node) {
		if (!getHash().equals(node.getHash()))
			throw new IllegalArgumentException(
					"MerkleNode does not match the hash for this reference");
		super.set(node);
	}

}
