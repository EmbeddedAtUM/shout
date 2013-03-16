
package org.whispercomm.shout.content;

import java.io.IOException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.errors.NotFoundException;

import android.support.v4.util.LruCache;
import android.util.Log;

public class ShoutImageStorage {
	private static final String TAG = ShoutImageStorage.class.getSimpleName();

	/**
	 * TODO: ShoutImageStorage should be an application singleton and cache an
	 * instance member, but until that happens, the cache is static.
	 */
	private static LruCache<Hash, ShoutImage> LRU_CACHE = new LruCache<Hash, ShoutImage>(100);

	private ContentManager mContentManager;

	public ShoutImageStorage(ContentManager contentManager) {
		this.mContentManager = contentManager;
	}

	public HashReference<ShoutImage> store(ShoutImage image) throws IOException {
		Hash hash = mContentManager.store(image.toByteArray(),
				image.getMimeType());
		return new SimpleHashReference<ShoutImage>(hash, image);
	}

	public HashReference<ShoutImage> retrieve(HashReference<ShoutImage> imageReference)
			throws IOException {
		return retrieve(imageReference.getHash());
	}

	public HashReference<ShoutImage> retrieve(Hash imageHash) throws IOException {
		ShoutImage image = LRU_CACHE.get(imageHash);

		if (image == null) {
			try {
				image = loadImage(imageHash);
				LRU_CACHE.put(imageHash, image);
			} catch (IllegalArgumentException e) {
				Log.w(TAG, "Treating undecodable image as missing.", e);
			} catch (NotFoundException e) {
				// Ignore.
			}
		}

		return new SimpleHashReference<ShoutImage>(imageHash, image);
	}

	private ShoutImage loadImage(Hash imageHash) throws NotFoundException,
			IllegalArgumentException,
			IOException {
		Content content = mContentManager.retrieve(imageHash);
		return new ShoutImage(content.getData(), content.getMimeType());
	}
}
