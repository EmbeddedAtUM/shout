package org.whispercomm.shout.network;

public class ShoutChainTooLongException extends Exception {

	/**
	 * Generated serial version UUID
	 */
	private static final long serialVersionUID = -5021448222790446105L;

	public ShoutChainTooLongException() {
		super("Maximum allowable Shout chain length is three.");
	}
}
