
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.interfaces.ECPublicKey;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.Tag;
import org.whispercomm.shout.User;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;
import org.whispercomm.shout.test.util.TestShout;
import org.whispercomm.shout.test.util.TestTag;
import org.whispercomm.shout.test.util.TestUser;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderContractTest {

	private static final String NAME = "duiu";
	private static final String MESSAGE = "Can you repeat the part of the stuff where you said all about the things?";
	private static final long TIME = 8675309L;
	private static final byte[] SIGNATURE = TestFactory.genByteArray(10);
	private static final byte[] HASH = TestFactory.genByteArray(16);

	private static final int AUTHOR = 1;
	private static final int PARENT = 1;
	private static final int TAG_ID = 1;

	private static final String TAG_NAME = "this_is_a_tag";

	Uri firstUserLocation;
	Uri firstShoutLocation;
	Uri firstTagLocation;

	private Context context;
	private ContentResolver cr;

	private ECPublicKey ecPubKey;
	private byte[] keyBytes;

	@Before
	public void setUp() {
		this.context = new Activity();
		this.cr = this.context.getContentResolver();

		this.ecPubKey = (ECPublicKey) TestFactory.genKeyPair().getPublic();
		this.keyBytes = this.ecPubKey.getEncoded();

		this.firstUserLocation = ProviderTestUtility.insertIntoUserTable(
				cr, NAME, this.keyBytes);
		assertEquals(AUTHOR,
				Integer.valueOf(firstUserLocation.getLastPathSegment())
						.intValue());
		this.firstShoutLocation = ProviderTestUtility
				.insertIntoShoutTable(cr, 1, -1, MESSAGE, TIME, SIGNATURE, HASH);
		assertEquals(PARENT,
				Integer.valueOf(firstShoutLocation.getLastPathSegment())
						.intValue());

		this.firstTagLocation = ProviderTestUtility.insertIntoTagTable(cr, TAG_NAME);
		assertNotNull(firstTagLocation);
		assertEquals(TAG_ID, Integer.valueOf(firstTagLocation.getLastPathSegment()).intValue());
	}

	@After
	public void takeDown() {
		this.cr = null;
	}

	@Test
	public void testRetrieveUser() {
		User fromDb = ShoutProviderContract.retrieveUserById(context, AUTHOR);
		assertNotNull(fromDb);
		assertEquals(NAME, fromDb.getUsername());
		assertEquals(this.ecPubKey, fromDb.getPublicKey());
		assertArrayEquals(this.keyBytes, fromDb.getPublicKey().getEncoded());
	}

	@Test
	public void testRetrieveShout() {
		Shout fromDb = ShoutProviderContract.retrieveShoutById(context, PARENT);
		assertNotNull(fromDb);
		assertNotNull(fromDb.getSender());
		assertEquals(MESSAGE, fromDb.getMessage());
		assertEquals(TIME, fromDb.getTimestamp().getMillis());
		assertArrayEquals(SIGNATURE, fromDb.getSignature());
		assertArrayEquals(HASH, fromDb.getHash());
		assertNull(fromDb.getParent());
	}
	
	@Test
	public void testRetrieveTag() {
		Tag fromDb = ShoutProviderContract.retrieveTagById(context, TAG_ID);
		assertNotNull(fromDb);
		String name = fromDb.getName();
		assertNotNull(name);
		assertEquals(TAG_NAME, name);
	}

	@Test
	public void testStoreTag() {
		String tagName = "thingsthatmakedavidbfeelold";
		TestTag tag = new TestTag(tagName);
		int id = ShoutProviderContract.storeTag(context, tag);
		assertTrue(id > 0);
		assertTrue(id != TAG_ID);
		Tag fromDb = ShoutProviderContract.retrieveTagById(context, id);
		assertEquals(tagName, fromDb.getName());
	}
	
	@Test
	public void testStoreUser() {
		String username = "drbild"; // It's funny because it looks like Dr.
		ECPublicKey ecKey = (ECPublicKey) TestFactory.genKeyPair().getPublic();
		TestUser user = new TestUser(username, ecKey);
		int id = ShoutProviderContract.storeUser(context, user);
		assertTrue(id > 0);
		User testFromDb = ShoutProviderContract.retrieveUserById(context, id);
		assertNotNull(testFromDb);
		assertEquals(username, testFromDb.getUsername());
		assertArrayEquals(ecKey.getEncoded(), testFromDb.getPublicKey().getEncoded());
	}

	@Test
	public void testStoreShout() {
		User author = ShoutProviderContract.retrieveUserById(context, AUTHOR);
		assertNotNull(author);
		Shout parent = ShoutProviderContract.retrieveShoutById(context, PARENT);
		assertNull(parent.getParent());
		assertNotNull(parent);
		TestShout shout = new TestShout(author, parent, "This shout has a parent", new DateTime(),
				TestFactory.genByteArray(8), TestFactory.genByteArray(12));
		int id = ShoutProviderContract.storeShout(context, shout);
		assertTrue(id > 0);
		Shout fromDb = ShoutProviderContract.retrieveShoutById(context, id);
		assertEquals(shout.getMessage(), fromDb.getMessage());
		assertEquals(shout.getTimestamp().getMillis(), fromDb.getTimestamp().getMillis());
		assertArrayEquals(shout.getHash(), fromDb.getHash());
		assertArrayEquals(shout.getSignature(), fromDb.getSignature());
		assertNotNull(fromDb.getParent());
		assertArrayEquals(parent.getHash(), fromDb.getParent().getHash());
		assertArrayEquals(parent.getSignature(), fromDb.getParent().getSignature());
		assertNull(fromDb.getParent().getParent());
	}

	@Test
	public void testStoreUserAlreadyInDatabase() {
		User fromDb = ShoutProviderContract.retrieveUserById(context, AUTHOR);
		assertNotNull(fromDb);
		int id = ShoutProviderContract.storeUser(context, fromDb);
		assertTrue(id == AUTHOR);
	}

	@Test
	public void testStoreShoutAlreadyInDatabase() {
		Shout fromDb = ShoutProviderContract.retrieveShoutById(context, PARENT);
		assertNotNull(fromDb);
		int id = ShoutProviderContract.storeShout(context, fromDb);
		assertTrue(id == PARENT);
	}
	
	@Test
	public void testStoreTagAlreadyInDatabase() {
		Tag fromDb = ShoutProviderContract.retrieveTagById(context, TAG_ID);
		assertNotNull(fromDb);
		int id = ShoutProviderContract.storeTag(context, fromDb);
		assertTrue(id == TAG_ID);
	}
}
