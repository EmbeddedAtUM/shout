
package org.whispercomm.shout.test.util;

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

	public static void testEqualUserFields(User lhs, User rhs) {
		if (lhs == null) {
			assertNull(rhs);
		}
		if (rhs == null) {
			assertNull(lhs);
		}
		assertEquals(lhs.getUsername(), rhs.getUsername());
		assertEquals(lhs.getPublicKey(), rhs.getPublicKey());
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
		assertEquals(lhs.getTimestamp().getMillis(), rhs.getTimestamp().getMillis());
		assertEquals(lhs.getSender().getUsername(), rhs.getSender().getUsername());
		assertEquals(lhs.getSender().getPublicKey(), rhs.getSender().getPublicKey());
		assertEquals(lhs.getSender().getAvatar(), rhs.getSender().getAvatar());
		assertEquals(lhs.getSignature(), rhs.getSignature());
		assertEquals(lhs.getHash(), rhs.getHash());
		testEqualShoutFields(lhs.getParent(), rhs.getParent());
	}

	public static void testEqualShoutFieldsNoParent(Shout lhs, Shout rhs) {
		if (lhs == null) {
			assertNull(rhs);
			return;
		}
		if (rhs == null) {
			assertNull(lhs);
			return;
		}
		assertEquals(lhs.getMessage(), rhs.getMessage());
		assertEquals(lhs.getTimestamp().getMillis(), rhs.getTimestamp().getMillis());
		assertEquals(lhs.getSender().getUsername(), rhs.getSender().getUsername());
		assertEquals(lhs.getSender().getPublicKey(), rhs.getSender().getPublicKey());
		assertEquals(lhs.getSignature(), rhs.getSignature());
		assertEquals(lhs.getHash(), rhs.getHash());
	}

}
