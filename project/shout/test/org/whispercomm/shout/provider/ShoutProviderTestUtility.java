
package org.whispercomm.shout.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Base64;

public class ShoutProviderTestUtility {
	
	public static Uri insertIntoUserTable(ContentResolver cr, String name, byte[] publicKey) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Users.USERNAME, name);
		values.put(ShoutProviderContract.Users.PUB_KEY, Base64.encodeToString(publicKey, Base64.DEFAULT));
		Uri location = cr.insert(ShoutProviderContract.Users.CONTENT_URI, values);
		return location;
	}

	public static Uri insertIntoShoutTable(ContentResolver cr, int author, int parent, String message, long time,
			byte[] signature, byte[] hash) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.AUTHOR, author);
		if (parent > 0) {
			values.put(ShoutProviderContract.Shouts.PARENT, parent);
		}
		values.put(ShoutProviderContract.Shouts.MESSAGE, message);
		values.put(ShoutProviderContract.Shouts.TIME, time);
		values.put(ShoutProviderContract.Shouts.SIGNATURE, Base64.encodeToString(signature, Base64.DEFAULT));
		values.put(ShoutProviderContract.Shouts.HASH, Base64.encodeToString(hash, Base64.DEFAULT));
		Uri location = cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
		return location;
	}
}
