
package org.whispercomm.shout.content.merkle;

import java.nio.ByteBuffer;
import java.util.List;

import org.whispercomm.shout.Hash;

public interface MerkleNode {

	public MerkleNodeReference getRef();

	/**
	 * Computes the hash of this node.
	 * 
	 * @return the hash of this node
	 */
	public Hash getHash();

	/**
	 * Checks if all the data blocks in this (sub)-tree are available.
	 * 
	 * @return {@code true} if all data blocks are available, otherwise
	 *         {@code false}
	 */
	public boolean isComplete();

	/**
	 * Adds the hashes of any node known to be missing from this (sub)-tree to
	 * the provided list. Nodes referenced by missing nodes are obviously not
	 * included in the list.
	 * 
	 * @param list the list to which the hashes are added
	 */
	public void getMissing(List<MerkleNodeReference> list);

	public void depthFirstTraversal(Visitor visitor);

	/**
	 * Computes the total size of the data block hashed by this (sub)-tree.
	 * 
	 * @return the total size of the underlying data block in bytes
	 * @throws IncompleteTreeException if some leaves are missing
	 */
	public int size() throws IncompleteTreeException;

	/**
	 * Reconstructs the original data
	 * 
	 * @return the original data block
	 * @throws IncompleteTreeException if some leaves are missing
	 */
	public void getData(ByteBuffer buffer) throws IncompleteTreeException;

	public MerkleNodeReference[] getChildren();

	public byte[] serialize();

	public boolean serialize(ByteBuffer buffer);

	public interface Visitor {
		public void visit(MerkleNodeReference merkleNodeReference);
	}

}
