package org.whispercomm.shout.provider;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
	
	@Before
	public void setUp() {
		userKey = Utility.genByteArray(16);
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Users.USERNAME, NAME);
		values.put(ShoutProviderContract.Users.PUB_KEY, Base64.encodeToString(userKey, Base64.DEFAULT));
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
		values.put(ShoutProviderContract.Shouts.SIGNATURE, Base64.encodeToString(sig, Base64.DEFAULT));
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
}
