package org.whispercomm.shout.id;

public class UserNameInvalidException extends Exception {

	/**
	 * Generated serial version UUID
	 */
	private static final long serialVersionUID = -3082937322762809141L;

	public UserNameInvalidException(){
		super("Invalid user name, e.g., null or too long.");
	}
}
