package org.whispercomm.shout.id;

public class UserNameInvalidException extends Exception {

	public UserNameInvalidException(){
		super("Invalid user name, e.g., null or too long.");
	}
}
