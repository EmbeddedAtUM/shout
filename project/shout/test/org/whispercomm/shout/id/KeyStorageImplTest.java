
package org.whispercomm.shout.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.test.ShoutTestRunner;

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class KeyStorageImplTest {

	private static final String USER_INIT_FAIL = "User was initiated";

	private KeyStorage keyStore;
	private Context context;
	private String username;

	@Before
	public void setUp() {
		this.context = new Activity();
		this.keyStore = new KeyStorageSharedPrefs(context);
		this.username = "Day9";
	}

	@After
	public void takeDown() {
		this.keyStore = null;
		this.username = null;
		this.context = null;
	}

	@Test
	public void testInitEmptyOnCreate() {
		assertTrue(keyStore.isEmpty());
	}

	@Test
	public void testCannotReadUnsetUser() {
		try {
			keyStore.readKeyPair();
		} catch (UserNotInitiatedException e) {
			try {
				keyStore.readUsername();
			} catch (UserNotInitiatedException e1) {
				return;
			}
			fail("Did not throw exception on read unset username");
		}
		fail("Did not throw exception on read unset KeyPair");
	}

	@Test
	public void testReadWriteKeyPair() {
		try {
			KeyPair keyPair = SignatureUtility.generateKeyPair();
			boolean status = keyStore.writeMe(username, keyPair);
			assertTrue(status);
			assertFalse(keyStore.isEmpty());
			KeyPair fromStore;
			fromStore = keyStore.readKeyPair();
			assertEquals(keyPair.getPublic(), fromStore.getPublic());
			assertEquals(keyPair.getPrivate(), fromStore.getPrivate());
		} catch (UserNotInitiatedException e) {
			e.printStackTrace();
			fail(USER_INIT_FAIL);
		}
	}

	@Test
	public void testReadWriteUsername() {
		KeyPair keyPair = SignatureUtility.generateKeyPair();
		boolean status = keyStore.writeMe(username, keyPair);
		assertTrue(status);
		assertFalse(keyStore.isEmpty());
		try {
			assertEquals(keyStore.readUsername(), username);
		} catch (UserNotInitiatedException e) {
			e.printStackTrace();
			fail(USER_INIT_FAIL);
		}

	}

}
