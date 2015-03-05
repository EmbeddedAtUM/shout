
package org.whispercomm.shout.id;

public class UserNotInitiatedException extends Exception {

	/**
	 * Generated serial version UUID
	 */
	private static final long serialVersionUID = -4998657903829600545L;

	public UserNotInitiatedException() {
		super("User information (i.e., name and key pair) is not initiated.");
	}
}
