
package org.whispercomm.shout.serialization;

public class ShoutPacketException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 4900598861077787951L;

	public ShoutPacketException() {
		super("Unable to parse packet");
	}

	public ShoutPacketException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ShoutPacketException(String detailMessage) {
		super(detailMessage);
	}

	public ShoutPacketException(Throwable throwable) {
		super(throwable);
	}

}
