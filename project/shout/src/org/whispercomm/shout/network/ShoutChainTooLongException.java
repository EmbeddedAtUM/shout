package org.whispercomm.shout.network;

public class ShoutChainTooLongException extends Exception {

	public ShoutChainTooLongException(){
		super("The length of the Shout chain is too long!");
	}
}
