
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.whispercomm.shout.provider.ProviderTestFactory.COMMENT_SHOUT_VALUES;
import static org.whispercomm.shout.provider.ProviderTestFactory.RECOMMENT_SHOUT_VALUES;
import static org.whispercomm.shout.provider.ProviderTestFactory.ROOT_SHOUT_VALUES;
import static org.whispercomm.shout.provider.ProviderTestFactory.USER_1_ID;
import static org.whispercomm.shout.provider.ProviderTestFactory.USER_1_VALUES;
import static org.whispercomm.shout.provider.ProviderTestFactory.USER_2_VALUES;
import static org.whispercomm.shout.provider.ProviderTestFactory.USER_3_VALUES;
import static org.whispercomm.shout.provider.ProviderTestFactory.clearAllContentValues;
import static org.whispercomm.shout.provider.ProviderTestFactory.initAllContentValues;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.shout.test.ShoutTestRunner;
import org.whispercomm.shout.test.util.TestFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

@RunWith(ShoutTestRunner.class)
public class ShoutProviderTest {

	private Context context;
	private ContentResolver cr;

	@Before
	public void setUp() {
		this.context = new Activity();
		this.cr = context.getContentResolver();
		initAllContentValues();
	}

	@After
	public void tearDown() {
		this.context = null;
		this.cr = null;
		clearAllContentValues();
	}

	@Test
	public void testUserInsert() {
		Uri userLocation = cr.insert(ShoutProviderContract.Users.CONTENT_URI,
				USER_1_VALUES);
		assertEquals((int) ContentUris.parseId(userLocation), USER_1_ID);
		Cursor cursor = cr.query(userLocation, null, null, null, null);
		assertTrue(cursor.moveToNext());

		int idIndex = cursor.getColumnIndex(ShoutProviderContract.Users._ID);
		int keyIndex = cursor.getColumnIndex(ShoutProviderContract.Users.PUB_KEY);
		int nameIndex = cursor.getColumnIndex(ShoutProviderContract.Users.USERNAME);
		int avatarIndex = cursor.getColumnIndex(ShoutProviderContract.Users.AVATAR);

		int newId = cursor.getInt(idIndex);
		assertEquals(USER_1_ID, newId);

		String encodedKey = cursor.getString(keyIndex);
		assertEquals(
				USER_1_VALUES.getAsString(ShoutProviderContract.Users.PUB_KEY),
				encodedKey);

		String username = cursor.getString(nameIndex);
		assertEquals(
				USER_1_VALUES.getAsString(ShoutProviderContract.Users.USERNAME),
				username);

		String encodedAvatarHash = cursor.getString(avatarIndex);
		assertEquals(USER_1_VALUES.getAsString(ShoutProviderContract.Users.AVATAR),
				encodedAvatarHash);
	}

