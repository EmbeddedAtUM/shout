
package org.whispercomm.shout.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.KeyGenerator;
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
			ECKeyPair keyPair = new KeyGenerator().generateKeyPair();
			boolean status = keyStore.writeMe(username, keyPair);
			assertTrue(status);
			assertFalse(keyStore.isEmpty());
			ECKeyPair fromStore;
			fromStore = keyStore.readKeyPair();
			assertEquals(keyPair.getPublicKey(), fromStore.getPublicKey());
			assertEquals(keyPair.getPrivateKey(), fromStore.getPrivateKey());
		} catch (UserNotInitiatedException e) {
			e.printStackTrace();
			fail(USER_INIT_FAIL);
		}
	}

	@Test
	public void testReadWriteUsername() {
		ECKeyPair keyPair = new KeyGenerator().generateKeyPair();
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
