
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
import org.whispercomm.shout.ShoutTestRunner;
import org.whispercomm.shout.Utility;

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

	private static final String CONTENT = "And then I was like, oh no you didn't! And she was like mmmmhmmm and I was like aw hell no";
	private static final long TIME = 100L;

	private ContentResolver cr = new Activity().getContentResolver();

	private byte[] userKey;
	private Uri userLocation;
	private int userId;

	private byte[] hash;
	private byte[] sig;
	private Uri shoutLocation;
	private int shoutId;

	private int numDavids = 2;

	private byte[][] hashes;

	private byte[][] sigs;

	private byte[][] keys;

	@Before
	public void setUp() {
		userKey = Utility.genByteArray(16);
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Users.USERNAME, NAME);
		values.put(ShoutProviderContract.Users.PUB_KEY,
				Base64.encodeToString(userKey, Base64.DEFAULT));
		userLocation = cr.insert(ShoutProviderContract.Users.CONTENT_URI, values);
		assertNotNull(userLocation);
		userId = Integer.valueOf(userLocation.getLastPathSegment());

		hash = Utility.genByteArray(16);
		sig = Utility.genByteArray(16);
		values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.AUTHOR, userId);
		values.put(ShoutProviderContract.Shouts.MESSAGE, CONTENT);
		values.put(ShoutProviderContract.Shouts.TIME, TIME);
		values.put(ShoutProviderContract.Shouts.HASH, Base64.encodeToString(hash, Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.SIGNATURE,
				Base64.encodeToString(sig, Base64.DEFAULT));
		shoutLocation = cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
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
		assertEquals(CONTENT, content);

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
		insertFourUsers();
		String[] projection = {
				ShoutProviderContract.Users._ID,
				ShoutProviderContract.Users.USERNAME
		};
		String selection = ShoutProviderContract.Users.USERNAME + " = ?";
		String[] selectionArgs = {
				"David"
		};
		Cursor cursor = cr.query(ShoutProviderContract.Users.CONTENT_URI, projection, selection,
				selectionArgs, null);
		assertNotNull(cursor);
		assertTrue(cursor.getCount() == numDavids);
		int idIndex = cursor.getColumnIndex(ShoutProviderContract.Users._ID);
		int nameIndex = cursor.getColumnIndex(ShoutProviderContract.Users.USERNAME);
		int lastId = -1;
		while (cursor.moveToNext()) {
			int id = cursor.getInt(idIndex);
			assertFalse(id == lastId);
			String name = cursor.getString(nameIndex);
			assertEquals(name, "David");
		}
	}

	@Test
	public void testSelectShoutsWithParameters() {
		insertFourUsers();
		insertFourShouts();
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
		Cursor cursor = cr.query(ShoutProviderContract.Shouts.CONTENT_URI, projection, selection, selectionArgs, null);
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
		values.put(ShoutProviderContract.Shouts.HASH, Base64.encodeToString(Utility.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.MESSAGE, "oh noes i dont have an author");
		values.put(ShoutProviderContract.Shouts.SIGNATURE, Base64.encodeToString(Utility.genByteArray(4), Base64.DEFAULT));
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
		values.put(ShoutProviderContract.Shouts.HASH, Base64.encodeToString(Utility.genByteArray(4), Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.MESSAGE, "oh noes i dont have an author");
		values.put(ShoutProviderContract.Shouts.SIGNATURE, Base64.encodeToString(Utility.genByteArray(4), Base64.DEFAULT));
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
		insertFourUsers();
		insertFourShouts();
		Uri uri = Uri.withAppendedPath(ShoutProviderContract.Shouts.CONTENT_URI, "user/2");
		Cursor cursor = cr.query(uri, null, null, null, null);
		assertNotNull(cursor);
		assertTrue(cursor.getCount() == 2);
		int authorIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.AUTHOR);
		while (cursor.moveToNext()) {
			assertEquals(2, cursor.getInt(authorIndex));
		}
	}

	private void insertFourUsers() {
		String[] usernames = {
				"David", "David", "Yue", "Prof Dick"
		};
		this.numDavids = 2;
		this.keys = new byte[usernames.length][];
		for (int i = 0; i < usernames.length; i++) {
			byte[] key = Utility.genByteArray(32);
			keys[i] = key;
			ContentValues values = new ContentValues();
			values.put(ShoutProviderContract.Users.USERNAME, usernames[i]);
			values.put(ShoutProviderContract.Users.PUB_KEY, key);
			Uri at = cr.insert(ShoutProviderContract.Users.CONTENT_URI, values);
			assertNotNull(at);
			assertTrue(Integer.valueOf(at.getLastPathSegment()) > 0);
		}
	}

	private void insertFourShouts() {
		String[] messages = {
				"Herp", "Derp", "Narwal", "Bacon"
		};
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
			byte[] hash = Utility.genByteArray(10);
			hashes[i] = hash;
			byte[] sig = Utility.genByteArray(32);
			sigs[i] = sig;
			ContentValues values = new ContentValues();
			values.put(ShoutProviderContract.Shouts.AUTHOR, authors[i]);
			values.put(ShoutProviderContract.Shouts.HASH,
					Base64.encodeToString(hash, Base64.DEFAULT));
			values.put(ShoutProviderContract.Shouts.MESSAGE, messages[i]);
			values.put(ShoutProviderContract.Shouts.PARENT, parent);
			values.put(ShoutProviderContract.Shouts.SIGNATURE,
					Base64.encodeToString(sig, Base64.DEFAULT));
			values.put(ShoutProviderContract.Shouts.TIME, times[i]);
			cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
		}
	}
}
