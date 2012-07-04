
package org.whispercomm.shout.serialization;

/**
 * An exception indicating a Shout chain was too long to be serialized into a
 * single packet.
 * 
 * @author Yue Liu 
 */
public class ShoutChainTooLongException extends Exception {

	/**
	 * Generated serial version UUID
	 */
	private static final long serialVersionUID = -5021448222790446105L;

	public ShoutChainTooLongException() {
		super("Maximum allowable Shout chain length is three.");
	}
}
