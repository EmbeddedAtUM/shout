
package org.whispercomm.shout.provider.image;

import org.whispercomm.shout.Hash;

import android.net.Uri;

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

	/**
	 * The uri for images
	 */
	static Uri IMAGES_URI_BASE = Uri.withAppendedPath(CONTENT_URI_BASE, "images");

	/**
	 * Constructs the content provider {@link Uri} for a shout image.
	 * 
	 * @param hash the hash reference of the shout image
	 * @return the content provider {@code Uri} for referenced image
	 */
	public static Uri imageUri(Hash hash) {
		return Uri.withAppendedPath(IMAGES_URI_BASE, hash.toString());
	}

}
