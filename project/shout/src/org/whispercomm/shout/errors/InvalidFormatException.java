
package org.whispercomm.shout.errors;

public class InvalidFormatException extends Exception {

	private static final long serialVersionUID = 7599781711133219880L;

	public InvalidFormatException() {
		super();
	}

	public InvalidFormatException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public InvalidFormatException(String detailMessage) {
		super(detailMessage);
	}

	public InvalidFormatException(Throwable throwable) {
		super(throwable);
	}

}
