
package org.whispercomm.shout.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class ShoutProviderTestUtility {
	
	public static Uri insertIntoUserTable(ContentResolver cr, String name, String publicKey) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Users.USERNAME, name);
		values.put(ShoutProviderContract.Users.PUB_KEY, publicKey);
		Uri location = cr.insert(ShoutProviderContract.Users.CONTENT_URI, values);
		return location;
	}

	public static Uri insertIntoShoutTable(ContentResolver cr, int author, int parent, String message, long time,
			String signature, String hash) {
		ContentValues values = new ContentValues();
		values.put(ShoutProviderContract.Shouts.AUTHOR, author);
		if (parent > 0) {
			values.put(ShoutProviderContract.Shouts.PARENT, parent);
		}
		values.put(ShoutProviderContract.Shouts.MESSAGE, message);
		values.put(ShoutProviderContract.Shouts.TIME, time);
		values.put(ShoutProviderContract.Shouts.SIGNATURE, signature);
		values.put(ShoutProviderContract.Shouts.HASH, hash);
		Uri location = cr.insert(ShoutProviderContract.Shouts.CONTENT_URI, values);
		return location;
	}

}
