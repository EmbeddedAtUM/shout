
package org.whispercomm.shout.network.shout;

public class InvalidShoutSignatureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4411698166855662609L;

	public InvalidShoutSignatureException() {
		super("Received shout did not have a valid signature, tossing entire chain");
	}
}
