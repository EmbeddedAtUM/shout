
package org.whispercomm.shout.serialization;

/**
 * Thrown when an invalid version is used to read or write an
 * {@link ShoutPacket}.
 * 
 * @author David Adrian
 */
public class NoSuchVersionException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -9125715692161309398L;

	public NoSuchVersionException() {
		super();
	}

	public NoSuchVersionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public NoSuchVersionException(String detailMessage) {
		super(detailMessage);
	}

	public NoSuchVersionException(Throwable throwable) {
		super(throwable);
	}

}
