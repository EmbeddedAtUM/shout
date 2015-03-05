
package org.whispercomm.shout.errors;

public class NotFoundException extends Exception {

	private static final long serialVersionUID = 1607753425037191592L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public NotFoundException(String detailMessage) {
		super(detailMessage);
	}

	public NotFoundException(Throwable throwable) {
		super(throwable);
	}

}
