
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestShout;
import org.whispercomm.shout.test.util.TestUser;
import org.whispercomm.shout.test.util.TestUtility;

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderContractTest {

	private static final String NAME = "duiu";
	private static final String MESSAGE = "Can you repeat the part of the stuff where you said all about the things?";
	private static final long TIME = 8675309L;
	private static final byte[] SIGNATURE = TestFactory.genByteArray(10);
	private static final byte[] HASH = TestFactory.genByteArray(16);

	private Context context;
	private User testUser;
	private Shout testShout;

	@Before
	public void setUp() {
		this.context = new Activity();
		testUser = new TestUser(NAME);
		testShout = new TestShout(testUser, null, MESSAGE, new DateTime(TIME), SIGNATURE, HASH);
	}

	@After
	public void takeDown() {
		this.context = null;
		this.testUser = null;
		this.testShout = null;
	}

	@Test
	public void testRetrieveUser() {
		int id = ShoutProviderContract.storeUser(context, testUser);
		User fromDb = ShoutProviderContract.retrieveUserById(context, id);
		assertNotNull(fromDb);
		assertTrue(TestUtility.testEqualUserFields(testUser, fromDb));
	}

	@Test
	public void testRetrieveShout() {
		ShoutProviderContract.storeUser(context, testUser);
		int shoutId = ShoutProviderContract.storeShout(context, testShout);
		Shout fromDb = ShoutProviderContract.retrieveShoutById(context, shoutId);
		assertNotNull(fromDb);
		assertTrue(TestUtility.testEqualShoutFields(testShout, fromDb));
	}

	@Test
	public void testStoreShoutWithParent() {
		User sender = new TestUser("drbeagle");
		Shout withParent = new TestShout(sender, testShout,
				"This is what happens when you Google people you work with", new DateTime(),
				TestFactory.genByteArray(10), TestFactory.genByteArray(10));
		int commentId = ShoutProviderContract.storeShout(context, withParent);
		assertTrue(commentId > 0);
		Shout fromDbWithParent = ShoutProviderContract.retrieveShoutById(context, commentId);
		assertTrue(TestUtility.testEqualShoutFields(withParent, fromDbWithParent));
		
		Shout withGrandparent = new TestShout(testUser, withParent, null, new DateTime(),
				TestFactory.genByteArray(4), TestFactory.genByteArray(8));
		int commentReshoutId = ShoutProviderContract.storeShout(context, withGrandparent);
		assertTrue(commentReshoutId > 0);
		Shout fromDbWithGrandParent = ShoutProviderContract.retrieveShoutById(context, commentReshoutId);
		assertTrue(TestUtility.testEqualShoutFields(withGrandparent, fromDbWithGrandParent));
	}

	@Test
	public void testStoreUser() {
		int id = ShoutProviderContract.storeUser(context, testUser);
		assertTrue(id > 0);
	}

	@Test
	public void testStoreShout() {
		int userId = ShoutProviderContract.storeUser(context, testUser);
		assertTrue(userId > 0);
		int shoutId = ShoutProviderContract.storeShout(context, testShout);
		assertTrue(shoutId > 0);
	}

	@Test
	public void testStoreShoutWithoutUserInDatabase() {
		int id = ShoutProviderContract.storeShout(context, testShout);
		assertTrue(id > 0);
		Shout fromDb = ShoutProviderContract.retrieveShoutById(context, id);
		assertTrue(TestUtility.testEqualShoutFields(testShout, fromDb));
	}

	@Test
	public void testStoreUserAlreadyInDatabase() {
		int id = ShoutProviderContract.storeUser(context, testUser);
		User fromDb = ShoutProviderContract.retrieveUserById(context, id);
		assertNotNull(fromDb);
		int newId = ShoutProviderContract.storeUser(context, fromDb);
		assertEquals(id, newId);
	}

	@Test
	public void testStoreShoutAlreadyInDatabase() {
		int id = ShoutProviderContract.storeShout(context, testShout);
		Shout fromDb = ShoutProviderContract.retrieveShoutById(context, id);
		assertNotNull(fromDb);
		int newId = ShoutProviderContract.storeShout(context, fromDb);
		assertEquals(id, newId);
	}
}
