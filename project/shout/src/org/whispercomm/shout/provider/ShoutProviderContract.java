
package org.whispercomm.shout.provider;

import java.security.interfaces.ECPublicKey;

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
	static final String AUTHORITY = "org.whispercomm.shout.provider";

	/**
	 * The content:// style URI for the Shout provider
	 */
	static final Uri CONTENT_URI_BASE = Uri.parse("content://" + AUTHORITY);

	/**
	 * Helper class for managing static variables associated with the Shout
	 * database table
	 * 
	 * @author David Adrian
	 */
	static class Shouts implements BaseColumns {
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
		 * user public key, Base64 encoded.
		 */
		public static final String AUTHOR = "Author";

		/**
		 * Column name for the message in a Shout. Stored as text.
		 */
		public static final String MESSAGE = "Message";

		/**
		 * Column name for the parent Shout. Stored as a reference to another
		 * Shout hash, Base64 encoded.
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

	static class Users implements BaseColumns {
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
	 * Retrieve Shout with given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if the shout is not in the database
	 */
	@Deprecated
	public static LocalShout retrieveShoutById(Context context, int id) {
		Uri uri = ContentUris.withAppendedId(Shouts.CONTENT_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, null, null,
				null, null);
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

	/**
	 * Retrieve a {@link LocalShout} from the database with the given hash.
	 * 
	 * @param context The context of the content resolver
	 * @param hash The hash of the Shout
	 * @return The LocalShout with the given hash
	 */
	public static LocalShout retrieveShoutByHash(Context context, byte[] hash) {
		String encodedHash = Base64.encodeToString(hash, Base64.DEFAULT);
		String selection = Shouts.HASH + " = ?";
		String[] selectionArgs = {
				encodedHash
		};
		Cursor cursor = context.getContentResolver().query(Shouts.CONTENT_URI, null, selection,
				selectionArgs, null);
		LocalShout shout = null;
		if (cursor.moveToFirst()) {
			shout = retrieveShoutFromCursor(context, cursor);
		}
		cursor.close();
		return shout;
	}

	/**
	 * When given a cursor at a position pointing to a Shout with all available
	 * fields, construct a Shout object using the data in that row.
	 * 
	 * @param context
	 * @param cursor
	 * @return {@code null} on failure
	 */
	public static LocalShout retrieveShoutFromCursor(Context context,
			Cursor cursor) {
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
		String encodedAuthor = cursor.getString(authorIndex);
		String encodedParentHash = cursor.isNull(parentIndex) ? null : cursor
				.getString(parentIndex);
		String message = cursor.getString(messageIndex);
		String encodedSig = cursor.getString(sigIndex);
		String encodedHash = cursor.getString(hashIndex);
		Long sentTime = cursor.getLong(timeIndex);
		Long receivedTime = cursor.getLong(revcIndex);
		int numComments = cursor.getInt(commentIndex);
		int numReshouts = cursor.getInt(reshoutIndex);
		LocalUser sender = retrieveUserByEncodedKey(context, encodedAuthor);
		LocalShout shout = new LocalShoutImpl(context, id, sender, message,
				encodedSig, encodedHash, sentTime, receivedTime, numComments,
				numReshouts, encodedParentHash);
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
	@Deprecated
	public static int storeShout(Context context, Shout shout) {
		if (shout.getParent() != null) {
			saveShout(context, shout.getParent());
		}
		saveUser(context, shout.getSender());
		ContentValues values = ContractHelper.buildContentValues(shout);
		Uri location = context.getContentResolver().insert(Shouts.CONTENT_URI, values);
		return Integer.valueOf(location.getLastPathSegment());
	}

	/**
	 * Stores the given Shout and its sender and parent, if not already present
	 * in the database.
	 * 
	 * @param context The context the content resolver is found in
	 * @param shout The shout to be stored
	 * @return the stored shout or {@code null} on failure.
	 */
	public static LocalShout saveShout(Context context, Shout shout) {
		if (shout.getParent() != null) {
			saveShout(context, shout.getParent());
		}
		saveUser(context, shout.getSender());
		ContentValues values = ContractHelper.buildContentValues(shout);
		Uri location = context.getContentResolver().insert(Shouts.CONTENT_URI, values);
		Cursor cursor = context.getContentResolver().query(location, null, null, null, null);
		cursor.moveToFirst();
		LocalShout result = retrieveShoutFromCursor(context, cursor);
		cursor.close();
		return result;
	}

	/**
	 * Retrieve the User with the given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if User is not present in the database
	 */
	@Deprecated
	public static LocalUser retrieveUserById(Context context, int id) {
		Uri uri = ContentUris.withAppendedId(Users.CONTENT_URI, id);
		Cursor cursor = context.getContentResolver().query(uri, null, null,
				null, null);
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
	 * Retrieve a {@link LocalUser} from the database with the given public key.
	 * 
	 * @param context The context the content resolver is found in
	 * @param key {@link ECPublicKey} of the user
	 * @return The LocalUser with the given key, {@code null} if the user does
	 *         not exist
	 */
	public static LocalUser retrieveUserByKey(Context context, ECPublicKey key) {
		byte[] keyBytes = key.getEncoded();
		String encodedKey = Base64.encodeToString(keyBytes, Base64.DEFAULT);
		return retrieveUserByEncodedKey(context, encodedKey);
	}

	/**
	 * Build an {@link LocalUser} object using the data in the current cursor
	 * row.
	 * 
	 * @param context
	 * @param cursor
	 * @return {@code LocalUser} represented by the current row.
	 */
	public static LocalUser retrieveUserFromCursor(Context context,
			Cursor cursor) {
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
	@Deprecated
	public static int storeUser(Context context, User user) {
		ContentValues values = ContractHelper.buildContentValues(user);
		Uri location = context.getContentResolver().insert(Users.CONTENT_URI, values);
		int id = Integer.valueOf(location.getLastPathSegment());
		return id;
	}

	public static LocalUser saveUser(Context context, User user) {
		ContentValues values = ContractHelper.buildContentValues(user);
		context.getContentResolver().insert(Users.CONTENT_URI, values);
		return retrieveUserByKey(context, user.getPublicKey());
	}

	/**
	 * Get a cursor over all original Shouts (i.e. not reshouts or comments).
	 * 
	 * @param context
	 * @return
	 */
	public static Cursor getCursorOverAllShouts(Context context) {
		String sortOrder = Shouts.TIME_RECEIVED + " DESC";
		Uri uri = Shouts.CONTENT_URI;
		String selection = Shouts.PARENT + " IS NULL";
		Cursor result = context.getContentResolver().query(uri, null,
				selection, null, sortOrder);
		return result;
	}
	
	/**
	 * Retrieves a cursor over the comments of the specified shout.
	 * 
	 * @param context the context used to access the content provider
	 * @param hash the hash of the parent whose comments to retrieve
	 * @return a cursor over the comments
	 */
	public static Cursor getComments(Context context, byte[] hash) {
		String sortOrder = Shouts.TIME_RECEIVED + " DESC";
		String selection = Shouts.PARENT + " = ? AND " + Shouts.MESSAGE + " IS NOT NULL";
		String[] selectionArgs = {
				Base64.encodeToString(hash, Base64.DEFAULT)
		};
		Cursor cursor = context.getContentResolver().query(Shouts.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return cursor;
	}

	/**
	 * Retrieves a cursor over the comments of the specified shout.
	 * 
	 * @param context the context used to access the content provider
	 * @param shout the parent whose comments to retrieve
	 * @return a cursor over the comments
	 */
	public static Cursor getComments(Context context, LocalShout shout) {
		return getComments(context, shout.getHash());
	}

	/**
	 * Retrieves a cursor over the comments of the specified shout.
	 * 
	 * @param context the context used to access the content provider
	 * @param shout the parent whose comments to retrieve
	 * @return a cursor over the comments
	 */
	public static Cursor getComments(Context context, Shout shout) {
		// TODO implement this
		throw new RuntimeException("Method not yet implemented.");
	}

	public static Cursor getCursorOverReshouts(Context context, byte[] parentHash) {
		String sortOrder = Shouts.TIME_RECEIVED + " DESC";
		String selection = Shouts.PARENT + " = ? AND " + Shouts.MESSAGE
				+ " IS NULL";
		String[] selectionArgs = {
				Base64.encodeToString(parentHash, Base64.DEFAULT)
		};

		Cursor result = context.getContentResolver().query(Shouts.CONTENT_URI,
				null, selection, selectionArgs, sortOrder);
		return result;
	}

	/**
	 * Retrieves a cursor over the reshouts of the specified shout.
	 * 
	 * @param context the context used to access the content provider
	 * @param shout the parent whose comments to retrieve
	 * @return a cursor over the reshouts
	 */
	public static Cursor getReshouts(Context context, LocalShout shout) {
		return getCursorOverReshouts(context, shout.getHash());
	}

	/**
	 * Retrieves a cursor over the reshouts of the specified shout.
	 * 
	 * @param context the context used to access the content provider
	 * @param shout the parent whose comments to retrieve
	 * @return a cursor over the reshouts
	 */
	public static Cursor getReshouts(Context context, Shout shout) {
		// TODO implement this
		throw new RuntimeException("Method not yet implemented");
	}

	/**
	 * Private default constructor
	 */
	private ShoutProviderContract() {
		throw new IllegalStateException(
				"Cannot instantiate ShoutProviderContract");
	}

	private static LocalUser retrieveUserByEncodedKey(Context context, String encodedKey) {
		String selection = Users.PUB_KEY + " = ?";
		String selectionArgs[] = {
				encodedKey
		};
		Cursor cursor = context.getContentResolver().query(Users.CONTENT_URI, null, selection,
				selectionArgs, null);
		LocalUser user = null;
		if (cursor.moveToFirst()) {
			user = retrieveUserFromCursor(context, cursor);
		}
		cursor.close();
		return user;
	}

	/**
	 * Helper class for storing and retrieving both Shout and User objects
	 * 
	 * @author David Adrian
	 */
	private static class ContractHelper {

		public static ContentValues buildContentValues(User user) {
			ContentValues values = new ContentValues();
			String encodedKey = Base64.encodeToString(user.getPublicKey().getEncoded(),
					Base64.DEFAULT);
			values.put(Users.USERNAME, user.getUsername());
			values.put(Users.PUB_KEY, encodedKey);
			return values;
		}

		public static ContentValues buildContentValues(Shout shout) {
			String encodedHash = Base64.encodeToString(shout.getHash(), Base64.DEFAULT);
			String encodedSig = Base64.encodeToString(shout.getSignature(), Base64.DEFAULT);
			String encodedSender = Base64.encodeToString(shout.getSender().getPublicKey()
					.getEncoded(), Base64.DEFAULT);
			ContentValues values = new ContentValues();
			values.put(Shouts.AUTHOR, encodedSender);
			values.put(Shouts.MESSAGE, shout.getMessage());
			values.put(Shouts.HASH, encodedHash);
			values.put(Shouts.SIGNATURE, encodedSig);
			values.put(Shouts.TIME_SENT, shout.getTimestamp().getMillis());
			values.put(Shouts.TIME_RECEIVED, System.currentTimeMillis());
			if (shout.getParent() != null) {
				String encodedParentHash = Base64.encodeToString(shout.getParent().getHash(),
						Base64.DEFAULT);
				values.put(Shouts.PARENT, encodedParentHash);
			}
			return values;
		}

	}
}
