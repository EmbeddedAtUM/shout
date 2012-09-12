
package org.whispercomm.shout.util;

import org.whispercomm.shout.serialization.SerializeUtility;

public class Validators {

	public static boolean validateUsername(String username) {
		byte[] bytes = username.getBytes();
		if (bytes.length > SerializeUtility.USERNAME_SIZE_MAX) {
			return false;
		}
		char c;
		for (int i = 0; i < username.length(); i++) {
			c = username.charAt(i);
			if (c == '\n') {
				return false;
			}
		}
		return true;
	}

	public static boolean validateShoutMessage(String message) {
		byte[] bytes = message.getBytes();
		if (bytes.length > SerializeUtility.MESSAGE_SIZE_MAX) {
			return false;
		}
		return true;
	}

	public static String removeTrailingSpaces(String string) {
		/*
		 * TODO Implement
		 */
		return string;
	}
}
