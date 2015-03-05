
package org.whispercomm.shout.twitter;

/**
 * Thrown if the request interaction with Twitter failed because the screenname
 * was unknown.
 * 
 * @author David R. Bild
 */
public class UnknownScreennameException extends Exception {

	private static final long serialVersionUID = 4094897186497717795L;

	private String screenname;

	public UnknownScreennameException(String screenname) {
		this.screenname = screenname;
	}

	public UnknownScreennameException(String screenname, String detailMessage) {
		super(detailMessage);
		this.screenname = screenname;
	}

	public UnknownScreennameException(String screenname, Throwable throwable) {
		super(throwable);
		this.screenname = screenname;
	}

	public UnknownScreennameException(String screenname, String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		this.screenname = screenname;
	}

	public String getScreenname() {
		return screenname;
	}

}
