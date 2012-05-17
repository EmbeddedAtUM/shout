
package org.whispercomm.shout.provider;

import java.util.List;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.Tag;
import org.whispercomm.shout.User;

import android.content.ContentResolver;
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
	
	private interface DatabaseObject {

		ContentValues makeContentValues();

		Uri getInsertLocation();

		int saveInDatabase(Context context);
	}

	static class DatabaseUser implements DatabaseObject {
		private int id;
		private String username;
		private String key;

		private boolean validId = false;

		public DatabaseUser(User user) {
			username = user.getUsername();
			key = Base64.encodeToString(user.getPublicKey().getEncoded(), Base64.DEFAULT);
			id = -1;
		}

		public DatabaseUser(int id, String username, String key) {
			this.id = id;
			this.username = username;
			this.key = key;
			validId = true;
		}

		@Override
		public ContentValues makeContentValues() {
			ContentValues values = new ContentValues();
			values.put(Users.USERNAME, username);
			values.put(Users.PUB_KEY, key);
			return values;
		}

		@Override
		public Uri getInsertLocation() {
			return Users.CONTENT_URI;
		}

		@Override
		public int saveInDatabase(Context context) {
			if (this.validId) {
				return this.id;
			} else {
				this.id = ContractHelper.queryForUser(context.getContentResolver(), this);
				if (this.id < 1) {
					this.id = ContractHelper.storeInDatabase(context.getContentResolver(), this);
				}
				this.validId = true;
				return this.id;
			}
		}

		public User makeUserImplementation() {
			return new ProviderUser(this.username, this.key);
		}

	}

	static class DatabaseShout implements DatabaseObject {
		private int id = -1;
		private int author;
		private int parent;
		private String message;
		private long time;
		private String signature;
		private String hash;

		boolean hasParent;

		public DatabaseShout(Shout shout, int author, int parent) {
			this.message = shout.getMessage();
			this.time = shout.getTimestamp().getMillis();
			this.signature = Base64.encodeToString(shout.getSignature(), Base64.DEFAULT);
			this.hash = Base64.encodeToString(shout.getHash(), Base64.DEFAULT);

			this.author = author;
			this.parent = parent;

			hasParent = (shout.getParent() != null) ? true : false;
		}

		public DatabaseShout(int id, int author, int parent, String message, long time,
				String signature, String hash, boolean hasParent) {
			this.id = id;
			this.author = author;
			this.parent = parent;
			this.message = message;
			this.time = time;
			this.signature = signature;
			this.hash = hash;

			this.hasParent = hasParent;
		}

		@Override
		public ContentValues makeContentValues() {
			ContentValues values = new ContentValues();
			values.put(Shouts.AUTHOR, this.author);
			values.put(Shouts.PARENT, this.parent);
			values.put(Shouts.MESSAGE, this.message);
			values.put(Shouts.TIME, this.time);
			values.put(Shouts.SIGNATURE, this.signature);
			values.put(Shouts.HASH, this.hash);
			return values;
		}

		@Override
		public Uri getInsertLocation() {
			return Shouts.CONTENT_URI;
		}

		@Override
		public int saveInDatabase(Context context) {
			if (this.id > 0) {
				return this.id;
			} else {
				this.id = ContractHelper.queryForShout(context.getContentResolver(), this);
			}
			return 0;
		}
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
		DatabaseUser dbUser = ContractHelper.queryForUser(context.getContentResolver(), id);
		if (dbUser == null) {
			return null;
		} else {
			return dbUser.makeUserImplementation();
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
		DatabaseUser dbUser = new DatabaseUser(user);
		int id = dbUser.saveInDatabase(context);
		return id;
	}

	public static Tag retrieveTagById(Context context, int id) {
		Uri uri = ContentUris.withAppendedId(Tags.CONTENT_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor on Tag search");
			return null;
		} else if (cursor.moveToNext()) {
			int nameIndex = cursor.getColumnIndex(Tags.TAG);
			String name = cursor.getString(nameIndex);
			Tag tag = new ProviderTag(name);
			return tag;
		} else {
			return null;
		}
	}

	public static List<Tag> retrieveTagsByShoutId(Context context, int id) {
		// TODO Determine how Tags should be linked to Shouts
		return null;
	}

	public static int storeTag(Context context, Tag tag) {
		String name = tag.getName();
		String[] projection = {
				Tags._ID
		};
		String selection = Tags.TAG + " = ?";
		String[] selectionArgs = {
				name
		};
		Cursor cursor = context.getContentResolver().query(Tags.CONTENT_URI, projection, selection,
				selectionArgs, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor on Tag lookup");
			return -1;
		}
		else if (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			return id;
		} else {
			ContentValues values = new ContentValues();
			values.put(Tags.TAG, name);
			Uri at = context.getContentResolver().insert(Tags.CONTENT_URI, values);
			int id = Integer.valueOf(at.getLastPathSegment());
			return id;
		}
	}

	private ShoutProviderContract() {
		// Don't allow this class to be instantiated
	}

	private static class ContractHelper {
		public static int storeInDatabase(ContentResolver cr, DatabaseObject dbObject) {
			ContentValues values = dbObject.makeContentValues();
			Uri table = dbObject.getInsertLocation();
			Uri location = cr.insert(table, values);
			int id = Integer.valueOf(location.getLastPathSegment());
			return id;
		}

		public static int queryForUser(ContentResolver cr, DatabaseUser user) {
			String[] projection = {
					Users._ID
			};
			String selection = Users.PUB_KEY + " = ? AND " + Users.USERNAME + " = ?";
			String[] selectionArgs = {
					user.key,
					user.username
			};
			Cursor cursor = cr.query(Users.CONTENT_URI, projection, selection, selectionArgs, null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on URI " + Users.CONTENT_URI);
				return -1;
			} else if (cursor.moveToNext()) {
				int index = cursor.getColumnIndex(projection[0]);
				int id = cursor.getInt(index);
				cursor.close();
				return id;
			} else {
				return -1;
			}
		}

		public static DatabaseUser queryForUser(ContentResolver cr, int id) {
			Uri uri = ContentUris.withAppendedId(Users.CONTENT_URI, id);
			Cursor cursor = cr.query(uri, null, null, null, null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on URI " + uri);
				return null;
			} else if (cursor.moveToNext()) {
				int idIndex = cursor.getColumnIndex(Users._ID);
				int nameIndex = cursor.getColumnIndex(Users.USERNAME);
				int keyIndex = cursor.getColumnIndex(Users.PUB_KEY);
				return new DatabaseUser(cursor.getInt(idIndex), cursor.getString(nameIndex),
						cursor.getString(keyIndex));
			} else {
				return null;
			}
		}

		public static int queryForShout(ContentResolver cr, DatabaseShout dbShout) {
			String[] projection = {
					Shouts._ID
			};
			String selectionClause = Shouts.HASH + " = ?  AND " + Shouts.SIGNATURE + " = ?";
			String[] selectionArgs = {
					dbShout.hash,
					dbShout.signature
			};

			Cursor cursor = cr.query(Shouts.CONTENT_URI, projection, selectionClause,
					selectionArgs, null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on URI " + Shouts.CONTENT_URI);
				return -1;
			} else if (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				cursor.close();
				return id;
			} else {
				return -1;
			}
		}

	}
}
