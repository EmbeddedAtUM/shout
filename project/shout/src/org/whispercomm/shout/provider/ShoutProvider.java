
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

	private static final int ALL_SHOUTS = 1;
	private static final int ORIGINAL_SHOUTS = 2;
	private static final int COMMENT_SHOUTS = 3;
	private static final int RESHOUT_SHOUTS = 4;

	private static final int USERS = 10;
	private static final int MESSAGES = 20;

	private static final int SHOUT_ID = 100;
	private static final int USER_ID = 110;
	private static final int MESSAGE_ID = 120;

	private static final int SHOUTS_USER_ID = 200;
	private static final int MESSAGES_SHOUT_ID = 300;

	static {
		sUriMatcher.addURI(AUTHORITY, "shouts", ALL_SHOUTS);
		sUriMatcher.addURI(AUTHORITY, "shouts/filter/original", ORIGINAL_SHOUTS);
		sUriMatcher.addURI(AUTHORITY, "shouts/filter/comment", COMMENT_SHOUTS);
		sUriMatcher.addURI(AUTHORITY, "shouts/filter/reshout", RESHOUT_SHOUTS);

		sUriMatcher.addURI(AUTHORITY, "users", USERS);

		sUriMatcher.addURI(AUTHORITY, "shouts/#", SHOUT_ID);
		sUriMatcher.addURI(AUTHORITY, "users/#", USER_ID);
		sUriMatcher.addURI(AUTHORITY, "shouts/users/#", SHOUTS_USER_ID);

		sUriMatcher.addURI(AUTHORITY, "message", MESSAGES);
		sUriMatcher.addURI(AUTHORITY, "message/#", MESSAGE_ID);
		sUriMatcher.addURI(AUTHORITY, "message/shout/#", MESSAGES_SHOUT_ID);
	}

	private ShoutDatabaseHelper mOpenHelper;

	private SQLiteDatabase mDB;

	private static final String ENABLE_FK = "PRAGMA foreign_keys = ON";

	@Override
	public boolean onCreate() {
		mOpenHelper = new ShoutDatabaseHelper(this.getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		int match = sUriMatcher.match(uri);
		switch (match) {
			case ALL_SHOUTS:
			case ORIGINAL_SHOUTS:
			case COMMENT_SHOUTS:
			case RESHOUT_SHOUTS:
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
			case ALL_SHOUTS:
				table = ShoutDatabaseHelper.SHOUTS_TABLE;
				break;
			case USERS:
				table = ShoutDatabaseHelper.USERS_TABLE;
				break;
			case MESSAGES:
				table = ShoutSearchContract.Messages.TABLE_NAME;
				break;
			case ORIGINAL_SHOUTS:
			case COMMENT_SHOUTS:
			case RESHOUT_SHOUTS:
				/* Use an INSTEAD OF trigger on the views to allow insertion */
				Log.w(TAG, "Ignoring attempt to insert record via view URI " + uri);
				throw new IllegalArgumentException("Cannot insert record via this URI" + uri);
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
			case ALL_SHOUTS:
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
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		mDB = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = sUriMatcher.match(uri);
		switch (match) {
			case ALL_SHOUTS:
				qBuilder.setTables(ShoutDatabaseHelper.DENORMED_SHOUT_VIEW);
				break;
			case ORIGINAL_SHOUTS:
				/*
				 * Order original shouts by most-recent comment. This special
				 * case ordering should be built into a better query language,
				 * at some point.
				 */
				String join_tables = ShoutDatabaseHelper.DENORMED_ORIGINAL_VIEW
						+ " AS original LEFT JOIN "
						+ ShoutDatabaseHelper.DENORMED_COMMENT_VIEW
						+ " AS comment ON (original." + ShoutProviderContract.Shouts.HASH
						+ "=comment."
						+ ShoutProviderContract.Shouts.PARENT + " OR original."
						+ ShoutProviderContract.Shouts.HASH + "=comment."
						+ ShoutProviderContract.Shouts.HASH + ")";
				qBuilder.setTables(join_tables);
				qBuilder.setDistinct(true);
				projection = new String[] {
						// coalesces must come first, for sortOrder to work
						"coalesce(comment.Time_received, original.Time_received) AS Time_received, "
								+ "coalesce(comment.Timestamp, original.Timestamp) AS Timestamp, "
								+ "original.*"
				};
				break;
			case COMMENT_SHOUTS:
				qBuilder.setTables(ShoutDatabaseHelper.DENORMED_COMMENT_VIEW);
				break;
			case RESHOUT_SHOUTS:
				qBuilder.setTables(ShoutDatabaseHelper.DENORMED_RESHOUT_VIEW);
				break;
			case SHOUT_ID:
				qBuilder.setTables(ShoutDatabaseHelper.DENORMED_SHOUT_VIEW);
				qBuilder.appendWhere(ShoutProviderContract.Shouts._ID + "="
						+ uri.getLastPathSegment());
				break;
			case SHOUTS_USER_ID:
				qBuilder.setTables(ShoutDatabaseHelper.DENORMED_SHOUT_VIEW);
				qBuilder.appendWhere(ShoutProviderContract.Shouts.AUTHOR + "="
						+ uri.getLastPathSegment());
				break;
			case USERS:
				qBuilder.setTables(ShoutDatabaseHelper.USERS_TABLE);
				break;
			case USER_ID:
				qBuilder.setTables(ShoutDatabaseHelper.USERS_TABLE);
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
		/*
		 * This update implementation is incomplete. The ALL_SHOUTS URI includes
		 * author information, the following queries do not join against the
		 * Users table.
		 */
		mDB = mOpenHelper.getWritableDatabase();
		mDB.execSQL(ENABLE_FK);
		String id, whereClause, table = null;
		String[] whereArgs = null;

		int match = sUriMatcher.match(uri);
		switch (match) {
			case ALL_SHOUTS:
				table = ShoutDatabaseHelper.SHOUTS_TABLE;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case ORIGINAL_SHOUTS:
			case COMMENT_SHOUTS:
			case RESHOUT_SHOUTS:
				/* Use an INSTEAD OF trigger on the views to allow updating */
				Log.w(TAG, "Ignoring attempt to update record via view URI " + uri);
				throw new IllegalArgumentException("Cannot update record via view URI" +
						uri);
			case SHOUT_ID:
				id = uri.getLastPathSegment();
				table = ShoutDatabaseHelper.SHOUTS_TABLE;
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
				table = ShoutDatabaseHelper.SHOUTS_TABLE;
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
				table = ShoutDatabaseHelper.USERS_TABLE;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case USER_ID:
				id = uri.getLastPathSegment();
				table = ShoutDatabaseHelper.USERS_TABLE;
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

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		/*
		 * This delete implementation is incomplete. The ALL_SHOUTS URI includes
		 * author information, the following queries do not join against the
		 * Users table.
		 */
		mDB = mOpenHelper.getWritableDatabase();
		mDB.execSQL(ENABLE_FK);
		int match = sUriMatcher.match(uri);
		String id, table, whereClause = null;
		String[] whereArgs = null;
		switch (match) {
			case ALL_SHOUTS:
				table = ShoutDatabaseHelper.SHOUTS_TABLE;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case ORIGINAL_SHOUTS:
			case COMMENT_SHOUTS:
			case RESHOUT_SHOUTS:
				/* Use an INSTEAD OF trigger on the views to allow deletion */
				Log.w(TAG, "Ignoring attempt to delete record via view URI " + uri);
				throw new IllegalArgumentException("Cannot delete via view URI" +
						uri);
			case SHOUT_ID:
				table = ShoutDatabaseHelper.SHOUTS_TABLE;
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
				table = ShoutDatabaseHelper.USERS_TABLE;
				whereClause = selection;
				whereArgs = selectionArgs;
				break;
			case USER_ID:
				table = ShoutDatabaseHelper.USERS_TABLE;
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

	private void notifyChange(Uri uri) {
		List<Uri> uris = new ArrayList<Uri>();

		uris.add(uri);

		// Add other URIs that also match
		int match = sUriMatcher.match(uri);
		switch (match) {
			case SHOUT_ID:
				uris.add(ShoutProviderContract.Shouts.CONTENT_URI);
			case ALL_SHOUTS:
				uris.add(ShoutProviderContract.Shouts.ORIGINAL_CONTENT_URI);
				uris.add(ShoutProviderContract.Shouts.COMMENT_CONTENT_URI);
				uris.add(ShoutProviderContract.Shouts.RESHOUT_CONTENT_URI);
				break;
			case ORIGINAL_SHOUTS:
			case COMMENT_SHOUTS:
			case RESHOUT_SHOUTS:
				uris.add(ShoutProviderContract.Shouts.CONTENT_URI);
		}

		for (Uri u : uris) {
			this.getContext().getContentResolver()
					.notifyChange(u, null);
		}
	}

	protected static final class ShoutDatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = ShoutDatabaseHelper.class
				.getSimpleName();

		public static final int VERSION = 2;
		public static final String DBNAME = "shout_base";

		public static final String SHOUTS_TABLE = "shout";
		public static final String USERS_TABLE = "user";

		public static final String DENORMED_SHOUT_VIEW = "denormed_shout";
		public static final String DENORMED_ORIGINAL_VIEW = "denormed_original";
		public static final String DENORMED_COMMENT_VIEW = "denormed_comment";
		public static final String DENORMED_RESHOUT_VIEW = "denormed_reshout";

		private static final String SQL_CREATE_USER = "CREATE TABLE "
				+ USERS_TABLE + "("
				+ ShoutProviderContract.Users._ID
				+ " INTEGER PRIMARY KEY ASC AUTOINCREMENT, "
				+ ShoutProviderContract.Users.USERNAME + " TEXT, "
				+ ShoutProviderContract.Users.PUB_KEY + " TEXT, "
				+ ShoutProviderContract.Users.AVATAR + " TEXT, "
				+ "UNIQUE (" + ShoutProviderContract.Users.PUB_KEY + ", "
				+ ShoutProviderContract.Users.USERNAME + ", "
				+ ShoutProviderContract.Users.AVATAR + " ) " + ");";

		private static final String SQL_CREATE_SHOUT = "CREATE TABLE "
				+ SHOUTS_TABLE + "("
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
				+ ") REFERENCES " + USERS_TABLE
				+ "(" + ShoutProviderContract.Users._ID + "), "
				+ "FOREIGN KEY(" + ShoutProviderContract.Shouts.PARENT
				+ ") REFERENCES " + SHOUTS_TABLE +
				"(" + ShoutProviderContract.Shouts.HASH + ")" + ");";

		private static final String SQL_CREATE_INDEX_SHOUT_PARENT = "CREATE INDEX idx_shout_parent ON "
				+ SHOUTS_TABLE
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
				+ SHOUTS_TABLE + " WHEN new."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL AND new."
				+ ShoutProviderContract.Shouts.PARENT + " IS NOT NULL "
				+ "\nBEGIN\n" + "UPDATE "
				+ SHOUTS_TABLE + " SET "
				+ ShoutProviderContract.Shouts.COMMENT_COUNT + " = "
				+ ShoutProviderContract.Shouts.COMMENT_COUNT + " + 1 WHERE "
				+ ShoutProviderContract.Shouts.HASH + " = new."
				+ ShoutProviderContract.Shouts.PARENT + ";\nEND;";

		private static final String SQL_CREATE_TRIGGER_RESHOUT = "CREATE TRIGGER "
				+ "Update_Reshout_Count AFTER INSERT ON "
				+ SHOUTS_TABLE + " WHEN new."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NULL AND new."
				+ ShoutProviderContract.Shouts.PARENT + " IS NOT NULL "
				+ "\nBEGIN\n" + "UPDATE "
				+ SHOUTS_TABLE + " SET "
				+ ShoutProviderContract.Shouts.RESHOUT_COUNT + " = "
				+ ShoutProviderContract.Shouts.RESHOUT_COUNT + " + 1 WHERE "
				+ ShoutProviderContract.Shouts.HASH + " = new."
				+ ShoutProviderContract.Shouts.PARENT + ";\nEND;";

		private static final String SQL_CREATE_TRIGGER_MESSAGE = "CREATE TRIGGER "
				+ "Update_FTS3_Message AFTER INSERT ON "
				+ SHOUTS_TABLE + " WHEN new."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL "
				+ "\nBEGIN\n" + "INSERT INTO " + ShoutSearchContract.Messages.TABLE_NAME + "( "
				+ ShoutSearchContract.Messages.SHOUT + ", " + ShoutSearchContract.Messages.MESSAGE
				+ " ) VALUES ( new." + ShoutProviderContract.Shouts._ID + ", new."
				+ ShoutProviderContract.Shouts.MESSAGE + " );\nEND;";

		private static final String SQL_CREATE_VIEW_DENORMED_SHOUT = "CREATE VIEW "
				+ DENORMED_SHOUT_VIEW
				+ " AS SELECT shout.*, user.Name, user.Key, user.Avatar FROM "
				+ SHOUTS_TABLE + " AS shout JOIN " + USERS_TABLE
				+ " AS user ON (shout.User_key=user._id);";

		private static final String SQL_CREATE_VIEW_DENORMED_ORIGINAL = "CREATE VIEW "
				+ DENORMED_ORIGINAL_VIEW + " AS SELECT * FROM "
				+ DENORMED_SHOUT_VIEW
				+ " WHERE " + ShoutProviderContract.Shouts.PARENT + " IS NULL AND "
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL;";

		private static final String SQL_CREATE_VIEW_DENORMED_COMMENT = "CREATE VIEW "
				+ DENORMED_COMMENT_VIEW
				+ " AS SELECT comment.* FROM "
				+ DENORMED_ORIGINAL_VIEW
				+ " AS root INNER JOIN " + DENORMED_SHOUT_VIEW
				+ " AS comment ON (comment.Parent=root." + ShoutProviderContract.Shouts.HASH
				+ " AND comment." + ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL);";

		private static final String SQL_CREATE_VIEW_DENORMED_RESHOUT = "CREATE VIEW "
				+ DENORMED_RESHOUT_VIEW
				+ " AS SELECT reshout.* FROM "
				+ DENORMED_SHOUT_VIEW
				+ " AS parent INNER JOIN " + DENORMED_SHOUT_VIEW
				+ " AS reshout ON (reshout." + ShoutProviderContract.Shouts.PARENT + "=parent."
				+ ShoutProviderContract.Shouts.HASH + " AND reshout."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NULL AND parent."
				+ ShoutProviderContract.Shouts.MESSAGE + " IS NOT NULL);";

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
			db.execSQL(SQL_CREATE_VIEW_DENORMED_SHOUT);
			db.execSQL(SQL_CREATE_VIEW_DENORMED_ORIGINAL);
			db.execSQL(SQL_CREATE_VIEW_DENORMED_COMMENT);
			db.execSQL(SQL_CREATE_VIEW_DENORMED_RESHOUT);
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
					db.execSQL(SQL_CREATE_VIEW_DENORMED_SHOUT);
					db.execSQL(SQL_CREATE_VIEW_DENORMED_ORIGINAL);
					db.execSQL(SQL_CREATE_VIEW_DENORMED_COMMENT);
					db.execSQL(SQL_CREATE_VIEW_DENORMED_RESHOUT);
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
