package org.whispercomm.shout;

public class UserNotInitiatedException extends Exception {

	public UserNotInitiatedException(){
		super("User information (i.e., name and key pair) is not initiated.");
	}
}
