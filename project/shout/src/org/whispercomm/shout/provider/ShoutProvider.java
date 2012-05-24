
package org.whispercomm.shout.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ShoutProvider extends ContentProvider {

	private static final String AUTHORITY = ShoutProviderContract.AUTHORITY;

	private static final String MIME_SHOUT = "vnd.android.cursor.item/shout";
	private static final String MIME_SHOUT_MANY = "vnd.android.cursor.dir/shout";
	private static final String MIME_USER = "vnd.android.cursor.item/shout-user";
	private static final String MIME_USER_MANY = "vnd.android.cursor.dir/shout-user";
	private static final String MIME_TAG = "vnd.android.cursor.item/shout-tag";
	private static final String MIME_TAG_MANY = "vnd.android.cursor.dir/shout-tag";

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int SHOUTS = 1;
	private static final int USERS = 2;
	private static final int TAGS = 3;
	private static final int SHOUT_ID = 10;
	private static final int USER_ID = 20;
	private static final int TAG_ID = 30;
	private static final int SHOUTS_USER_ID = 120;

	static {
		sUriMatcher.addURI(AUTHORITY, "shout", SHOUTS);
		sUriMatcher.addURI(AUTHORITY, "user", USERS);
		sUriMatcher.addURI(AUTHORITY, "shout/#", SHOUT_ID);
		sUriMatcher.addURI(AUTHORITY, "user/#", USER_ID);
		sUriMatcher.addURI(AUTHORITY, "shout/user/#", SHOUTS_USER_ID);
		sUriMatcher.addURI(AUTHORITY, "tag", TAGS);
		sUriMatcher.addURI(AUTHORITY, "tag/#", TAG_ID);
	}

	private ShoutDatabaseHelper mOpenHelper;

	private SQLiteDatabase mDB;

	private static final String ENABLE_FK = "PRAGMA foreign_keys = ON";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		mDB = mOpenHelper.getWritableDatabase();
		mDB.execSQL(ENABLE_FK);
		int match = sUriMatcher.match(uri);
		String id, table, whereClause = null;
		String[] whereArgs = null;
		switch (match) {
			case SHOUTS:
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case SHOUT_ID:
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					whereClause = ShoutProviderContract.Shouts._ID + "=" + id;
					whereArgs = null;
				} else {
					whereClause = selection + " and " + ShoutProviderContract.Shouts._ID + "=" + id;
					whereArgs = selectionArgs;
				}
				break;
			case USERS:
				table = ShoutProviderContract.Users.TABLE_NAME;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case USER_ID:
				table = ShoutProviderContract.Users.TABLE_NAME;
				id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					whereClause = ShoutProviderContract.Users._ID + "=" + id;
					whereArgs = null;
				} else {
					whereClause = selection + " and " + ShoutProviderContract.Users._ID + "=" + id;
					whereArgs = selectionArgs;
				}
				break;
			default:
				throw new IllegalArgumentException("Invalid or unknown URI " + uri);
		}
		int rowsAffected = mDB.delete(table, whereClause, whereArgs);
		this.getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
		int match = sUriMatcher.match(uri);
		switch (match) {
			case SHOUTS:
			case SHOUTS_USER_ID:
				return MIME_SHOUT_MANY;
			case TAGS:
				return MIME_TAG_MANY;
			case TAG_ID:
				return MIME_TAG;
			case SHOUT_ID:
				return MIME_SHOUT;
			case USERS:
				return MIME_USER_MANY;
			case USER_ID:
				return MIME_USER;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		mDB = mOpenHelper.getWritableDatabase();
		mDB.execSQL(ENABLE_FK);
		int match = sUriMatcher.match(uri);
		String table = null;
		switch (match) {
			case SHOUTS:
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				break;
			case USERS:
				table = ShoutProviderContract.Users.TABLE_NAME;
				break;
			case TAGS:
				table = ShoutProviderContract.Tags.TABLE_NAME;
				break;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}

		long rowId = mDB.insert(table, null, values);
		if (rowId < 0) { // An error occurred
			// TODO Differentiate errors (UNIQUE vs actual error)
			throw new SQLException("Failed to insert row into " + uri);
		}
		Uri insertLocation = ContentUris.withAppendedId(uri, rowId);
		this.getContext().getContentResolver().notifyChange(insertLocation, null);
		return insertLocation;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new ShoutDatabaseHelper(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		mDB = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = sUriMatcher.match(uri);
		switch (match) {
			case SHOUTS:
				qBuilder.setTables(ShoutProviderContract.Shouts.TABLE_NAME);
				break;
			case SHOUT_ID:
				qBuilder.setTables(ShoutProviderContract.Shouts.TABLE_NAME);
				qBuilder.appendWhere(ShoutProviderContract.Shouts._ID + "="
						+ uri.getLastPathSegment());
				break;
			case SHOUTS_USER_ID:
				qBuilder.setTables(ShoutProviderContract.Shouts.TABLE_NAME);
				qBuilder.appendWhere(ShoutProviderContract.Shouts.AUTHOR + "="
						+ uri.getLastPathSegment());
				break;
			case USERS:
				qBuilder.setTables(ShoutProviderContract.Users.TABLE_NAME);
				break;
			case USER_ID:
				qBuilder.setTables(ShoutProviderContract.Users.TABLE_NAME);
				qBuilder.appendWhere(ShoutProviderContract.Shouts._ID + "="
						+ uri.getLastPathSegment());
				break;
			case TAGS:
				qBuilder.setTables(ShoutProviderContract.Tags.TABLE_NAME);
				break;
			case TAG_ID:
				qBuilder.setTables(ShoutProviderContract.Tags.TABLE_NAME);
				qBuilder.appendWhere(ShoutProviderContract.Tags._ID + "="
						+ uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}

		Cursor resultCursor = qBuilder.query(mDB, projection, selection, selectionArgs, null, null,
				sortOrder);
		resultCursor.setNotificationUri(this.getContext().getContentResolver(), uri);

		return resultCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		mDB = mOpenHelper.getWritableDatabase();
		mDB.execSQL(ENABLE_FK);
		String id, whereClause, table = null;
		String[] whereArgs = null;

		int match = sUriMatcher.match(uri);
		switch (match) {
			case SHOUTS:
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case SHOUT_ID:
				id = uri.getLastPathSegment();
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				if (TextUtils.isEmpty(selection)) {
					whereClause = ShoutProviderContract.Shouts._ID + "=" + id;
					whereArgs = null;
				} else {
					whereClause = selection + " and " + ShoutProviderContract.Shouts._ID + "=" + id;
					whereArgs = selectionArgs;
				}
				break;
			case SHOUTS_USER_ID:
				id = uri.getLastPathSegment();
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				if (TextUtils.isEmpty(selection)) {
					whereClause = ShoutProviderContract.Shouts.AUTHOR + "=" + id;
					whereArgs = null;
				} else {
					whereClause = selection + " and " + ShoutProviderContract.Shouts.AUTHOR + "="
							+ id;
					whereArgs = selectionArgs;
				}
				break;
			case USERS:
				table = ShoutProviderContract.Users.TABLE_NAME;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case USER_ID:
				id = uri.getLastPathSegment();
				table = ShoutProviderContract.Users.TABLE_NAME;
				if (TextUtils.isEmpty(selection)) {
					whereClause = ShoutProviderContract.Users._ID + "=" + id;
					whereArgs = null;
				} else {
					whereClause = selection + " and " + ShoutProviderContract.Users._ID + "=" + id;
					whereArgs = selectionArgs;
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}
		int rowsAffected = mDB.update(table, values, whereClause, whereArgs);
		this.getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	protected static final class ShoutDatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = ShoutDatabaseHelper.class
				.getSimpleName();

		public static final int VERSION = 1;
		public static final String DBNAME = "shout_base";

		private static final String SQL_CREATE_USER = "CREATE TABLE " +
				ShoutProviderContract.Users.TABLE_NAME +
				"(" +
				ShoutProviderContract.Users._ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
				ShoutProviderContract.Users.USERNAME + " TEXT, " +
				ShoutProviderContract.Users.PUB_KEY + " TEXT UNIQUE" +
				");";

		private static final String SQL_CREATE_SHOUT = "CREATE TABLE " +
				ShoutProviderContract.Shouts.TABLE_NAME +
				"(" +
				ShoutProviderContract.Shouts._ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
				ShoutProviderContract.Shouts.AUTHOR + " INTEGER, " +
				ShoutProviderContract.Shouts.PARENT + " INTEGER, " +
				ShoutProviderContract.Shouts.MESSAGE + " TEXT, " +
				ShoutProviderContract.Shouts.TIME + " LONG, " +
				ShoutProviderContract.Shouts.HASH + " TEXT UNIQUE, " +
				ShoutProviderContract.Shouts.SIGNATURE + " TEXT UNIQUE, " +
				"FOREIGN KEY(" + ShoutProviderContract.Shouts.AUTHOR + ") REFERENCES " +
				ShoutProviderContract.Users.TABLE_NAME +
				"(" + ShoutProviderContract.Users._ID + ") " +
				"FOREIGN KEY(" + ShoutProviderContract.Shouts.PARENT + ") REFERENCES " +
				ShoutProviderContract.Shouts.TABLE_NAME +
				"(" + ShoutProviderContract.Shouts._ID + ") " +
				");";

		private static final String SQL_CREATE_TAG = "CREATE TABLE " +
				ShoutProviderContract.Tags.TABLE_NAME +
				"(" +
				ShoutProviderContract.Tags._ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
				ShoutProviderContract.Tags.TAG + " TEXT UNIQUE" +
				");";

		public ShoutDatabaseHelper(Context context) {
			super(context, DBNAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_USER);
			db.execSQL(SQL_CREATE_TAG);
			db.execSQL(SQL_CREATE_SHOUT);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			db.execSQL(ENABLE_FK);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.e(TAG, "Unsupported call to onUpgrade");
			throw new IllegalStateException();
		}

	}

}
