package org.whispercomm.shout.network;

public class AuthenticityFailureException extends Exception {
	/**
	 * Generated serial version UUID
	 */
	private static final long serialVersionUID = 4836360163269665079L;

	public AuthenticityFailureException() {
		super("The received Shout failed the authenticity check.");
	}
}
