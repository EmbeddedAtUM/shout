
package org.whispercomm.shout.content.merkle;

import java.util.LinkedList;
import java.util.Queue;

public class MerkleBuilder {

	public static final int MAX_DATA_LEN = 100 * 1024; // 100 kB

	/**
	 * Build the Merkle tree for the provided data;
	 * 
	 * @param data
	 * @param offset
	 * @param len
	 * @return the root of the Merkle tree
	 */
	public static MerkleNode build(byte[] data, int offset, int len) {
		if (len > MAX_DATA_LEN)
			throw new IllegalArgumentException("Cannot build tree for data larger than 100 kB.");

		// Number of leaves
		int cnt = divideRoundUp(len, DataBlockNode.MAX_LEN);

		// Replace with ArrayDeque (from Java 1.6), if we ever update to API
		// Level 9 or beyond. LinkedList has bad cached behavior and object
		// creation overhead.
		//
		// Dequeue<MerkleNode> nodes = new ArrayDeque(cnt);
		Queue<MerkleNode> nodes = new LinkedList<MerkleNode>();

		// Create leaf nodes
		int idx = offset;
		int rem = len;
		while (rem > DataBlockNode.MAX_LEN) {
			nodes.add(new DataBlockNode(data, idx, DataBlockNode.MAX_LEN));
			idx += DataBlockNode.MAX_LEN;
			rem -= DataBlockNode.MAX_LEN;
		}
		nodes.add(new DataBlockNode(data, idx, rem));

		// Build the complete binary tree, with nodes packed to the left
		int e = cnt; // number of enqueued nodes
		int r = cnt; // number of leaf nodes not yet processed
		// process all leaf nodes that sit at lowest level
		while (!pow2(e)) {
			nodes.add(new InnerNode(nodes.remove(), nodes.remove()));
			e -= 1;
			r -= 2;
		}
		// move all leaf nodes that sit at 2nd-lowest level to back of queue
		for (; r > 0; --r) {
			nodes.add(nodes.remove());
		}
		// join all nodes, breadth-first
		while (e > 1) {
			nodes.add(new InnerNode(nodes.remove(), nodes.remove()));
			e -= 1;
		}

		return nodes.remove();
	}

	private static final boolean pow2(int val) {
		return 0 == (val & (val - 1));
	}

	private static final int divideRoundUp(int dividend, int divisor) {
		return (dividend + divisor - 1) / divisor;
	}

}
