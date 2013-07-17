
package org.whispercomm.shout.provider.image;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.content.Content;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.errors.NotFoundException;
import org.whispercomm.shout.provider.ShoutProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * Content Provider to store and share images on this device
 * 
 * @author Bowen Xu
 */

public class ImageProvider extends ContentProvider {
	private static final String TAG = ShoutProvider.class.getSimpleName();
	private static final String AUTHORITY = ImageProviderContract.AUTHORITY;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int THUMBNAILS_ID = 1;
	private static final int THUMBNAILS = 2;
	private static final int IMAGES_ID = 3;
	private static final int IMAGES = 4;

	static {
		sUriMatcher.addURI(AUTHORITY, "thumbnails", THUMBNAILS);
		sUriMatcher.addURI(AUTHORITY, "thumbnails/*", THUMBNAILS_ID);
		sUriMatcher.addURI(AUTHORITY, "images", IMAGES);
		sUriMatcher.addURI(AUTHORITY, "images/*", IMAGES_ID);
	}

	/**
	 * Define the MIME types of image provider
	 */
	private static final String MIME_THUMBNAIL = "vnd.android.cursor.item/thumbnail";
	private static final String MIME_THUMBNAILS = "vnd.android.cursor.dir/thumbnail";
	private static final String MIME_IMAGE = "vnd.android.cursor.item/image";
	private static final String MIME_IMAGES = "vnd.android.cursor.dir/images";

	private ContentManager mContentManager;

	private ExecutorService mExecutor;

	@Override
	public boolean onCreate() {
		mContentManager = new ContentManager(this.getContext());
		mExecutor = Executors.newCachedThreadPool();
		return true;
	}

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
			case IMAGES_ID:
				return MIME_IMAGE;
			case IMAGES:
				return MIME_IMAGES;
			default:
				throw new IllegalArgumentException("Unknown or invalid URI " + uri);

		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
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
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		int match = sUriMatcher.match(uri);
		if (match != THUMBNAILS_ID && match != IMAGES_ID)
			throw new IllegalArgumentException("Invalid URI: " + uri);

		try {
			Content content = mContentManager.retrieve(new Hash(uri.getLastPathSegment()));
			return openPipeHelper(content.getData());
		} catch (IOException e) {
			Log.e(TAG, "", e);
			throw new FileNotFoundException("IOException while retrieving content");
		} catch (NotFoundException e) {
			throw new FileNotFoundException(e.getMessage());
		}
	}

	/**
	 * Partial replacement for the openPipeHelper(...), which was not added
	 * until API 11.
	 * 
	 * @param data the data to write to the pipe
	 * @return the file descriptor pointing to the read of the pipe
	 * @throws FileNotFoundException on IO exceptions, to match the API 11
	 *             version of this method
	 */
	private ParcelFileDescriptor openPipeHelper(final byte[] data) throws FileNotFoundException {
		try {
			final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();

			Runnable task = new Runnable() {
				@Override
				public void run() {
					writeDataToPipe(fds[1], data);
					try {
						fds[1].close();
					} catch (IOException e) {
						Log.w(TAG, "Failed to close pipe", e);
					}
				}
			};
			mExecutor.submit(task);

			return fds[0];
		} catch (IOException e) {
			throw new FileNotFoundException("Failed to create pipe");
		}
	}

	/**
	 * Partial replacement for the writeDataToPipe(...) method of the
	 * ContentProvider.DataPipeWriter<T> interface, which was not added until
	 * API 11.
	 * 
	 * @param output the file descriptor of the write end of the pipe
	 * @param data the data to write to the pipe
	 */
	private void writeDataToPipe(ParcelFileDescriptor output, byte[] data) {
		FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
		try {
			fout.write(data);
		} catch (IOException e) {
			Log.e(TAG, "Failed to transfer data stream.");
		} finally {
			try {
				fout.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed to close fileoutput stream");
			}
		}
	}
}
