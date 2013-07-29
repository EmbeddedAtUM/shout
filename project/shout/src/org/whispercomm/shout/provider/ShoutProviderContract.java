
package org.whispercomm.shout.provider;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.SimpleLocation;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.KeyGenerator;

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
	 * Helper class for managing static variables associated with the shout
	 * content URI.
	 * 
	 * @author David Adrian
	 */
	public static class Shouts implements BaseColumns {

		/**
		 * Base content URI for the table of Shouts
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				CONTENT_URI_BASE, "shouts");

		/**
		 * Content URI for root shouts
		 */
		public static final Uri ORIGINAL_CONTENT_URI = Uri
				.withAppendedPath(CONTENT_URI_BASE, "shouts/filter/original/");

		/**
		 * Content URI for comments
		 */
		public static final Uri COMMENT_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI_BASE,
				"shouts/filter/comment");

		/**
		 * Content URI for reshouts
		 */
		public static final Uri RESHOUT_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI_BASE,
				"shouts/filter/reshout");

		/**
		 * Column name of the primary key.
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * Column name for the version of the canonical form use for signing the
		 * shout.
		 */
		public static final String VERSION = "Version";

		/**
		 * Column name for the author of a Shout. Stored as a reference to a
		 * user public key, Base64 encoded.
		 */
		public static final String AUTHOR = "Author";

		/**
		 * Column name for the username of the author of the shout.
		 */
		public static final String USERNAME = Users.USERNAME;

		/**
		 * Column name for the public key of the author of the shout.
		 */
		public static final String PUB_KEY = Users.PUB_KEY;

		/**
		 * Column name for the avatar hash of the author of the shout.
		 */
		public static final String AVATAR = Users.AVATAR;

		/**
		 * Column name for the message in a Shout. Stored as text.
		 */
		public static final String MESSAGE = "Message";

		/**
		 * Column name for the longitude in a Shout. Stored as a real.
		 */
		public static final String LONGITUDE = "Longitude";

		/**
		 * Column name for the latitude in a Shout. Stored as a real.
		 */
		public static final String LATITUDE = "Latitude";

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

		/**
		 * Column name for referencing the user primary key column
		 */
		public static final String USER_PK = "User_Key";

		public static final String COMMENT_COUNT = "Comment_Count";

		public static final String RESHOUT_COUNT = "Reshout_Count";

		public static final String RESHOUTER_COUNT = "Reshouter_Count";

	}

	static class Users implements BaseColumns {

		/**
		 * The base content URI for the Users table.
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				CONTENT_URI_BASE, "users");

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

		/**
		 * Hash of the avatar, stored as a Base64 string encoding of the hash
		 * represented as a byte array.
		 */
		public static final String AVATAR = "Avatar";
	}

	/**
	 * Retrieve Shout with given database ID
	 * 
	 * @param context
	 * @param id
	 * @return {@code null} if the shout is not in the database
	 */
	static LocalShout retrieveShoutById(Context context, int id) {
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
	public static LocalShout retrieveShoutByHash(Context context, Hash hash) {
		String encodedHash = Base64.encodeToString(hash.toByteArray(), Base64.DEFAULT);
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
		int versionIndex = cursor.getColumnIndex(Shouts.VERSION);
		int parentIndex = cursor.getColumnIndex(Shouts.PARENT);
		int usernameIndex = cursor.getColumnIndex(Shouts.USERNAME);
		int pubkeyIndex = cursor.getColumnIndex(Shouts.PUB_KEY);
		int avatarIndex = cursor.getColumnIndex(Shouts.AVATAR);
		int messageIndex = cursor.getColumnIndex(Shouts.MESSAGE);
		int longitudeIndex = cursor.getColumnIndex(Shouts.LONGITUDE);
		int latitudeIndex = cursor.getColumnIndex(Shouts.LATITUDE);
		int hashIndex = cursor.getColumnIndex(Shouts.HASH);
		int sigIndex = cursor.getColumnIndex(Shouts.SIGNATURE);
		int timeIndex = cursor.getColumnIndex(Shouts.TIME_SENT);
		int revcIndex = cursor.getColumnIndex(Shouts.TIME_RECEIVED);
		int commentIndex = cursor.getColumnIndex(Shouts.COMMENT_COUNT);
		int reshoutIndex = cursor.getColumnIndex(Shouts.RESHOUT_COUNT);
		int reshouterIndex = cursor.getColumnIndex(Shouts.RESHOUTER_COUNT);

		int version = cursor.getInt(versionIndex);
		String encodedParentHash = cursor.isNull(parentIndex) ? null : cursor
				.getString(parentIndex);
		String message = cursor.getString(messageIndex);
		Double longitude = null;
		Double latitude = null;
		/*
		 * -1 check is for unit tests. RoboEletric isn't aware of columns with
		 * all null values, but returns -1 as the index instead.
		 */
		if (longitudeIndex >= 0 && latitudeIndex >= 0) {
			if (!cursor.isNull(longitudeIndex)
					&& !cursor.isNull(latitudeIndex)) {
				longitude = cursor.getDouble(longitudeIndex);
				latitude = cursor.getDouble(latitudeIndex);
			}
		}
		String encodedSig = cursor.getString(sigIndex);
		String encodedHash = cursor.getString(hashIndex);
		Long sentTime = cursor.getLong(timeIndex);
		Long receivedTime = cursor.getLong(revcIndex);
		int numComments = cursor.getInt(commentIndex);
		int numReshouts = cursor.getInt(reshoutIndex);
		int numReshouters = cursor.getInt(reshouterIndex);

		Location location = null;
		if (longitude != null && latitude != null) {
			location = new SimpleLocation(longitude, latitude);
		}

		String encodedKey = cursor.getString(pubkeyIndex);
		String name = cursor.getString(usernameIndex);
		String encodedAvatarHash = cursor.getString(avatarIndex);

		LocalUser sender = new LocalUserImpl(name, encodedKey, encodedAvatarHash);
		LocalShout shout = new LocalShoutImpl(context, version, sender, message, location,
				encodedSig, encodedHash, sentTime, receivedTime, numComments,
				numReshouts, numReshouters, encodedParentHash);
		return shout;
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
		int authorId = saveUserAndReturnId(context, shout.getSender());
		ContentValues values = ContractHelper.buildContentValues(shout, authorId);
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
	static LocalUser retrieveUserById(Context context, int id) {
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
	public static LocalUser retrieveUserByTuple(Context context, ECPublicKey key, String username) {
		String encodedKey = Base64.encodeToString(KeyGenerator.encodePublic(key), Base64.DEFAULT);
		return retrieveUserByEncodedTuple(context, encodedKey, username);
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
		int keyIndex = cursor.getColumnIndex(Users.PUB_KEY);
		int nameIndex = cursor.getColumnIndex(Users.USERNAME);
		int avatarIndex = cursor.getColumnIndex(Users.AVATAR);
		String encodedKey = cursor.getString(keyIndex);
		String name = cursor.getString(nameIndex);
		String encodedAvatarHash = cursor.getString(avatarIndex);
		return new LocalUserImpl(name, encodedKey, encodedAvatarHash);
	}

	public static LocalUser saveUser(Context context, User user) {
		int userId = saveUserAndReturnId(context, user);
		return retrieveUserById(context, userId);
	}

	/**
	 * Get a cursor over all original Shouts (i.e. not reshouts or comments).
	 * 
	 * @param context
	 * @return
	 */
	public static Cursor getCursorOverAllShouts(Context context, SortOrder sort) {
		Uri uri = Shouts.ORIGINAL_CONTENT_URI;
		Cursor result = context.getContentResolver().query(uri, null,
				null, null, sort.sql());
		return result;
	}

	/**
	 * Retrieves a cursor over the comments of the specified shout.
	 * 
	 * @param context the context used to access the content provider
	 * @param hash the hash of the parent whose comments to retrieve
	 * @return a cursor over the comments
	 */
	public static Cursor getComments(Context context, Hash hash) {
		String sortOrder = Shouts.TIME_RECEIVED + " ASC";
		String selection = Shouts.PARENT + " = ?";
		String[] selectionArgs = {
				Base64.encodeToString(hash.toByteArray(), Base64.DEFAULT)
		};
		Cursor cursor = context.getContentResolver().query(Shouts.COMMENT_CONTENT_URI, null,
				selection,
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
	public static Cursor getComments(Context context, Shout shout) {
		return getComments(context, shout.getHash());

	}

	public static Cursor getCursorOverReshouts(Context context, Hash parentHash) {
		String sortOrder = Shouts.TIME_RECEIVED + " DESC";
		String selection = Shouts.PARENT + " = ?";
		String[] selectionArgs = {
				Base64.encodeToString(parentHash.toByteArray(), Base64.DEFAULT)
		};

		Cursor result = context.getContentResolver().query(Shouts.RESHOUT_CONTENT_URI,
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
	public static Cursor getReshouts(Context context, Shout shout) {
		return getCursorOverReshouts(context, shout.getHash());
	}

	/**
	 * Private default constructor
	 */
	private ShoutProviderContract() {
		throw new IllegalStateException(
				"Cannot instantiate ShoutProviderContract");
	}

	/**
	 * Helper method for retrieving a user by a Base64 encoded public key
	 * 
	 * @param context
	 * @param encodedKey
	 * @return {@code null} if the user does not exist
	 */
	private static LocalUser retrieveUserByEncodedTuple(Context context, String encodedKey,
			String username) {
		String selection = Users.PUB_KEY + " = ? AND " + Users.USERNAME + " = ?";
		String selectionArgs[] = {
				encodedKey,
				username
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
	 * Private helper method for getting the database ID of a User object. Saves
	 * the user in the database if it is not already in the database.
	 * 
	 * @param context
	 * @param user
	 * @return
	 */
	private static int saveUserAndReturnId(Context context, User user) {
		ContentValues values = ContractHelper.buildContentValues(user);
		Uri location = context.getContentResolver().insert(Users.CONTENT_URI, values);
		int id = (int) ContentUris.parseId(location);
		return id;
	}

	/**
	 * Enum specified the sorting order for cursors over shouts.
	 */
	public static enum SortOrder {

		/**
		 * Sort by most recently received first.
		 */
		ReceivedTimeDescending(Shouts.TIME_RECEIVED + " DESC"),
		/**
		 * Sort by most recently sent first.
		 */
		SentTimeDescending(
				Shouts.TIME_SENT + " DESC");

		private String sql;

		private SortOrder(String sql) {
			this.sql = sql;
		}

		private String sql() {
			return this.sql;
		}

	}

	/**
	 * Helper class for storing and retrieving both Shout and User objects
	 * 
	 * @author David Adrian
	 */
	private static class ContractHelper {

		public static ContentValues buildContentValues(User user) {
			ContentValues values = new ContentValues();
			String encodedKey = Base64.encodeToString(
					KeyGenerator.encodePublic(user.getPublicKey()),
					Base64.DEFAULT);
			String encodedAvatarHash = Base64.encodeToString(user.getAvatar().getHash()
					.toByteArray(), Base64.DEFAULT);
			values.put(Users.USERNAME, user.getUsername());
			values.put(Users.PUB_KEY, encodedKey);
			values.put(Users.AVATAR, encodedAvatarHash);
			return values;
		}

		public static ContentValues buildContentValues(Shout shout, int authorId) {
			String encodedHash = Base64.encodeToString(shout.getHash().toByteArray(),
					Base64.DEFAULT);
			String encodedSig = Base64.encodeToString(DsaSignature.encode(shout.getSignature()),
					Base64.DEFAULT);
			String encodedSender = Base64.encodeToString(
					KeyGenerator.encodePublic(shout.getSender().getPublicKey()), Base64.DEFAULT);
			ContentValues values = new ContentValues();
			values.put(Shouts.VERSION, shout.getVersion());
			values.put(Shouts.AUTHOR, encodedSender);
			values.put(Shouts.MESSAGE, shout.getMessage());
			if (shout.getLocation() != null) {
				values.put(Shouts.LONGITUDE, shout.getLocation().getLongitude());
				values.put(Shouts.LATITUDE, shout.getLocation().getLatitude());
			}
			values.put(Shouts.HASH, encodedHash);
			values.put(Shouts.SIGNATURE, encodedSig);
			values.put(Shouts.TIME_SENT, shout.getTimestamp().getMillis());
			values.put(Shouts.TIME_RECEIVED, System.currentTimeMillis());
			if (shout.getParent() != null) {
				String encodedParentHash = Base64.encodeToString(shout.getParent().getHash()
						.toByteArray(),
						Base64.DEFAULT);
				values.put(Shouts.PARENT, encodedParentHash);
			}
			values.put(Shouts.USER_PK, authorId);
			return values;
		}

	}
}
