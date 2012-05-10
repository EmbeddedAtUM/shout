package org.whispercomm.shout.network;

public class AuthenticityFailureException extends Exception {
	public AuthenticityFailureException(){
		super("The network received Shout message fails authenticity check.");
	}
}
