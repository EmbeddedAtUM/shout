package org.whispercomm.shout.provider;

/**
 * Content provider for storing Shout messages
 */
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class ShoutProvider extends ContentProvider {

	private static final String TAG = ShoutProvider.class.getName();
	private static final String AUTHORITY = TAG;

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	/**
	 * Initializer block to set URIs in the UriMatcher
	 */
	static {

	}

	private SQLiteDatabase mDB;
	private ShoutProviderDatabaseHelper mDBHelper;

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate() called");
		mDBHelper = new ShoutProviderDatabaseHelper(super.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		mDB = mDBHelper.getReadableDatabase();
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
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
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}


}
