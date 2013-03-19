
package org.whispercomm.shout.provider;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Content provider to store Shouts and Users seen by this device.
 * 
 * @author David Adrian
 */
public class ShoutProvider extends ContentProvider {
	private static final String TAG = ShoutProvider.class.getSimpleName();

	private static final String AUTHORITY = ShoutProviderContract.AUTHORITY;

	private static final String MIME_SHOUT = "vnd.android.cursor.item/shout";
	private static final String MIME_SHOUT_MANY = "vnd.android.cursor.dir/shout";
	private static final String MIME_USER = "vnd.android.cursor.item/shout-user";
	private static final String MIME_USER_MANY = "vnd.android.cursor.dir/shout-user";

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int SHOUTS = 1;
	private static final int USERS = 2;
	private static final int MESSAGES = 4;
	private static final int ROOTS = 5;
	private static final int COMMENTS = 6;
	private static final int RESHOUTS = 7;
	private static final int SHOUT_ID = 10;
	private static final int USER_ID = 20;
	private static final int MESSAGE_ID = 40;
	private static final int SHOUTS_USER_ID = 120;
	private static final int MESSAGES_SHOUT_ID = 410;

	static {
		sUriMatcher.addURI(AUTHORITY, "shout", SHOUTS);
		sUriMatcher.addURI(AUTHORITY, "user", USERS);
		sUriMatcher.addURI(AUTHORITY, "root", ROOTS);
		sUriMatcher.addURI(AUTHORITY, "comment", COMMENTS);
		sUriMatcher.addURI(AUTHORITY, "reshout", RESHOUTS);
		sUriMatcher.addURI(AUTHORITY, "shout/#", SHOUT_ID);
		sUriMatcher.addURI(AUTHORITY, "user/#", USER_ID);
		sUriMatcher.addURI(AUTHORITY, "shout/user/#", SHOUTS_USER_ID);

		sUriMatcher.addURI(AUTHORITY, "message", MESSAGES);
		sUriMatcher.addURI(AUTHORITY, "message/#", MESSAGE_ID);
		sUriMatcher.addURI(AUTHORITY, "message/shout/#", MESSAGES_SHOUT_ID);
	}

	private ShoutDatabaseHelper mOpenHelper;

	private SQLiteDatabase mDB;

	private static final String ENABLE_FK = "PRAGMA foreign_keys = ON";