	@Test
	public void testShoutInsert() {
		cr.insert(ShoutProviderContract.Users.CONTENT_URI, USER_1_VALUES);
		Uri shoutLocation = cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, ROOT_SHOUT_VALUES);
		assertNotNull(shoutLocation);
		int shoutId = (int) ContentUris.parseId(shoutLocation);
		assertEquals(shoutId, 1);
		Cursor cursor = cr.query(shoutLocation, null, null, null, null);
		assertNotNull(cursor);
		assertTrue(cursor.moveToNext());
		int idIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts._ID);
		int authorIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.AUTHOR);
		int messageIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.MESSAGE);
		int timeIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.TIME_SENT);
		int timeRecvIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.TIME_RECEIVED);
		int hashIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.HASH);
		int sigIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.SIGNATURE);
		int latIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.LATITUDE);
		int longIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.LONGITUDE);
		int userIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.USER_PK);

		int newId = cursor.getInt(idIndex);
		assertEquals(shoutId, newId);

		String encodedAuthor = cursor.getString(authorIndex);
		assertEquals(ROOT_SHOUT_VALUES.getAsString(ShoutProviderContract.Shouts.AUTHOR),
				encodedAuthor);

		String message = cursor.getString(messageIndex);
		assertEquals(ROOT_SHOUT_VALUES.getAsString(ShoutProviderContract.Shouts.MESSAGE), message);

		long timeSent = cursor.getLong(timeIndex);
		assertEquals((long) ROOT_SHOUT_VALUES.getAsLong(ShoutProviderContract.Shouts.TIME_SENT),
				timeSent);

		long timeReceived = cursor.getLong(timeRecvIndex);
		assertEquals(
				(long) ROOT_SHOUT_VALUES.getAsLong(ShoutProviderContract.Shouts.TIME_RECEIVED),
				timeReceived);

		String encodedHash = cursor.getString(hashIndex);
		assertEquals(ROOT_SHOUT_VALUES.getAsString(ShoutProviderContract.Shouts.HASH), encodedHash);

		String encodedSignature = cursor.getString(sigIndex);
		assertEquals(ROOT_SHOUT_VALUES.getAsString(ShoutProviderContract.Shouts.SIGNATURE),
				encodedSignature);

		// One of the few situations where it is O.K. to directly compare a
		// double to a double.
		double latitude = cursor.getDouble(latIndex);
		assertTrue((double) ROOT_SHOUT_VALUES.getAsDouble(ShoutProviderContract.Shouts.LATITUDE) == latitude);

		double longitude = cursor.getDouble(longIndex);
		assertTrue((double) ROOT_SHOUT_VALUES.getAsDouble(ShoutProviderContract.Shouts.LONGITUDE) == longitude);

		int userId = cursor.getInt(userIndex);
		assertEquals(USER_1_ID, userId);

		assertFalse(cursor.moveToNext());
	}

	@Test
	public void testShoutForeignKeyUserId() {
		COMMENT_SHOUT_VALUES.put(ShoutProviderContract.Shouts.USER_PK, 4);
		try {
			cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, COMMENT_SHOUT_VALUES);
		} catch (SQLException e) {
			return;
		}
		fail("Did not catch exception on non-existant author");
	}

	@Test
	public void testShoutForeignKeyParent() {
		cr.insert(ShoutProviderContract.Users.CONTENT_URI, USER_1_VALUES);
		cr.insert(ShoutProviderContract.Users.CONTENT_URI, USER_2_VALUES);
		cr.insert(ShoutProviderContract.Users.CONTENT_URI, USER_3_VALUES);
		cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, ROOT_SHOUT_VALUES);
		cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, COMMENT_SHOUT_VALUES);
		RECOMMENT_SHOUT_VALUES.put(ShoutProviderContract.Shouts.PARENT,
				TestFactory.generateRandomBase64String(32));
		try {
			cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, RECOMMENT_SHOUT_VALUES);
		} catch (SQLException e) {
			return;
		}
		fail("Did not catch exception on non-existant parent");
	}

	@Test
	public void testInsertDuplicateShoutDoesNothing() {
		cr.insert(ShoutProviderContract.Users.CONTENT_URI, USER_1_VALUES);
		Uri shoutLocation = cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, ROOT_SHOUT_VALUES);
		Uri nextLocation = null;
		try {
			nextLocation = cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, ROOT_SHOUT_VALUES);
		} catch (SQLException e) {
			fail("Inserting a duplicate should not throw an exception");
		}
		assertNotNull(nextLocation);
		assertEquals(shoutLocation, nextLocation);
	}

	@Test
	public void testInsertDuplicateUserDoesNothing() {
		Uri userLocation = cr.insert(ShoutProviderContract.Users.CONTENT_URI, USER_1_VALUES);
		Uri nextLocation = null;
		try {
			nextLocation = cr.insert(ShoutProviderContract.Users.CONTENT_URI, USER_1_VALUES);
		} catch (SQLException e) {
			fail("Inserting a duplicate should not throw an exception");
		}
		assertNotNull(nextLocation);
		assertEquals(userLocation, nextLocation);
	}
}
