
package org.whispercomm.shout.serialization;

/**
 * Exception used to indicate a Shout with a bad version came over the network.
 * 
 * @author David Adrian
 */
public class BadShoutVersionException extends Exception {

	/**
	 * Generated serial version UUID
	 */
	private static final long serialVersionUID = -65027072768408572L;

	public BadShoutVersionException() {
		super();
	}

	public BadShoutVersionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public BadShoutVersionException(String detailMessage) {
		super(detailMessage);
	}

	public BadShoutVersionException(Throwable throwable) {
		super(throwable);
	}

}