	private void notifyChange(Uri uri) {
		List<Uri> uris = new ArrayList<Uri>();

		uris.add(uri);

		// Add other URIs that also match
		int match = sUriMatcher.match(uri);
		switch (match) {
			case SHOUT_ID:
				uris.add(ShoutProviderContract.Shouts.CONTENT_URI);
			case SHOUTS:
				uris.add(ShoutProviderContract.Shouts.ROOT_CONTENT_URI);
				uris.add(ShoutProviderContract.Shouts.COMMENT_CONTENT_URI);
				uris.add(ShoutProviderContract.Shouts.RESHOUT_CONTENT_URI);
				break;
			case ROOTS:
				uris.add(ShoutProviderContract.Shouts.CONTENT_URI);
				break;
			case COMMENTS:
				uris.add(ShoutProviderContract.Shouts.CONTENT_URI);
				break;
			case RESHOUTS:
				uris.add(ShoutProviderContract.Shouts.CONTENT_URI);
				break;
		}

		for (Uri u : uris) {
			this.getContext().getContentResolver()
					.notifyChange(u, null);
		}
	}

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
			case ROOTS:
			case COMMENTS:
			case RESHOUTS:
				/* Use an INSTEAD OF trigger on the views to allow deletion */
				Log.w(TAG, "Ignoring attempt to delete record via view URI " + uri);
				throw new IllegalArgumentException("Cannot delete via view URI" + uri);
			case SHOUT_ID:
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					whereClause = ShoutProviderContract.Shouts._ID + "=" + id;
					whereArgs = null;
				} else {
					whereClause = selection + " and "
							+ ShoutProviderContract.Shouts._ID + "=" + id;
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
					whereClause = selection + " and "
							+ ShoutProviderContract.Users._ID + "=" + id;
					whereArgs = selectionArgs;
				}
				break;
			default:
				throw new IllegalArgumentException("Invalid or unknown URI " + uri);
		}
		int rowsAffected = mDB.delete(table, whereClause, whereArgs);
		notifyChange(uri);
		return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
		int match = sUriMatcher.match(uri);
		switch (match) {
			case SHOUTS:
			case SHOUTS_USER_ID:
				return MIME_SHOUT_MANY;
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
		int match = sUriMatcher.match(uri);
		String table = null;
		switch (match) {
			case SHOUTS:
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				break;
			case ROOTS:
			case COMMENTS:
			case RESHOUTS:
				/* Use an INSTEAD OF trigger on the views to allow insertion */
				Log.w(TAG, "Ignoring attempt to insert record via view URI " + uri);
				throw new IllegalArgumentException("Cannot insert record via view URI" + uri);
			case USERS:
				table = ShoutProviderContract.Users.TABLE_NAME;
				break;
			case MESSAGES:
				table = ShoutSearchContract.Messages.TABLE_NAME;
				break;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}
		Uri insertLocation = queryForPrexistingThenInsert(match, values, table, uri);
		return insertLocation;
	}

	/**
	 * Query to see if there is a row in the table at the specified URI that
	 * will cause a conflict due to a unique constraint.
	 * 
	 * @param uri The URI of the table with a unique constraint
	 * @param values The values that may cause conflict
	 * @param uniqueCols The columns specified as unique in the schema
	 * @param _ID The name of the integer primary key field
	 * @return The ID of the conflicting row, or -1 if there is no conflict
	 */
	private int queryForUniqueConflict(Uri uri, ContentValues values,
			String[] uniqueCols, final String _ID) {
		int id = -1;
		String[] projection = new String[] {
				_ID
		};
		StringBuilder selectionBuilder = new StringBuilder(16 * uniqueCols.length);
		String[] selectionArgs = new String[uniqueCols.length];
		for (int i = 0; i < uniqueCols.length; i++) {
			if (i > 0) {
				selectionBuilder.append(" AND ");
			}
			selectionBuilder.append(uniqueCols[i] + " = ?");
			String arg = values.getAsString(uniqueCols[i]);
			if (arg == null) {
				return -1;
			}
			selectionArgs[i] = arg;
		}
		Cursor cursor = query(uri, projection, selectionBuilder.toString(), selectionArgs, null);
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndex(_ID);
			id = cursor.getInt(idIndex);
		}
		cursor.close();
		return id;
	}

	private Uri queryForPrexistingThenInsert(int match, ContentValues values, String table, Uri uri) {
		int id;
		String[] uniqueCols;
		String idColumnName;
		switch (match) {
			case SHOUTS:
				uniqueCols = ShoutDatabaseHelper.SHOUT_UNIQUE_COLS;
				idColumnName = ShoutProviderContract.Shouts._ID;
				break;
			case USERS:
				uniqueCols = ShoutDatabaseHelper.USER_UNIQUE_COLS;
				idColumnName = ShoutProviderContract.Users._ID;
				break;
			default:
				throw new SQLException(
						"There is no unique column constraint, but you are checking for prexisting at uri: "
								+ uri.toString());
		}
		id = queryForUniqueConflict(uri, values, uniqueCols, idColumnName);
		// In the database already if the id is not -1
		boolean exists = (id != -1);
		if (!exists) {
			// Not in the database, so insert
			mDB = mOpenHelper.getWritableDatabase();
			long error = mDB.insert(table, null, values);
			/*
			 * Triggers break the row returned here, so requery to get correct
			 * row ID.
			 */
			if (error == -1) {
				throw new SQLException("Unable to insert into table " + table);
			} else {
				id = queryForUniqueConflict(uri, values, uniqueCols, idColumnName);
			}
		}
		Uri insertLocation = ContentUris.withAppendedId(uri, id);
		if (!exists) {
			// Only notify the observers if this was a new insert
			notifyChange(insertLocation);
		}
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
			case ROOTS:
				qBuilder.setTables(ShoutProviderContract.Shouts.ROOT_VIEW);
				break;
			case COMMENTS:
				qBuilder.setTables(ShoutProviderContract.Shouts.COMMENT_VIEW);
				break;
			case RESHOUTS:
				qBuilder.setTables(ShoutProviderContract.Shouts.RESHOUT_VIEW);
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
			case MESSAGES:
				qBuilder.setTables(ShoutSearchContract.Messages.TABLE_NAME);
				break;
			case MESSAGE_ID:
				qBuilder.setTables(ShoutSearchContract.Messages.TABLE_NAME);
				qBuilder.appendWhere(ShoutSearchContract.Messages._ID + "="
						+ uri.getLastPathSegment());
				break;
			case MESSAGES_SHOUT_ID:
				qBuilder.setTables(ShoutSearchContract.Messages.TABLE_NAME);
				qBuilder.appendWhere(ShoutSearchContract.Messages.SHOUT + " MATCH "
						+ uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}

		Cursor resultCursor = qBuilder.query(mDB, projection, selection,
				selectionArgs, null, null, sortOrder);
		resultCursor.setNotificationUri(this.getContext().getContentResolver(),
				uri);

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
			case ROOTS:
			case COMMENTS:
			case RESHOUTS:
				/* Use an INSTEAD OF trigger on the views to allow updating */
				Log.w(TAG, "Ignoring attempt to update record via view URI " + uri);
				throw new IllegalArgumentException("Cannot update record via view URI" + uri);
			case SHOUT_ID:
				id = uri.getLastPathSegment();
				table = ShoutProviderContract.Shouts.TABLE_NAME;
				if (TextUtils.isEmpty(selection)) {
					whereClause = ShoutProviderContract.Shouts._ID + "=" + id;
					whereArgs = null;
				} else {
					whereClause = selection + " and "
							+ ShoutProviderContract.Shouts._ID + "=" + id;
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
					whereClause = selection + " and "
							+ ShoutProviderContract.Shouts.AUTHOR + "=" + id;
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
					whereClause = selection + " and "
							+ ShoutProviderContract.Users._ID + "=" + id;
					whereArgs = selectionArgs;
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}
		int rowsAffected = mDB.update(table, values, whereClause, whereArgs);
		notifyChange(uri);
		return rowsAffected;
	}

	protected static final class ShoutDatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = ShoutDatabaseHelper.class
				.getSimpleName();

		public static final int VERSION = 2;
		public static final String DBNAME = "shout_base";

		private static final String SQL_CREATE_USER = "CREATE TABLE "
				+ ShoutProviderContract.Users.TABLE_NAME + "("
				+ ShoutProviderContract.Users._ID
				+ " INTEGER PRIMARY KEY ASC AUTOINCREMENT, "
				+ ShoutProviderContract.Users.USERNAME + " TEXT, "
				+ ShoutProviderContract.Users.PUB_KEY + " TEXT, "
				+ ShoutProviderContract.Users.AVATAR + " TEXT, "
				+ "UNIQUE (" + ShoutProviderContract.Users.PUB_KEY + ", "
				+ ShoutProviderContract.Users.USERNAME + ", "
				+ ShoutProviderContract.Users.AVATAR + " ) " + ");";

		private static final String SQL_CREATE_SHOUT = "CREATE TABLE "
				+ ShoutProviderContract.Shouts.TABLE_NAME + "("
				+ ShoutProviderContract.Shouts._ID
				+ " INTEGER PRIMARY KEY ASC AUTOINCREMENT, "
				+ ShoutProviderContract.Shouts.VERSION + " INTEGER, "
				+ ShoutProviderContract.Shouts.AUTHOR + " TEXT, "
				+ ShoutProviderContract.Shouts.PARENT + " TEXT, "
				+ ShoutProviderContract.Shouts.MESSAGE + " TEXT, "
				+ ShoutProviderContract.Shouts.LONGITUDE + " REAL, "
				+ ShoutProviderContract.Shouts.LATITUDE + " REAL, "
				+ ShoutProviderContract.Shouts.TIME_SENT + " LONG, "
				+ ShoutProviderContract.Shouts.TIME_RECEIVED + " LONG, "
				+ ShoutProviderContract.Shouts.HASH + " TEXT, "
				+ ShoutProviderContract.Shouts.SIGNATURE + " TEXT, "
				+ ShoutProviderContract.Shouts.USER_PK + " INTEGER, "
				+ ShoutProviderContract.Shouts.COMMENT_COUNT + " INTEGER DEFAULT 0, "
				+ ShoutProviderContract.Shouts.RESHOUT_COUNT + " INTEGER DEFAULT 0, "
				+ "UNIQUE (" + ShoutProviderContract.Shouts.HASH + "), "
				+ "FOREIGN KEY(" + ShoutProviderContract.Shouts.USER_PK
				+ ") REFERENCES " + ShoutProviderContract.Users.TABLE_NAME
				+ "(" + ShoutProviderContract.Users._ID + "), "
				+ "FOREIGN KEY(" + ShoutProviderContract.Shouts.PARENT
				+ ") REFERENCES " + ShoutProviderContract.Shouts.TABLE_NAME +
				"(" + ShoutProviderContract.Shouts.HASH + ")" + ");";

		private static final String SQL_CREATE_INDEX_SHOUT_PARENT = "CREATE INDEX idx_shout_parent ON "
				+ ShoutProviderContract.Shouts.TABLE_NAME
				+ " ("
				+ ShoutProviderContract.Shouts.PARENT + ");";

		private static final String SQL_CREATE_VIRTUAL_MESSAGE = "CREATE VIRTUAL TABLE "
				+ ShoutSearchContract.Messages.TABLE_NAME
				+ " USING fts3("
				+ ShoutSearchContract.Messages.SHOUT
				+ ", "
				+ ShoutSearchContract.Messages.MESSAGE + ");";

		private static final String SQL_CREATE_TRIGGER_COMMENT = "CREATE TRIGGER "
				+ "Update_Comment_Count AFTER INSERT ON "
				+ ShoutProviderContract.Shouts.TABLE_NAME + " WHEN new."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL AND new."
				+ ShoutProviderContract.Shouts.PARENT + " IS NOT NULL "
				+ "\nBEGIN\n" + "UPDATE "
				+ ShoutProviderContract.Shouts.TABLE_NAME + " SET "
				+ ShoutProviderContract.Shouts.COMMENT_COUNT + " = "
				+ ShoutProviderContract.Shouts.COMMENT_COUNT + " + 1 WHERE "
				+ ShoutProviderContract.Shouts.HASH + " = new."
				+ ShoutProviderContract.Shouts.PARENT + ";\nEND;";

		private static final String SQL_CREATE_TRIGGER_RESHOUT = "CREATE TRIGGER "
				+ "Update_Reshout_Count AFTER INSERT ON "
				+ ShoutProviderContract.Shouts.TABLE_NAME + " WHEN new."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NULL AND new."
				+ ShoutProviderContract.Shouts.PARENT + " IS NOT NULL "
				+ "\nBEGIN\n" + "UPDATE "
				+ ShoutProviderContract.Shouts.TABLE_NAME + " SET "
				+ ShoutProviderContract.Shouts.RESHOUT_COUNT + " = "
				+ ShoutProviderContract.Shouts.RESHOUT_COUNT + " + 1 WHERE "
				+ ShoutProviderContract.Shouts.HASH + " = new."
				+ ShoutProviderContract.Shouts.PARENT + ";\nEND;";

		private static final String SQL_CREATE_TRIGGER_MESSAGE = "CREATE TRIGGER "
				+ "Update_FTS3_Message AFTER INSERT ON "
				+ ShoutProviderContract.Shouts.TABLE_NAME + " WHEN new."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL "
				+ "\nBEGIN\n" + "INSERT INTO " + ShoutSearchContract.Messages.TABLE_NAME + "( "
				+ ShoutSearchContract.Messages.SHOUT + ", " + ShoutSearchContract.Messages.MESSAGE
				+ " ) VALUES ( new." + ShoutProviderContract.Shouts._ID + ", new."
				+ ShoutProviderContract.Shouts.MESSAGE + " );\nEND;";

		private static final String SQL_CREATE_VIEW_ROOT = "CREATE VIEW "
				+ ShoutProviderContract.Shouts.ROOT_VIEW + " AS SELECT * FROM "
				+ ShoutProviderContract.Shouts.TABLE_NAME
				+ " WHERE " + ShoutProviderContract.Shouts.PARENT + " IS NULL AND "
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL;";

		private static final String SQL_CREATE_VIEW_COMMENT = "CREATE VIEW "
				+ ShoutProviderContract.Shouts.COMMENT_VIEW
				+ " AS SELECT comment.* FROM "
				+ ShoutProviderContract.Shouts.ROOT_VIEW
				+ " AS root INNER JOIN " + ShoutProviderContract.Shouts.TABLE_NAME
				+ " AS comment ON (comment.Parent=root." + ShoutProviderContract.Shouts.HASH
				+ " AND comment." + ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL);";

		private static final String SQL_CREATE_VIEW_RESHOUT = "CREATE VIEW "
				+ ShoutProviderContract.Shouts.RESHOUT_VIEW
				+ " AS SELECT reshout.* FROM "
				+ ShoutProviderContract.Shouts.TABLE_NAME
				+ " AS parent INNER JOIN " + ShoutProviderContract.Shouts.TABLE_NAME
				+ " AS reshout ON (reshout." + ShoutProviderContract.Shouts.PARENT + "=parent."
				+ ShoutProviderContract.Shouts.HASH + " AND reshout."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NULL);";

		private static final String[] USER_UNIQUE_COLS = {
				ShoutProviderContract.Users.PUB_KEY,
				ShoutProviderContract.Users.USERNAME,
				ShoutProviderContract.Users.AVATAR
		};

		private static final String[] SHOUT_UNIQUE_COLS = {
				ShoutProviderContract.Shouts.HASH
		};

		public ShoutDatabaseHelper(Context context) {
			super(context, DBNAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_USER);
			db.execSQL(SQL_CREATE_SHOUT);
			db.execSQL(SQL_CREATE_INDEX_SHOUT_PARENT);
			db.execSQL(SQL_CREATE_VIRTUAL_MESSAGE);
			db.execSQL(SQL_CREATE_TRIGGER_COMMENT);
			db.execSQL(SQL_CREATE_TRIGGER_RESHOUT);
			db.execSQL(SQL_CREATE_TRIGGER_MESSAGE);
			db.execSQL(SQL_CREATE_VIEW_ROOT);
			db.execSQL(SQL_CREATE_VIEW_COMMENT);
			db.execSQL(SQL_CREATE_VIEW_RESHOUT);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			db.execSQL(ENABLE_FK);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
				case 1:
					// Upgrade to version 2
					db.execSQL(SQL_CREATE_INDEX_SHOUT_PARENT);
					db.execSQL(SQL_CREATE_VIEW_ROOT);
					db.execSQL(SQL_CREATE_VIEW_COMMENT);
					db.execSQL(SQL_CREATE_VIEW_RESHOUT);
					break;
				default:
					Log.e(TAG, String.format(
							"Unsupported call to onUpgrade. Old Version: %d. New Version: %d.",
							oldVersion, newVersion));
					throw new IllegalStateException();
			}
		}

	}

}
