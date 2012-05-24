
package org.whispercomm.shout.provider;

import java.util.List;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.Tag;
import org.whispercomm.shout.User;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Base64;
import android.util.Log;

/**
 * A provider of shouts and shout users seen by this device, to be used for user
 * interface generation and authentication of senders.
 * 
 * @author David Adrian
 */
public class ShoutProviderContract {
	// TODO Move functions into related inner class for each table

	private static final String TAG = ShoutProviderContract.class.getSimpleName();

	/**
	 * The authority for the Shout content provider
	 */
	public static final String AUTHORITY = "org.whispercomm.shout.provider";

	/**
	 * The content:// style URI for the Shout provider
	 */
	public static final Uri CONTENT_URI_BASE = Uri.parse("content://" + AUTHORITY);

	/**
	 * Helper class for managing static variables associated with the Shout
	 * database table
	 * 
	 * @author David Adrian
	 */
	public static class Shouts implements BaseColumns {
		/**
		 * SQLite table name. Not needed for managed queries, and can be ignored
		 * in most use cases
		 */
		public static final String TABLE_NAME = "shout";

		/**
		 * Base content URI for the table of Shouts
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_URI_BASE, TABLE_NAME);

		/**
		 * Column name of the primary key. This represents the database ID of a
		 * Shout, which can be used in the {@code Shouts.PARENT} field to
		 * reference another Shout in the database.
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * Column name for the author of a Shout. Stored as a reference to a
		 * user.
		 */
		public static final String AUTHOR = "Author";

		/**
		 * Column name for the message in a Shout. Stored as text.
		 */
		public static final String MESSAGE = "Message";

		/**
		 * Column name for the parent Shout. Stored as a reference to another
		 * Shout in the database.
		 */
		public static final String PARENT = "Parent";

		/**
		 * Column name for the signature on a Shout. Stored as default Base64
		 * string encoding of a byte array.
		 */
		public static final String SIGNATURE = "Signature";

		/**
		 * Column name for the hash of a Shout. Stored as default Base64 string
		 * encoding of a byte array.
		 */
		public static final String HASH = "Hash";

		/**
		 * Column name for the timestamp on a Shout. Stored in the database as a
		 * long representing the number of milliseconds since the UNIX epoch.
		 */
		public static final String TIME = "Timestamp";

	}

	public static class Users implements BaseColumns {
		/**
		 * The SQLite table name for the Users table. Not needed for managed
		 * queries, and can be ignored in most use cases.
		 */
		public static final String TABLE_NAME = "user";

		/**
		 * The base content URI for the Users table.
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_URI_BASE, TABLE_NAME);

		/**
		 * The column name for the username associated with a User. Stored as
		 * text.
		 */
		public static final String USERNAME = "Name";

		/**
		 * The column name for a User's public key. Stored as a Base64 string
		 * encoding of the key represented as a byte array.
		 */
		public static final String PUB_KEY = "Key";
	}

	public static class Tags implements BaseColumns {
		/**
		 * The SQLite table name for the Tags table. Not needed for managed
		 * queries, and can be ignored in most cases.
		 */
		public static final String TABLE_NAME = "tag";

		/**
		 * The base content URI for the Tags table.
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_URI_BASE, TABLE_NAME);

		/**
		 * Column name of the primary key. This represent the database ID of a
		 * specific tag, which is used to assign a Tag to a Shout in the
		 * database.
		 */
		public static final String _ID = BaseColumns._ID;
		
		/**
		 * Column name for the tag itself. Does not store the leading #.
		 */
		public static final String TAG = "Name";

	}

	/**
	 * Retrieve Shout with given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if the shout is not in the database
	 */
	public static Shout retrieveShoutById(Context context, int id) {
		// TODO Find all calls to this method, check to make sure a non-null
		// context is being passed.
		Uri uri = ContentUris.withAppendedId(Shouts.CONTENT_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor returned on URI " + uri);
			return null;
		} else if (!cursor.moveToFirst()) {
			Log.d(TAG, "No results returned on URI " + uri);
			cursor.close();
			return null;
		} else {
			int userColumn = cursor.getColumnIndex(Shouts.AUTHOR);
			int messageColumn = cursor.getColumnIndex(Shouts.MESSAGE);
			int parentColumn = cursor.getColumnIndex(Shouts.PARENT);
			int timeColumn = cursor.getColumnIndex(Shouts.TIME);
			int signatureColumn = cursor.getColumnIndex(Shouts.SIGNATURE);
			int hashColumn = cursor.getColumnIndex(Shouts.HASH);

			int userId = cursor.getInt(userColumn);

			long time = cursor.getLong(timeColumn);

			String content = null;
			if (!cursor.isNull(messageColumn)) {
				content = cursor.getString(messageColumn);
			}

			int parentId = -1;
			if (!cursor.isNull(parentColumn)) {
				parentId = cursor.getInt(parentColumn);
			}

			String encodedSignature = cursor.getString(signatureColumn);
			String encodedHash = cursor.getString(hashColumn);

			cursor.close();

			return new ProviderShout(userId, parentId, content, time, encodedHash,
					encodedSignature, context);
		}
	};

