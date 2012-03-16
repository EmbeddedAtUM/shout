package org.whispercomm.shout.provider;

/**
 * Content provider for storing Shout messages
 */
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class ShoutProvider extends ContentProvider {

	private static final String TAG = ShoutProvider.class.getName();
	private static final String AUTHORITY = TAG;

	private static final String URI_ERROR = "Error: Invalid URI ";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int SHOUTS = 0;
	private static final int SHOUTS_ID = 1;
	private static final int SHOUTS_USER_ID = 2;
	private static final int SHOUTS_TAG_ID = 3;
	private static final int TAGS = 4;
	private static final int TAGS_ID = 5;
	private static final int USERS = 6;
	private static final int USERS_ID = 7;

	// These are the basic query strings. Note that none of them are semicolon
	// delimited within the string.
	private static final String SQL_QUERY_SHOUTS = "SELECT S._ID, S.User_ID, S.Content, S.Date, U.Username, U.Public_Key "
			+ "FROM Shout AS S "
			+ "JOIN `User` AS U "
			+ "ON S.User_ID = U.User_ID;";
	private static final String SQL_QUERY_SHOUTS_ID = "SELECT S._ID, S.User_ID, S.Content, S.Date, U.Username, U.Public_Key "
			+ "FROM Shout AS S "
			+ "JOIN `User` AS U "
			+ "ON S.User_ID = U.User_ID " + "WHERE S.Shout_ID = ?;";
	private static final String SQL_QUERY_SHOUTS_USER_ID = "SELECT S._ID, S.User_ID, S.Content, S.Date, U.Username, U.Public_Key "
			+ "FROM Shout AS S "
			+ "JOIN `User` AS U "
			+ "ON S.User_ID = U.User_ID " + "WHERE S.User_ID = ?;";
	private static final String SQL_QUERY_SHOUTS_TAG_ID = "SELECT S._ID, S.User_ID, S.Content, S.Date, U.Username, U.Public_Key "
			+ "FROM Tag_Assignment AS TA "
			+ "JOIN Shout AS S "
			+ "ON S._ID = TA.Shout_ID "
			+ "JOIN `User` as U "
			+ "ON S.User_ID = U._ID " + "WHERE TA.Tag_ID = ?";
	private static final String SQL_QUERY_TAGS = "SELECT T._ID, T.Name FROM Tag AS T;";
	private static final String SQL_QUERY_TAGS_ID = "SELECT T._ID, T.Name FROM Tag AS T "
			+ " WHERE T._ID = ?;";
	private static final String SQL_QUERY_USERS = "SELECT U._ID, U.Username, U.Public_Key FROM User AS U;";
	private static final String SQL_QUERY_USERS_ID = "SELECT U._ID, U.Username, U.Public_Key FROM User AS U "
			+ " WHERE U._ID = ?;";

	/**
	 * Initializer block to set URIs in the UriMatcher
	 */
	static {
		sURIMatcher.addURI(AUTHORITY, "shouts", SHOUTS);
		sURIMatcher.addURI(AUTHORITY, "shouts/#", SHOUTS_ID);
		sURIMatcher.addURI(AUTHORITY, "shouts/user/#", SHOUTS_USER_ID);
		sURIMatcher.addURI(AUTHORITY, "shouts/tag/#", SHOUTS_TAG_ID);
		sURIMatcher.addURI(AUTHORITY, "tags", TAGS);
		sURIMatcher.addURI(AUTHORITY, "tags/#", TAGS_ID);
		sURIMatcher.addURI(AUTHORITY, "users", USERS);
		sURIMatcher.addURI(AUTHORITY, "users/#", USERS_ID);
	}

	private SQLiteDatabase mDB;
	private ShoutProviderDatabaseHelper mDBHelper;

	@Override
	public boolean onCreate() {
		Log.v(TAG, "onCreate() called");
		mDBHelper = new ShoutProviderDatabaseHelper(super.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		mDB = mDBHelper.getReadableDatabase();
		int match = sURIMatcher.match(uri);
		String query = null;
		String[] condition = { uri.getLastPathSegment() };
		switch (match) {
		case SHOUTS:
			query = SQL_QUERY_SHOUTS;
			break;
		case SHOUTS_ID:
			query = SQL_QUERY_SHOUTS_ID;
			break;
		case SHOUTS_USER_ID:
			query = SQL_QUERY_SHOUTS_USER_ID;
			break;
		case SHOUTS_TAG_ID:
			query = SQL_QUERY_SHOUTS_TAG_ID;
			break;
		case TAGS:
			query = SQL_QUERY_TAGS;
			break;
		case TAGS_ID:
			query = SQL_QUERY_TAGS_ID;
			break;
		case USERS:
			query = SQL_QUERY_USERS;
			break;
		case USERS_ID:
			query = SQL_QUERY_USERS_ID;
			break;
		default:
			throw new IllegalArgumentException(URI_ERROR + uri);
		}
		return mDB.rawQuery(query, condition);
	}

	@Override
	public String getType(Uri uri) {
		int match = sURIMatcher.match(uri);
		switch (match) {
		case SHOUTS:
			break;
		case SHOUTS_ID:
			break;
		case SHOUTS_USER_ID:
			break;
		case SHOUTS_TAG_ID:
			break;
		case TAGS:
			break;
		case TAGS_ID:
			break;
		case USERS:
			break;
		case USERS_ID:
			break;
		default:
			throw new IllegalArgumentException(URI_ERROR + uri);
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		mDB = mDBHelper.getWritableDatabase();
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		mDB = mDBHelper.getWritableDatabase();
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		mDB = mDBHelper.getWritableDatabase();
		return 0;
	}
}
