
package org.whispercomm.shout.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class ColorProvider extends ContentProvider {

	private ColorDatabase dbOpenHelper;
	private SQLiteDatabase db;

	private static final String TAG = ShoutColorContract.class.getSimpleName();

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final String AUTHORITY = ShoutColorContract.AUTHORITY;

	public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri COLOR_URI = Uri.withAppendedPath(BASE_URI, ColorDatabase.DATABASE_NAME);

	static {
		sUriMatcher.addURI(AUTHORITY, ColorDatabase.DATABASE_NAME, 1);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Don't worry about this now
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Don't worry about this now
		return null;
	}

	@Override
	public boolean onCreate() {
		Log.v(TAG, "ColorProvider onCreate is called");
		dbOpenHelper = new ColorDatabase(this.getContext());
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = sUriMatcher.match(uri);
		String table = null;
		if (match == 1) {
			table = ColorDatabase.DATABASE_NAME;
		}
		else {
			throw new IllegalArgumentException("Unknown or invalid URI " + uri);
		}
		db = dbOpenHelper.getWritableDatabase();
		long insertRow = db.insert(table, null, values);
		Uri insertLocation = ContentUris.withAppendedId(uri, insertRow);
		return insertLocation;

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String
			selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		db = dbOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = sUriMatcher.match(uri);
		switch (match) {
			case 1:
				qBuilder.setTables(ColorDatabase.DATABASE_NAME);
				break;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);

		}
		Cursor resultCursor = qBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		resultCursor.setNotificationUri(this.getContext().getContentResolver(),
				uri);

		return resultCursor;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Don't worry about this now
		return 0;
	}

	protected static final class ColorDatabase extends SQLiteOpenHelper {

		private static final String TAG = ColorDatabase.class.getSimpleName();

		public static final String KEY_ID = "id";
		public static final String KEY_USERNAME = "username";
		public static final String KEY_PUBLIC_KEY = "user_public_key";
		public static final String KEY_COLOR = "color";
		public static final String DATABASE_NAME = "colordatabase";
		private static final int DATABASE_VERSION = 1;

		public static final String[] allColumns = {
				ColorDatabase.KEY_USERNAME, ColorDatabase.KEY_PUBLIC_KEY, ColorDatabase.KEY_COLOR
		};

		private static final String DATABASE_CREATE = "CREATE TABLE "
				+ DATABASE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_USERNAME
				+ " TEXT, "
				+ KEY_PUBLIC_KEY
				+ " TEXT, " + KEY_COLOR + " INTEGER NOT NULL);";

		public ColorDatabase(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			System.out.println("colordatabase oncreate called");
			Log.v(TAG, "Created database");
			database.execSQL(DATABASE_CREATE);
		}

		public String[] getDatabaseColumns() {
			return allColumns;
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			throw new IllegalStateException("Database upgrade not supported");
		}

	}

}