	/**
	 * Stores the given Shout and its sender and parent, if not already present
	 * in the database.
	 * 
	 * @param context The context the content resolver is found in
	 * @param shout The shout to be stored
	 * @return The ID of the Shout in the database, -1 on failure
	 */
	public static int storeShout(Context context, Shout shout) {
		String encodedHash = Base64.encodeToString(shout.getHash(), Base64.DEFAULT);
		String encodedSignature = Base64.encodeToString(shout.getSignature(), Base64.DEFAULT);

		User sender = shout.getSender();
		int author = storeUser(context, sender);
		if (author < 0) {
			Log.e(TAG, "Failed to insert or find user for Shout " + encodedHash);
		}

		String[] projection = {
				Shouts._ID
		};
		String selectionClause = Shouts.HASH + " = ?  and " + Shouts.SIGNATURE + " = ?";
		String[] selectionArgs = {
				encodedHash,
				encodedSignature
		};

		Cursor cursor = context.getContentResolver().query(Shouts.CONTENT_URI, projection,
				selectionClause, selectionArgs, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor on Shout search");
			return -1;
		} else if (cursor.moveToNext()) {
			int index = cursor.getColumnIndex(Shouts._ID);
			int shoutId = cursor.getInt(index);
			cursor.close();
			return shoutId; // Shout already in the database
		} else {
			cursor.close();
			ContentValues values = new ContentValues();
			Shout parent = shout.getParent();
			if (parent != null) {
				int parentId = storeShout(context, parent);
				if (parentId > 0) {
					values.put(Shouts.AUTHOR, author);
					values.put(Shouts.MESSAGE, shout.getMessage());
					values.put(Shouts.TIME, shout.getTimestamp().getMillis());
					values.put(Shouts.HASH, encodedHash);
					values.put(Shouts.SIGNATURE, encodedSignature);
					values.put(Shouts.PARENT, parentId);
				} else {
					Log.e(TAG, "Failed to insert or find parent of Shout " + encodedHash);
					return -1;
				}
			}
			Uri location = context.getContentResolver().insert(Shouts.CONTENT_URI, values);
			return Integer.valueOf(location.getLastPathSegment());
		}
	}

	/**
	 * Retrieve the User with the given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if User is not present in the database
	 */
	public static User retrieveUserById(Context context, int id) {
		Uri uri = ContentUris.withAppendedId(Users.CONTENT_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor returned on URI " + uri);
			return null;
		} else if (!cursor.moveToFirst()) {
			Log.d(TAG, "No results returned on URI " + uri);
			cursor.close();
			return null;
		} else {
			int nameColumn = cursor.getColumnIndex(Users.USERNAME);
			int keyColumn = cursor.getColumnIndex(Users.PUB_KEY);

			String username = cursor.getString(nameColumn);
			String encodedKey = cursor.getString(keyColumn);
			cursor.close();
			return new ProviderUser(username, encodedKey);
		}
	}

	/**
	 * Stores the given User in the database, if not already present.
	 * 
	 * @param context
	 * @param user
	 * @return The ID of the User in the database or -1 on failure
	 */
	public static int storeUser(Context context, User user) {
		String username = user.getUsername();
		byte[] key = user.getPublicKey().getEncoded();
		String encodedKey = Base64.encodeToString(key, Base64.DEFAULT);

		String[] projection = {
				Users._ID
		};

		String selection = Users.PUB_KEY + " = ? AND " + Users.USERNAME + " = ?";
		String[] selectionArgs = {
				encodedKey,
				username
		};

		Cursor cursor = context.getContentResolver().query(Users.CONTENT_URI, projection,
				selection, selectionArgs, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor on User search");
			return -1;
		} else if (cursor.moveToNext()) {
			Log.v(TAG, "Found user in database");
			int index = cursor.getColumnIndex(Users._ID);
			int userId = cursor.getInt(index);
			cursor.close();
			return userId; // User already in the database
		} else {
			cursor.close();
			Log.v(TAG, "Did not find user in database: " + username + " " + encodedKey);
			ContentValues values = new ContentValues();
			values.put(Users.USERNAME, username);
			values.put(Users.PUB_KEY, encodedKey);
			Uri location = context.getContentResolver().insert(Users.CONTENT_URI, values);
			return Integer.valueOf(location.getLastPathSegment());
		}
	}

	// TODO Support for Tags
	public static Tag retrieveTagById(Context context, long id) {
		return null;
	}

	public static List<Tag> retrieveTagsByShoutId(Context context, long id) {
		return null;
	}

	public static long storeTag(Context context, Tag tag) {
		return -1;
	}

	private ShoutProviderContract() {
		// Don't allow this class to be instantiated
	}
}
