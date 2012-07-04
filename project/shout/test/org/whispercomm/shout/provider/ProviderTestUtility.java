
package org.whispercomm.shout.provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.whispercomm.shout.test.util.TestFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Base64;

public class ProviderTestUtility {

	public static Uri insertIntoUserTable(ContentResolver cr, String name, byte[] publicKey) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Users.USERNAME, name);
		values.put(ShoutProviderContract.Users.PUB_KEY,
				Base64.encodeToString(publicKey, Base64.DEFAULT));
		Uri location = cr.insert(ShoutProviderContract.Users.CONTENT_URI, values);
		return location;
	}

	public static Uri insertIntoShoutTable(ContentResolver cr, int author, int parent,
			String message, long time,
			byte[] signature, byte[] hash) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.AUTHOR, author);
		if (parent > 0) {
			values.put(ShoutProviderContract.Shouts.PARENT, parent);
		}
		values.put(ShoutProviderContract.Shouts.MESSAGE, message);
		values.put(ShoutProviderContract.Shouts.TIME_SENT, time);
		values.put(ShoutProviderContract.Shouts.SIGNATURE,
				Base64.encodeToString(signature, Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.HASH, Base64.encodeToString(hash, Base64.DEFAULT));
		Uri location = cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
		return location;
	}

	public static void insertFourUsers(ContentResolver cr, String[] usernames) {
		byte[][] keys = new byte[usernames.length][];
		for (int i = 0; i < usernames.length; i++) {
			keys[i] = TestFactory.genByteArray(32);
			Uri at = ProviderTestUtility.insertIntoUserTable(cr, usernames[i], keys[i]);
			assertNotNull(at);
			assertTrue(Integer.valueOf(at.getLastPathSegment()) > 0);
		}
	}

	public static void insertFourShouts(ContentResolver cr, String[] messages, int[] parents) {
		int[] authors = {
				1, 1, 2, 2
		};
		long[] times = {
				100, 200, 300, 400
		};
		byte[][] hashes = new byte[messages.length][];
		byte[][] sigs = new byte[messages.length][];
		for (int i = 0; i < messages.length; i++) {
			hashes[i] = TestFactory.genByteArray(10);
			sigs[i] = TestFactory.genByteArray(32);
			Uri at = ProviderTestUtility.insertIntoShoutTable(cr, authors[i], parents[i],
					messages[i], times[i], sigs[i], hashes[i]);
			assertNotNull(at);
			assertTrue(Integer.valueOf(at.getLastPathSegment()) > 0);
		}
	}
}
