
package org.whispercomm.shout.provider.image;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class defines constants like content URIs, column names.
 * 
 * @author Bowen Xu
 */
public class ImageProviderContract {

	private static final String TAG = ImageProviderContract.class
			.getSimpleName();

	/**
	 * Define the authority for content provider
	 */
	static final String AUTHORITY = "org.whispercomm.shout.image.provider";
	/**
	 * The content:// style URI for the Shout provider
	 */
	static final Uri CONTENT_URI_BASE = Uri.parse("content://" + AUTHORITY);

	public static class Avatars implements BaseColumns {
		/**
		 * Base content URI for the table of Shouts
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				CONTENT_URI_BASE, "avatars");
		/**
		 * Column name of the primary key.
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * Column name of the value.
		 */
		public static final String DATA = "_data";
	}

	public static class Thumbnails implements BaseColumns {
		/**
		 * Base content URI for the table of Shouts
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				CONTENT_URI_BASE, "thumbnails");

		/**
		 * Column name of the primary key.
		 */
		public static final String _ID = BaseColumns._ID;

		/**
		 * Column name of the value.
		 */
		public static final String DATA = "_data";

	}

}
