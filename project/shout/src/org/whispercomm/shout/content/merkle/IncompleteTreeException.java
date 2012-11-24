
package org.whispercomm.shout.content.merkle;

/**
 * Throw if an operation that requires a complete tree is called on an
 * incomplete tree.
 * 
 * @author David R. Bild
 */
public class IncompleteTreeException extends Exception {

	private static final long serialVersionUID = 6390729805619145525L;

	public IncompleteTreeException() {
		super();
	}

	public IncompleteTreeException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public IncompleteTreeException(String detailMessage) {
		super(detailMessage);
	}

	public IncompleteTreeException(Throwable throwable) {
		super(throwable);
	}

}
