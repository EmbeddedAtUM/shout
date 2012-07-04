
package org.whispercomm.shout.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class KeyStorageImplTest {

	private KeyStorage keyStore;
	private Context context;

	@Before
	public void setUp() {
		this.context = new Activity();
		this.keyStore = new KeyStorageSharedPrefs(context);
	}

	@After
	public void takeDown() {
		this.keyStore = null;
	}

	@Test
	public void testInitEmptyOnCreate() {
		assertTrue(keyStore.isEmpty());
	}

	@Test
	public void testReadWriteKeyPair() {
		int id = 1;
		KeyPair keyPair = TestFactory.genKeyPair();
		keyStore.writeKeyPair(id, keyPair);
		assertFalse(keyStore.isEmpty());
		KeyPair fromStore = keyStore.readKeyPair();
		assertEquals(keyPair.getPublic(), fromStore.getPublic());
		assertEquals(keyPair.getPrivate(), fromStore.getPrivate());
	}

	@Test
	public void testReadWriteId() {
		int id = 1;
		KeyPair keyPair = TestFactory.genKeyPair();
		keyStore.writeKeyPair(1, keyPair);
		assertFalse(keyStore.isEmpty());
		int fromStore = keyStore.getId();
		assertEquals(id, fromStore);
	}
	
	@Test
	public void testCannotStoreInvalidId() {
		int id = 0;
		KeyPair keyPair = TestFactory.genKeyPair();
		keyStore.writeKeyPair(id, keyPair);
		assertTrue(keyStore.isEmpty());
	}

}
