
package org.whispercomm.shout.provider;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.Shout;
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
 * <p>
 * Instead of querying the database directly, access the database using this
 * contract class in order to guarantee business logic is sound and reduce
 * coupling to the Content Provider implementation.
 * 
 * @author David Adrian
 */
public class ShoutProviderContract {

	private static final String TAG = ShoutProviderContract.class
			.getSimpleName();

	/**
	 * The authority for the Shout content provider
	 */
	public static final String AUTHORITY = "org.whispercomm.shout.provider";

	/**
	 * The content:// style URI for the Shout provider
	 */
	public static final Uri CONTENT_URI_BASE = Uri.parse("content://"
			+ AUTHORITY);

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
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				CONTENT_URI_BASE, TABLE_NAME);

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
		 * Column name for the senders's timestamp on a Shout. Stored in the
		 * database as a long representing the number of milliseconds since the
		 * UNIX epoch.
		 */
		public static final String TIME_SENT = "Timestamp";

		/**
		 * Column name for the received time on a Shout. Stored in the database
		 * as a long representing the number of milliseconds since the UNIX
		 * epoch.
		 */
		public static final String TIME_RECEIVED = "Time_Received";

		public static final String COMMENT_COUNT = "Comment_Count";

		public static final String RESHOUT_COUNT = "Reshout_Count";

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
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				CONTENT_URI_BASE, TABLE_NAME);

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

	/**
	 * Private interface to make inserting objects into the database easier
	 * 
	 * @author David Adrian
	 */
	private interface DatabaseObject {
		/**
		 * Construct a representation of the DatabaseObject as a ContentValues
		 * object with the necessary key/value pairs.
		 * 
		 * @return ContentValues representation of {@code this}
		 */
		ContentValues makeContentValues();

		/**
		 * @return The URI where this object should be inserted
		 */
		Uri getInsertLocation();

		/**
		 * Save self in database, if not already in the database.
		 * 
		 * @return id of {@code this} in the database.
		 */
		int saveInDatabase();
	}

	private static class DatabaseUser implements DatabaseObject {
		private int id;
		private String username;
		private String key;

		private Context context;

		public DatabaseUser(User user, Context context) {
			username = user.getUsername();
			key = Base64.encodeToString(user.getPublicKey().getEncoded(),
					Base64.DEFAULT);
			id = -1;
			this.context = context;
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
		public int saveInDatabase() {
			if (this.id > 0) {
				return this.id;
			} else {
				this.id = ContractHelper.queryForUser(context, this);
				if (this.id < 1) {
					this.id = ContractHelper.storeInDatabase(context, this);
				}
				return this.id;
			}
		}

	}

	private static class DatabaseShout implements DatabaseObject {
		private int id = -1;
		private int author;
		private int parent;
		private String message;
		private long time;
		private String signature;
		private String hash;
		private Context context;

		public DatabaseShout(Shout shout, int author, int parent,
				Context context) {
			this.message = shout.getMessage();
			this.time = shout.getTimestamp().getMillis();
			this.signature = Base64.encodeToString(shout.getSignature(),
					Base64.DEFAULT);
			this.hash = Base64.encodeToString(shout.getHash(), Base64.DEFAULT);

			this.author = author;
			this.parent = parent;

			this.context = context;
		}

		@Override
		public ContentValues makeContentValues() {
			ContentValues values = new ContentValues();
			values.put(Shouts.AUTHOR, this.author);
			if (this.parent > 0) {
				values.put(Shouts.PARENT, this.parent);
			}
			values.put(Shouts.MESSAGE, this.message);
			values.put(Shouts.TIME_SENT, this.time);
			values.put(Shouts.TIME_RECEIVED, System.currentTimeMillis());
			values.put(Shouts.SIGNATURE, this.signature);
			values.put(Shouts.HASH, this.hash);
			return values;
		}

		@Override
		public Uri getInsertLocation() {
			return Shouts.CONTENT_URI;
		}

		@Override
		public int saveInDatabase() {
			if (this.id > 0) {
				return this.id;
			} else {
				this.id = ContractHelper.queryForShout(context, this);
				if (this.id < 0) {
					this.id = ContractHelper.storeInDatabase(context, this);
					if (this.message != null) {
						ShoutMessage text = new ShoutMessage(this.id,
								this.message);
						text.saveInDatabase();
					}
				}
			}
			return this.id;
		}

		private class ShoutMessage implements DatabaseObject {

			private int rowId = -1;
			private int shoutId;
			private String message;

			public ShoutMessage(int shoutId, String message) {
				this.shoutId = shoutId;
				this.message = message;
			}

			@Override
			public ContentValues makeContentValues() {
				ContentValues values = new ContentValues();
				values.put(ShoutSearchContract.Messages.SHOUT, shoutId);
				values.put(ShoutSearchContract.Messages.MESSAGE, message);
				return values;
			}

			@Override
			public Uri getInsertLocation() {
				return ShoutSearchContract.Messages.CONTENT_URI;
			}

			@Override
			public int saveInDatabase() {
				if (this.rowId < 0) {
					this.rowId = ContractHelper.storeInDatabase(context, this);
				}
				return this.rowId;
			}

		}
	}

	/**
	 * Retrieve Shout with given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if the shout is not in the database
	 */
	public static LocalShout retrieveShoutById(Context context, int id) {
		Uri uri = ContentUris.withAppendedId(Shouts.CONTENT_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor returned on Shout lookup by ID");
			return null;
		}
		LocalShout shout = null;
		if (cursor.moveToFirst()) {
			shout = retrieveShoutFromCursor(context, cursor);
		}
		cursor.close();
		return shout;
	}

	public static LocalShout retrieveShoutFromCursor(Context context, Cursor cursor) {
		int idIndex = cursor.getColumnIndex(Shouts._ID);
		int authorIndex = cursor.getColumnIndex(Shouts.AUTHOR);
		int parentIndex = cursor.getColumnIndex(Shouts.PARENT);
		int messageIndex = cursor.getColumnIndex(Shouts.MESSAGE);
		int hashIndex = cursor.getColumnIndex(Shouts.HASH);
		int sigIndex = cursor.getColumnIndex(Shouts.SIGNATURE);
		int timeIndex = cursor.getColumnIndex(Shouts.TIME_SENT);
		int revcIndex = cursor.getColumnIndex(Shouts.TIME_RECEIVED);
		int commentIndex = cursor.getColumnIndex(Shouts.COMMENT_COUNT);
		int reshoutIndex = cursor.getColumnIndex(Shouts.RESHOUT_COUNT);

		int id = cursor.getInt(idIndex);
		int authorId = cursor.getInt(authorIndex);
		int parentId = cursor.isNull(parentIndex) ? -1 : cursor.getInt(parentIndex);
		String message = cursor.getString(messageIndex);
		String encodedSig = cursor.getString(sigIndex);
		String encodedHash = cursor.getString(hashIndex);
		Long sentTime = cursor.getLong(timeIndex);
		Long receivedTime = cursor.getLong(revcIndex);
		int numComments = cursor.getInt(commentIndex);
		int numReshouts = cursor.getInt(reshoutIndex);
		LocalUser sender = retrieveUserById(context, authorId);
		LocalShout shout = new LocalShoutImpl(context, id, sender, message, encodedSig,
				encodedHash, sentTime, receivedTime, numComments, numReshouts, parentId);
		return shout;
	}

	/**
	 * Stores the given Shout and its sender and parent, if not already present
	 * in the database.
	 * 
	 * @param context The context the content resolver is found in
	 * @param shout The shout to be stored
	 * @return The ID of the Shout in the database, -1 on failure
	 */
	public static int storeShout(Context context, Shout shout) {
		int id = ContractHelper.queryForShout(context, new DatabaseShout(shout,
				-1, -1, context));
		if (id < 0) {
			int parent = -1;
			if (shout.getParent() != null) {
				parent = storeShout(context, shout.getParent());
			}
			int author = storeUser(context, shout.getSender());
			DatabaseShout dbShout = new DatabaseShout(shout, author, parent,
					context);
			id = dbShout.saveInDatabase();
		}
		return id;
	}

	public static LocalShout getReshoutIfExists(Context context, LocalShout parent,
			LocalUser reshouter) {
		String selection = Shouts.PARENT + " = ? AND " + Shouts.AUTHOR + " = ? AND "
				+ Shouts.MESSAGE + " IS NULL";
		String[] selectionArgs = {
				Integer.toString(parent.getDatabaseId()),
				Integer.toString(reshouter.getDatabaseId())
		};
		Cursor cursor = context.getContentResolver().query(Shouts.CONTENT_URI, null, selection,
				selectionArgs, null);
		LocalShout reshout = null;
		if (cursor.moveToFirst()) {
			reshout = ShoutProviderContract.retrieveShoutFromCursor(context, cursor);
		}
		return reshout;
	}

	/**
	 * Retrieve the User with the given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if User is not present in the database
	 */
	public static LocalUser retrieveUserById(Context context, int id) {
		Uri uri = ContentUris.withAppendedId(Users.CONTENT_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor returned on User lookup by ID");
			return null;
		}
		LocalUser user = null;
		if (cursor.moveToFirst()) {
			user = retrieveUserFromCursor(context, cursor);
		}
		cursor.close();
		return user;
	}

	/**
	 * @param context
	 * @param cursor
	 * @return
	 */
	public static LocalUser retrieveUserFromCursor(Context context, Cursor cursor) {
		int idIndex = cursor.getColumnIndex(Users._ID);
		int keyIndex = cursor.getColumnIndex(Users.PUB_KEY);
		int nameIndex = cursor.getColumnIndex(Users.USERNAME);
		int id = cursor.getInt(idIndex);
		String encodedKey = cursor.getString(keyIndex);
		String name = cursor.getString(nameIndex);

		return new LocalUserImpl(context, id, name, encodedKey);
	}

	/**
	 * Stores the given User in the database, if not already present.
	 * 
	 * @param context
	 * @param user
	 * @return The ID of the User in the database or -1 on failure
	 */
	public static int storeUser(Context context, User user) {
		DatabaseUser dbUser = new DatabaseUser(user, context);
		int id = dbUser.saveInDatabase();
		return id;
	}

	public static Cursor getCursorOverAllShouts(Context context) {
		Uri uri = Shouts.CONTENT_URI;
		String selection = Shouts.PARENT + " IS NULL";
		Cursor result = context.getContentResolver().query(uri,
				null, selection, null, null);
		return result;
	}

	public static Cursor getCursorOverShoutComments(Context context, int shoutId) {
		String[] projection = {
				Shouts._ID
		};
		String sortOrder = Shouts.TIME_SENT + " DESC";
		String selection = Shouts.PARENT + " = ? AND " + Shouts.MESSAGE + " IS NOT NULL";
		String[] selectionArgs = {
				Integer.toString(shoutId)
		};
		Cursor result = context.getContentResolver().query(Shouts.CONTENT_URI, projection,
				selection, selectionArgs, sortOrder);
		return result;
	}

	private ShoutProviderContract() {
		// Don't allow this class to be instantiated
	}

	/**
	 * Helper class for storing and retrieving both Shout and User objects
	 * (specifically the DatabaseObject representations of)
	 * 
	 * @author David Adrian
	 */
	private static class ContractHelper {
		/**
		 * Store a DatabaseObject in the database.
		 * 
		 * @param context
		 * @param dbObject
		 * @return id of the DatabaseObject in the database
		 */
		public static int storeInDatabase(Context context,
				DatabaseObject dbObject) {
			ContentValues values = dbObject.makeContentValues();
			Uri table = dbObject.getInsertLocation();
			Uri location = context.getContentResolver().insert(table, values);
			int id = Integer.valueOf(location.getLastPathSegment());
			return id;
		}

		/**
		 * Return the ID of a given DatabaseUser
		 * 
		 * @param context
		 * @param user
		 * @return -1 if not in the database
		 */
		public static int queryForUser(Context context, DatabaseUser user) {
			String[] projection = {
					Users._ID
			};
			String selection = Users.PUB_KEY + " = ? AND " + Users.USERNAME
					+ " = ?";
			String[] selectionArgs = {
					user.key, user.username
			};
			Cursor cursor = context.getContentResolver().query(
					Users.CONTENT_URI, projection, selection, selectionArgs,
					null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on URI " + Users.CONTENT_URI);
				return -1;
			} else if (cursor.moveToNext()) {
				int index = cursor.getColumnIndex(projection[0]);
				int id = cursor.getInt(index);
				cursor.close();
				return id;
			} else {
				cursor.close();
				return -1;
			}
		}

		/**
		 * Return the ID of a given DatabaseShout
		 * 
		 * @param context
		 * @param dbShout
		 * @return -1 on failure
		 */
		public static int queryForShout(Context context, DatabaseShout dbShout) {
			String[] projection = {
					Shouts._ID
			};
			String selection = Shouts.HASH + " = ? AND " + Shouts.SIGNATURE
					+ " = ?";
			String[] selectionArgs = {
					dbShout.hash, dbShout.signature
			};

			Cursor cursor = context.getContentResolver().query(
					Shouts.CONTENT_URI, projection, selection, selectionArgs,
					null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on Shout query");
				return -1;
			} else if (cursor.moveToNext()) {
				int idIndex = cursor.getColumnIndex(Shouts._ID);
				int id = cursor.getInt(idIndex); // Only one column
				cursor.close();
				return id;
			} else {
				cursor.close();
				return -1;
			}
		}

	}
}
