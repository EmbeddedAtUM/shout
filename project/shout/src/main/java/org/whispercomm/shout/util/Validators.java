
package org.whispercomm.shout.util;

import java.io.UnsupportedEncodingException;

import org.whispercomm.shout.serialization.SerializeUtility;

public class Validators {

	public static boolean validateUsername(String username) {
		if (username.length() == 0) {
			return false;
		}

		byte[] bytes = null;
		try {
			bytes = username.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false; // TODO
		}
		if (bytes.length > SerializeUtility.USERNAME_SIZE_MAX) {
			return false;
		}

		char c;
		for (int i = 0; i < username.length(); i++) {
			c = username.charAt(i);
			if (c == '\n' || c == '\t') {
				return false;
			}
		}
		return true;
	}

	public static boolean validateShoutMessage(String message) {
		byte[] bytes;
		try {
			bytes = message.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false; // TODO
		}
		if (bytes.length > SerializeUtility.MESSAGE_SIZE_MAX) {
			return false;
		}
		return true;
	}

	public static String removeTrailingSpaces(String string) {
		/*
		 * Replace a sequence white space characters (\s+) at the end of a line
		 * (+) with nothing
		 */
		return string.replaceAll("\\s+$", "");
	}
}
