
package org.whispercomm.shout.network;

public class UnsupportedVersionException extends Exception {
	private static final long serialVersionUID = -3026901949270430380L;

	public UnsupportedVersionException() {
		super();
	}

	public UnsupportedVersionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public UnsupportedVersionException(String detailMessage) {
		super(detailMessage);
	}

	public UnsupportedVersionException(Throwable throwable) {
		super(throwable);
	}

}
