
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
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Base64;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderTest {

	private static final String NAME = "duiu";

	private static final String MESSAGE = "And then I was like, oh no you didn't! And she was like mmmmhmmm and I was like aw hell no";
	private static final long TIME = 100L;

	private ContentResolver cr = new Activity().getContentResolver();

	private byte[] userKey;
	private Uri userLocation;
	private int userId;

	private byte[] hash;
	private byte[] sig;
	private Uri shoutLocation;
	private int shoutId;

	private static final String USERNAME_DUPE = "David";
	private static final String[] USERNAMES = {
			USERNAME_DUPE, USERNAME_DUPE, "Yue", "Prof Dick"
	};
	private static final int NUM_USER_SAME_NAME = 2;

	private static final String[] MESSAGES = {
			"Herp", "Derp", "Narwal", "Bacon"
	};

	private byte[][] hashes;

	private byte[][] sigs;

	private byte[][] keys;

	@Before
	public void setUp() {
		userKey = TestFactory.genByteArray(16);
		userLocation = ShoutProviderTestUtility.insertIntoUserTable(cr, NAME, userKey);
		assertNotNull(userLocation);
		userId = Integer.valueOf(userLocation.getLastPathSegment());
		assertTrue(userId > 0);

		hash = TestFactory.genByteArray(16);
		sig = TestFactory.genByteArray(16);
		shoutLocation = ShoutProviderTestUtility.insertIntoShoutTable(cr, userId, -1, MESSAGE,
				TIME, sig, hash);
		assertNotNull(shoutLocation);
		shoutId = Integer.valueOf(shoutLocation.getLastPathSegment());
	}

	@After
	public void tearDown() {
		cr = new Activity().getContentResolver();
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
		assertArrayEquals(userKey, Base64.decode(encodedKey, Base64.DEFAULT));

		String username = cursor.getString(nameIndex);
		assertEquals(NAME, username);

		assertFalse(cursor.moveToNext());
	}

	@Test
	public void testShoutInsert() {

		assertTrue(shoutId > 0);

		Cursor cursor = cr.query(shoutLocation, null, null, null, null);
		assertNotNull(cursor);
		assertTrue(cursor.moveToNext());
		int idIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts._ID);
		int authorIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.AUTHOR);
		int messageIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.MESSAGE);
		int timeIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.TIME);
		int hashIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.HASH);
		int sigIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.SIGNATURE);

		int newId = cursor.getInt(idIndex);
		assertEquals(shoutId, newId);

		int author = cursor.getInt(authorIndex);
		assertEquals(1, author);

		String content = cursor.getString(messageIndex);
		assertEquals(MESSAGE, content);

		long time = cursor.getLong(timeIndex);
		assertEquals(TIME, time);

		String encodedHash = cursor.getString(hashIndex);
		assertArrayEquals(hash, Base64.decode(encodedHash, Base64.DEFAULT));

		String encodedSig = cursor.getString(sigIndex);
		assertArrayEquals(sig, Base64.decode(encodedSig, Base64.DEFAULT));

		assertFalse(cursor.moveToNext());
	}

	@Test
	public void testSelectManyUser() {
		insertFourUsers(USERNAMES);
		String[] projection = {
				ShoutProviderContract.Users._ID,
				ShoutProviderContract.Users.USERNAME
		};
		String selection = ShoutProviderContract.Users.USERNAME + " = ?";
		String[] selectionArgs = {
				USERNAME_DUPE
		};
		Cursor cursor = cr.query(ShoutProviderContract.Users.CONTENT_URI, projection, selection,
				selectionArgs, null);
		assertNotNull(cursor);
		assertTrue(cursor.getCount() == NUM_USER_SAME_NAME);
		int idIndex = cursor.getColumnIndex(ShoutProviderContract.Users._ID);
		int nameIndex = cursor.getColumnIndex(ShoutProviderContract.Users.USERNAME);
		int lastId = -1;
		while (cursor.moveToNext()) {
			int id = cursor.getInt(idIndex);
			assertFalse(id == lastId);
			String name = cursor.getString(nameIndex);
			assertEquals(name, USERNAME_DUPE);
			lastId = id;
		}
	}

	@Test
	public void testSelectShoutsWithParameters() {
		insertFourUsers(USERNAMES);
		insertFourShouts(MESSAGES);
		String[] projection = {
				ShoutProviderContract.Shouts.AUTHOR,
				ShoutProviderContract.Shouts.MESSAGE
		};
		String selection = ShoutProviderContract.Shouts.AUTHOR + " = ? AND "
				+ ShoutProviderContract.Shouts.MESSAGE + " = ?";
		String[] selectionArgs = {
				"1",
				"Derp"
		};
		Cursor cursor = cr.query(ShoutProviderContract.Shouts.CONTENT_URI, projection, selection,
				selectionArgs, null);
		assertNotNull(cursor);
		assertTrue(cursor.moveToNext());
		int authorIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.AUTHOR);
		int messageIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.MESSAGE);
		int author = cursor.getInt(authorIndex);
		String message = cursor.getString(messageIndex);
		assertEquals(1, author);
		assertEquals("Derp", message);
		assertFalse(cursor.moveToNext());
	}

	@Test
	public void testShoutForeignKeyUser() {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.AUTHOR, 9001);
		values.put(ShoutProviderContract.Shouts.HASH,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.MESSAGE, "oh noes i dont have an author");
		values.put(ShoutProviderContract.Shouts.SIGNATURE,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.TIME, 101L);
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
		values.put(ShoutProviderContract.Shouts.AUTHOR, 1);
		values.put(ShoutProviderContract.Shouts.HASH,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.MESSAGE, "oh noes i dont have an author");
		values.put(ShoutProviderContract.Shouts.SIGNATURE,
				Base64.encodeToString(TestFactory.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.TIME, 101L);
		values.put(ShoutProviderContract.Shouts.PARENT, 9001);
		try {
			cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
		} catch (SQLException e) {
			return;
		}
		fail("Did not catch exception on non-existant parent");
	}

	@Test
	public void testSelectShoutByUserURI() {
		insertFourUsers(USERNAMES);
		insertFourShouts(MESSAGES);
		Uri uri = Uri.withAppendedPath(ShoutProviderContract.Shouts.CONTENT_URI, "user/2");
		Cursor cursor = cr.query(uri, null, null, null, null);
		assertNotNull(cursor);
		assertTrue(cursor.getCount() == 2);
		int authorIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.AUTHOR);
		while (cursor.moveToNext()) {
			assertEquals(2, cursor.getInt(authorIndex));
		}
	}

	private void insertFourUsers(String[] usernames) {
		this.keys = new byte[usernames.length][];
		for (int i = 0; i < usernames.length; i++) {
			keys[i] = TestFactory.genByteArray(32);
			Uri at = ShoutProviderTestUtility.insertIntoUserTable(cr, usernames[i], keys[i]);
			assertNotNull(at);
			assertTrue(Integer.valueOf(at.getLastPathSegment()) > 0);
		}
	}

	private void insertFourShouts(String[] messages) {
		int[] authors = {
				1, 1, 2, 2
		};
		int parent = shoutId;
		long[] times = {
				100, 200, 300, 400
		};
		this.hashes = new byte[messages.length][];
		this.sigs = new byte[messages.length][];
		for (int i = 0; i < messages.length; i++) {
			hashes[i] = TestFactory.genByteArray(10); 
			sigs[i] = TestFactory.genByteArray(32);
			Uri at = ShoutProviderTestUtility.insertIntoShoutTable(cr, authors[i], parent,
					messages[i], times[i], sigs[i], hashes[i]);
			assertNotNull(at);
			assertTrue(Integer.valueOf(at.getLastPathSegment()) > 0);
		}
	}
}
