
package org.whispercomm.shout.provider;

import java.util.ArrayList;
import java.util.List;

import org.whispercomm.shout.Shout;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class ShoutSearchContract {

	private static final String TAG = ShoutSearchContract.class.getSimpleName();

	public static class Messages implements BaseColumns {
		public static final String TABLE_NAME = "message";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				ShoutProviderContract.CONTENT_URI_BASE, TABLE_NAME);

		/**
		 * The primary key column. It is automatically maintained by the FTS3
		 * table.
		 */
		public static final String _ID = "rowid";

		/**
		 * A reference to a Shout primary key (ID)
		 */
		public static final String SHOUT = "Shout";

		/**
		 * The shout message for the shout with the given ID.
		 */
		public static final String MESSAGE = "Content";
	}

	/**
	 * Searches for Shouts with the given string in the message body.
	 * 
	 * @param context
	 * @param searchString
	 * @return List of Shouts matching the query, empty list if no Shouts
	 *         matched
	 */
	public static List<Shout> searchShoutMessage(Context context, String searchString) {
		String[] projection = {
				Messages.SHOUT
		};
		String selection = Messages.MESSAGE + " MATCH ?";
		String[] selectionArgs = {
				searchString
		};
		Cursor cursor = context.getContentResolver().query(Messages.CONTENT_URI, projection,
				selection, selectionArgs, null);
		if (cursor == null) {
			Log.e(TAG, "Null cursor returned on messages FTS");
			return null;
		}
		List<Shout> results = new ArrayList<Shout>();
		int idIndex = cursor.getColumnIndex(Messages.SHOUT);
		while (cursor.moveToNext()) {
			int shoutId = cursor.getInt(idIndex);
			Shout match = ShoutProviderContract.retrieveShoutById(context, shoutId);
			results.add(match);
		}
		return results;
	}
}
