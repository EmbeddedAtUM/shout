
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertNotNull;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestShout;
import org.whispercomm.shout.test.util.TestUtility;

import android.app.Activity;
import android.content.Context;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderContractTest {

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
		this.testUser = TestFactory.TEST_USER_1;
		this.testShout = new TestShout(testUser, null, MESSAGE, new DateTime(TIME), SIGNATURE, HASH);
	}

	@After
	public void takeDown() {
		this.context = null;
		this.testUser = null;
		this.testShout = null;
	}

	@Test
	public void testRetrieveUser() {
		LocalUser localUser = ShoutProviderContract.saveUser(context, testUser);
		assertNotNull(localUser);
		TestUtility.testEqualUserFields(testUser, localUser);
		LocalUser fromDb = ShoutProviderContract
				.retrieveUserByKey(context, testUser.getPublicKey());
		assertNotNull(fromDb);
		TestUtility.testEqualUserFields(testUser, fromDb);
		TestUtility.testEqualUserFields(localUser, fromDb);
	}

	@Test
	public void testRetrieveShout() {
		LocalShout localShout = ShoutProviderContract.saveShout(context, testShout);
		assertNotNull(localShout);
		TestUtility.testEqualShoutFields(testShout, localShout);
		LocalShout fromDb = ShoutProviderContract.retrieveShoutByHash(context, testShout.getHash());
		assertNotNull(fromDb);
		TestUtility.testEqualShoutFields(testShout, fromDb);
		TestUtility.testEqualShoutFields(localShout, fromDb);
	}

	@Test
	public void testStoreShoutWithParent() {
		User sender = TestFactory.TEST_USER_1;
		Shout withParent = new TestShout(sender, testShout,
				"This is what happens when you Google people you work with", new DateTime(),
				TestFactory.genByteArray(10), TestFactory.genByteArray(10));
		LocalShout comment = ShoutProviderContract.saveShout(context, withParent);
		assertNotNull(comment);
		LocalShout parentFromDb = comment.getParent();
		assertNotNull(parentFromDb);
		TestUtility.testEqualShoutFields(withParent, comment);

		Shout withGrandparent = new TestShout(testUser, withParent, null, new DateTime(),
				TestFactory.genByteArray(4), TestFactory.genByteArray(8));
		LocalShout localWithGrandparent = ShoutProviderContract.saveShout(context, withGrandparent);
		assertNotNull(localWithGrandparent);
		TestUtility.testEqualShoutFields(withGrandparent, localWithGrandparent);
	}

	@Test
	public void testStoreUser() {
		LocalUser fromDb = ShoutProviderContract.saveUser(context, testUser);
		assertNotNull(fromDb);
		TestUtility.testEqualUserFields(testUser, fromDb);
	}

	@Test
	public void testStoreShout() {
		ShoutProviderContract.saveUser(context, testUser);
		LocalShout fromDb = ShoutProviderContract.saveShout(context, testShout);
		assertNotNull(fromDb);
		TestUtility.testEqualShoutFields(testShout, fromDb);
	}

	@Test
	public void testStoreShoutWithoutUserInDatabase() {
		LocalShout fromDb = ShoutProviderContract.saveShout(context, testShout);
		assertNotNull(fromDb);
		TestUtility.testEqualShoutFields(testShout, fromDb);
	}

	@Test
	public void testStoreUserAlreadyInDatabase() {
		LocalUser first = ShoutProviderContract.saveUser(context, testUser);
		LocalUser second = ShoutProviderContract.saveUser(context, testUser);
		TestUtility.testEqualUserFields(first, second);
		LocalUser search = ShoutProviderContract
				.retrieveUserByKey(context, testUser.getPublicKey());
		TestUtility.testEqualUserFields(testUser, search);
	}

	@Test
	public void testStoreShoutAlreadyInDatabase() {
		LocalShout first = ShoutProviderContract.saveShout(context, testShout);
		LocalShout second = ShoutProviderContract.saveShout(context, testShout);
		TestUtility.testEqualShoutFields(first, second);
		LocalShout search = ShoutProviderContract.retrieveShoutByHash(context, testShout.getHash());
		TestUtility.testEqualShoutFields(testShout, search);
	}
}
