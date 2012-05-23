
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
 * <p>
 * Instead of querying the database directly, access the database using this
 * contract class in order to guarantee business logic is sound and reduce
 * coupling to the Content Provider implementation.
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
			key = Base64.encodeToString(user.getPublicKey().getEncoded(), Base64.DEFAULT);
			id = -1;
			this.context = context;
		}

		public DatabaseUser(int id, String username, String key, Context context) {
			this.id = id;
			this.username = username;
			this.key = key;

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

		public User makeUserImplementation() {
			return new ProviderUser(this.username, this.key);
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

		public DatabaseShout(Shout shout, int author, int parent, Context context) {
			this.message = shout.getMessage();
			this.time = shout.getTimestamp().getMillis();
			this.signature = Base64.encodeToString(shout.getSignature(), Base64.DEFAULT);
			this.hash = Base64.encodeToString(shout.getHash(), Base64.DEFAULT);

			this.author = author;
			this.parent = parent;

			this.context = context;
		}

		public DatabaseShout(int id, int author, int parent, String message, long time,
				String signature, String hash, Context context) {
			this.id = id;
			this.author = author;
			this.parent = parent;
			this.message = message;
			this.time = time;
			this.signature = signature;
			this.hash = hash;

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
		public int saveInDatabase() {
			if (this.id > 0) {
				return this.id;
			} else {
				this.id = ContractHelper.queryForShout(context, this);
				if (this.id < 0) {
					this.id = ContractHelper.storeInDatabase(context, this);
				}
			}
			return this.id;
		}

		public Shout makeShoutImplementation() {
			return new ProviderShout(this.author, this.parent, this.message, this.time, this.hash,
					this.signature, this.context);
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
		DatabaseShout dbShout = ContractHelper.queryForShout(context, id);
		if (dbShout == null) {
			return null;
		} else {
			return dbShout.makeShoutImplementation();
		}
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
		int id = ContractHelper.queryForShout(context, new DatabaseShout(shout, -1, -1, context));
		if (id < 0) {
			int parent = -1;
			if (shout.getParent() != null) {
				parent = storeShout(context, shout.getParent());
			}
			int author = storeUser(context, shout.getSender());
			DatabaseShout dbShout = new DatabaseShout(shout, author, parent, context);
			id = dbShout.saveInDatabase();
		}
		return id;
	}

	/**
	 * Retrieve the User with the given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if User is not present in the database
	 */
	public static User retrieveUserById(Context context, int id) {
		DatabaseUser dbUser = ContractHelper.queryForUser(context, id);
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
		DatabaseUser dbUser = new DatabaseUser(user, context);
		int id = dbUser.saveInDatabase();
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
		public static int storeInDatabase(Context context, DatabaseObject dbObject) {
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
			String selection = Users.PUB_KEY + " = ? AND " + Users.USERNAME + " = ?";
			String[] selectionArgs = {
					user.key,
					user.username
			};
			Cursor cursor = context.getContentResolver().query(Users.CONTENT_URI, projection,
					selection, selectionArgs, null);
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

		/**
		 * Return the DatabaseUser representation of the User saved in the
		 * database with the given ID.
		 * 
		 * @param context
		 * @param id
		 * @return null if no User with that ID exists
		 */
		public static DatabaseUser queryForUser(Context context, int id) {
			Uri uri = ContentUris.withAppendedId(Users.CONTENT_URI, id);
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on URI " + uri);
				return null;
			} else if (cursor.moveToNext()) {
				int idIndex = cursor.getColumnIndex(Users._ID);
				int nameIndex = cursor.getColumnIndex(Users.USERNAME);
				int keyIndex = cursor.getColumnIndex(Users.PUB_KEY);
				return new DatabaseUser(cursor.getInt(idIndex), cursor.getString(nameIndex),
						cursor.getString(keyIndex), context);
			} else {
				return null;
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
			String selection = Shouts.HASH + " = ? AND " + Shouts.SIGNATURE + " = ?";
			String[] selectionArgs = {
					dbShout.hash,
					dbShout.signature
			};

			Cursor cursor = context.getContentResolver().query(Shouts.CONTENT_URI, projection,
					selection, selectionArgs, null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on Shout query");
				return -1;
			} else if (cursor.moveToNext()) {
				int idIndex = cursor.getColumnIndex(Shouts._ID);
				int id = cursor.getInt(idIndex); // Only one column
				return id;
			} else {
				return -1;
			}
		}

		/**
		 * Return a DatabaseShout representation of Shout with the given ID.
		 * 
		 * @param context
		 * @param id
		 * @return Null if no Shout with the given ID exists
		 */
		public static DatabaseShout queryForShout(Context context, int id) {
			Uri uri = ContentUris.withAppendedId(Shouts.CONTENT_URI, id);
			Cursor cursor = context.getContentResolver().query(uri, null,
					null,
					null, null);
			if (cursor == null) {
				Log.e(TAG, "Null cursor returned on URI " + Shouts.CONTENT_URI);
				return null;
			} else if (cursor.moveToNext()) {
				int authorIndex = cursor.getColumnIndex(Shouts.AUTHOR);
				int parentIndex = cursor.getColumnIndex(Shouts.PARENT);
				int messageIndex = cursor.getColumnIndex(Shouts.MESSAGE);
				int timeIndex = cursor.getColumnIndex(Shouts.TIME);
				int sigIndex = cursor.getColumnIndex(Shouts.SIGNATURE);
				int hashIndex = cursor.getColumnIndex(Shouts.HASH);

				int author = cursor.getInt(authorIndex);
				int parent = -1;
				if (!cursor.isNull(parentIndex)) {
					parent = cursor.getInt(parentIndex);
				}
				String message = null;
				if (!cursor.isNull(messageIndex)) {
					message = cursor.getString(messageIndex);
				}
				long time = cursor.getLong(timeIndex);
				String encodedHash = cursor.getString(hashIndex);
				String encodedSig = cursor.getString(sigIndex);
				cursor.close();
				return new DatabaseShout(id, author, parent, message, time, encodedSig,
						encodedHash, context);
			} else {
				return null;
			}
		}

	}
}
