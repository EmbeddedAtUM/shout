
package org.whispercomm.shout.test.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.Tag;
import org.whispercomm.shout.User;

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

	public static void testEqualShoutFields(Shout lhs, Shout rhs) {
		if (lhs == null) {
			assertNull(rhs);
			return;
		}
		if (rhs == null) {
			assertNull(lhs);
			return;
		}
		assertEquals(lhs.getMessage(), rhs.getMessage());
		assertEquals(lhs.getTimestamp(), rhs.getTimestamp());
		assertEquals(lhs.getSender().getUsername(), rhs.getSender().getUsername());
		assertEquals(lhs.getSender().getPublicKey(), rhs.getSender().getPublicKey());
		assertArrayEquals(lhs.getSignature(), rhs.getSignature());
		assertArrayEquals(lhs.getHash(), rhs.getHash());
		testEqualShoutFields(lhs.getParent(), rhs.getParent());
	}

}
