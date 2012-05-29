
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.Tag;
import org.whispercomm.shout.User;
import org.whispercomm.shout.util.Arrays;

public class TestUtility {

	public static boolean testEqualTagFields(Tag lhs, Tag rhs) {
		if (lhs == null || rhs == null)
			return false;
		return (lhs.getName().equals(rhs.getName()));
	}

	public static boolean testEqualUserFields(User lhs, User rhs) {
		boolean result = false;
		if (lhs == null || rhs == null)
			return false;
		result = (lhs.getUsername().equals(rhs.getUsername()) && lhs.getPublicKey().equals(
				rhs.getPublicKey()));
		return result;
	}

	public static boolean testEqualShoutFields(Shout lhs, Shout rhs) {
		if (lhs == rhs) {
			return true;
		} else if (lhs == null || rhs == null) {
			return false;
		}
		boolean result = false;
		if (Arrays.equals(lhs.getHash(), rhs.getHash())) {
			if (Arrays.equals(lhs.getSignature(), rhs.getSignature())) {
				if (lhs.getTimestamp().equals(rhs.getTimestamp())) {
					result = testEqualUserFields(lhs.getSender(), rhs.getSender());
				}
			}
		}
		if (result) {
			if (lhs.getMessage() != null && rhs.getMessage() != null) {
				result = lhs.getMessage().equals(rhs.getMessage());
			} else if (lhs.getMessage() != rhs.getMessage()) {
				result = false;
			}
		}
		if (result) {
			if (lhs.getParent() != null || rhs.getParent() != null) {
				result = testEqualShoutFields(lhs.getParent(), rhs.getParent());
			}
		}
		return result;
	}

}
