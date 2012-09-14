
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Base64;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderTest {

	private Context context;
	private ContentResolver cr;
	private User user;
	private String base64Key;
	private Shout shout;
	private String base64Hash;
	private Uri userLocation;
	private Uri shoutLocation;
	private int shoutId;
	private int userId;

	@Before
	public void setUp() {
		this.context = new Activity();
		this.cr = context.getContentResolver();
		this.user = TestFactory.TEST_USER_1;
		this.base64Key = Base64.encodeToString(KeyGenerator.encodePublic(user.getPublicKey()),
				Base64.DEFAULT);
		this.shout = TestFactory.ROOT_SHOUT;
		this.base64Hash = Base64.encodeToString(shout.getHash(), Base64.DEFAULT);
		this.userLocation = ProviderTestUtility.insertIntoUserTable(cr, user.getUsername(),
				KeyGenerator.encodePublic(user.getPublicKey()));
		this.shoutLocation = ProviderTestUtility.insertIntoShoutTable(cr,
				KeyGenerator.encodePublic(shout.getSender()
						.getPublicKey()), null, shout.getMessage(), shout.getTimestamp()
						.getMillis(),
				shout.getSignature(), shout.getHash());
		this.userId = Integer.valueOf(userLocation.getLastPathSegment());
		this.shoutId = Integer.valueOf(shoutLocation.getLastPathSegment());
	}

	@After
	public void tearDown() {
		this.context = null;
		this.cr = null;
	}

	@Test
	public void testUserInsert() {
		assertTrue(userId > 0);
		Cursor cursor = cr.query(userLocation, null, null, null, null);
		assertTrue(cursor.moveToNext());

		int idIndex = cursor.getColumnIndex(ShoutProviderContract.Users._ID);
		int keyIndex = cursor.getColumnIndex(ShoutProviderContract.Users.PUB_KEY);
		int nameIndex = cursor.getColumnIndex(ShoutProviderContract.Users.USERNAME);

		int newId = cursor.getInt(idIndex);
		assertEquals(userId, newId);

		String encodedKey = cursor.getString(keyIndex);
		assertArrayEquals(KeyGenerator.encodePublic(user.getPublicKey()),
				Base64.decode(encodedKey, Base64.DEFAULT));

		String username = cursor.getString(nameIndex);
		assertEquals(user.getUsername(), username);

		assertFalse(cursor.moveToNext());
	}

	@Test
	public void testShoutInsert() {
		Cursor cursor = cr.query(shoutLocation, null, null, null, null);
		assertNotNull(cursor);
		assertTrue(cursor.moveToNext());
		int idIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts._ID);
		int authorIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.AUTHOR);
		int messageIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.MESSAGE);
		int timeIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.TIME_SENT);
		int hashIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.HASH);
		int sigIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.SIGNATURE);

		int newId = cursor.getInt(idIndex);
		assertEquals(shoutId, newId);

		String author = cursor.getString(authorIndex);
		assertEquals(base64Key, author);

		String content = cursor.getString(messageIndex);
		assertEquals(shout.getMessage(), content);

		long time = cursor.getLong(timeIndex);
		assertEquals(shout.getTimestamp().getMillis(), time);

		String encodedHash = cursor.getString(hashIndex);
		assertEquals(base64Hash, encodedHash);

		String encodedSig = cursor.getString(sigIndex);
		assertEquals(shout.getSignature(),
				DsaSignature.decode(Base64.decode(encodedSig, Base64.DEFAULT)));

		assertFalse(cursor.moveToNext());
	}

	@Test
	public void testShoutForeignKeyUser() {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.AUTHOR, TestFactory.generateRandomBase64String(32));
		values.put(ShoutProviderContract.Shouts.HASH,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.MESSAGE, "oh noes i dont have an author");
		values.put(ShoutProviderContract.Shouts.SIGNATURE,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.TIME_SENT, 101L);
		try {
			cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
		} catch (SQLException e) {
			return;
		}
		fail("Did not catch exception on non-existant author");
	}

	@Test
	public void testShoutForeignKeyParent() {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.AUTHOR, base64Key);
		values.put(ShoutProviderContract.Shouts.HASH,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.MESSAGE, "oh noes i dont have a parent");
		values.put(ShoutProviderContract.Shouts.SIGNATURE,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.TIME_SENT, 101L);
		values.put(ShoutProviderContract.Shouts.PARENT, 9001);
		try {
			cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
		} catch (SQLException e) {
			return;
		}
		fail("Did not catch exception on non-existant parent");
	}

	@Test
	public void testInsertDuplicateShoutDoesNothing() {
		try {
			ProviderTestUtility.insertIntoShoutTable(cr,
					KeyGenerator.encodePublic(user.getPublicKey()), null,
					shout.getMessage(), shout.getTimestamp().getMillis(), shout.getSignature(),
					shout.getHash());
		} catch (SQLException e) {
			fail("Inserting a duplicate should not throw an exception");
		}
	}

	@Test
	public void testInsertDuplicateUserDoesNothing() {
		try {
			ProviderTestUtility.insertIntoUserTable(cr, user.getUsername(),
					KeyGenerator.encodePublic(user.getPublicKey()));
		} catch (SQLException e) {
			fail("Inserting a duplicate should not throw an exception");
		}
	}
}
