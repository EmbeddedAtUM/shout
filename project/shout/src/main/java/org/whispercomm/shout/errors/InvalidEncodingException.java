
package org.whispercomm.shout.errors;

/**
 * Thrown when an invalid valid encoding is used to create an object.
 * 
 * @author David R. Bild
 */
public class InvalidEncodingException extends IllegalArgumentException {

	private static final long serialVersionUID = -1448573675396303643L;

	public InvalidEncodingException() {
		super();
	}

	public InvalidEncodingException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public InvalidEncodingException(String detailMessage) {
		super(detailMessage);
	}

	public InvalidEncodingException(Throwable throwable) {
		super(throwable);
	}

}
