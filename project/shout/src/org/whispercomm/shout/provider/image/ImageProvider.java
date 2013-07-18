
package org.whispercomm.shout.provider.image;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.content.Content;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.errors.NotFoundException;
import org.whispercomm.shout.provider.ShoutProvider;

import android.content.ContentProvider;
import android.content.ContentProvider.PipeDataWriter;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * Content Provider to store and share images on this device
 * 
 * @author Bowen Xu
 */

public class ImageProvider extends ContentProvider implements PipeDataWriter<byte[]> {
	private static final String TAG = ShoutProvider.class.getSimpleName();
	private static final String AUTHORITY = ImageProviderContract.AUTHORITY;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int THUMBNAILS_ID = 1;
	private static final int THUMBNAILS = 2;
	private static final int AVATARS_ID = 3;
	private static final int AVATARS = 4;

	static {
		sUriMatcher.addURI(AUTHORITY, "thumbnails", THUMBNAILS);
		sUriMatcher.addURI(AUTHORITY, "thumbnails/*", THUMBNAILS_ID);
		sUriMatcher.addURI(AUTHORITY, "avatars", AVATARS);
		sUriMatcher.addURI(AUTHORITY, "avatars/*", AVATARS_ID);
	}

	/**
	 * Define the MIME types of image provider
	 */
	private static final String MIME_THUMBNAIL = "vnd.android.cursor.item/thumbnail";
	private static final String MIME_THUMBNAILS = "vnd.android.cursor.dir/thumbnail";
	private static final String MIME_AVATAR = "vnd.android.cursor.item/avatar";
	private static final String MIME_AVATARS = "vnd.android.cursor.dir/avatars";

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		int match = sUriMatcher.match(uri);
		switch (match) {
			case THUMBNAILS_ID:
				return MIME_THUMBNAIL;
			case THUMBNAILS:
				return MIME_THUMBNAILS;
			case AVATARS_ID:
				return MIME_AVATAR;
			case AVATARS:
				return MIME_AVATARS;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);

		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {

		int match = sUriMatcher.match(uri);
		if (match != THUMBNAILS_ID && match != AVATARS_ID) {
			throw new IllegalArgumentException("Can't open file due to unknown or invalid URI "
					+ uri);
		}
		String imageType = match == THUMBNAILS_ID ? "image/jpeg" : "image/png";

		// Retrieve data stream of images from content manager
		ContentManager mContentManager = new ContentManager(this.getContext());
		String hashStr = uri.getLastPathSegment();
		Hash hash = new Hash(hashStr);
		Content content = null;
		// InputStream is = null;
		AssetFileDescriptor afd = null;
		try {
			content = mContentManager.retrieve(hash);

			if (content != null) {
				afd = new AssetFileDescriptor(openPipeHelper(uri, imageType, null,
						content.getData(),
						this), 0,
						AssetFileDescriptor.UNKNOWN_LENGTH);
			}
			else
				throw new FileNotFoundException();

		} catch (NotFoundException e) {
			Log.i(TAG, "content retrieve failed.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return afd;

	}

	@Override
	public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType, Bundle opts,
			byte[] args) {
		// transfer data from stream to pipe
		FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());

		try {
			if (args == null)
				return;
			fout.write(args);

		} catch (IOException e) {
			Log.i(TAG, "Failed to transfer data stream.");
		} finally {
			try {
				fout.close();
			} catch (IOException e) {
				Log.i(TAG, "Failed to close fileoutput stream");
			}
		}
	}
}
