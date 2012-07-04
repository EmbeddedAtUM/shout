package org.whispercomm.shout.util;

import org.whispercomm.shout.serialization.SerializeUtility;

public class Validators {

	public static boolean validateUsername(String username) {
		byte[] bytes = username.getBytes();
		if (bytes.length > SerializeUtility.MAX_USERNAME_SIZE) {
			return false;
		}
		return true;
	}
	
	public static boolean validateShoutMessage(String message) {
		byte[] bytes = message.getBytes();
		if (bytes.length > SerializeUtility.MAX_MESSAGE_SIZE) {
			return false;
		}
		return true;
	}
	
	public static String removeTrailingSpaces() {
		/*
		 * TODO 1) Determine if this is needed
		 * 		2) Write this
		 */
		return null;
	}
}
