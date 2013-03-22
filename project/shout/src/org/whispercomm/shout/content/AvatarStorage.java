
package org.whispercomm.shout.content;

import java.io.IOException;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.errors.NotFoundException;

import android.app.Application;
import android.content.Context;
import android.support.v4.util.LruCache;
import android.util.Log;

public class AvatarStorage {
	private static final String TAG = AvatarStorage.class.getSimpleName();

	/**
	 * Use with {@link Context#getSystemService(String)} to retrieve an
	 * {@link AvatarStorage} for accessing content.
	 * <p>
	 * Since {@link AvatarStorage} is not a standard system service, you must
	 * create a custom {@link Application} subclass implementing
	 * {@link Application#getSystemService(String)} and add it to your
	 * {@code AndroidManifest.xml}.
	 */
	public static final String SHOUT_AVATAR_SERVICE = "org.whispercomm.shout.content.AvatarStorage";

	/**
	 * TODO: AvatarStorage should be an application singleton and cache an
	 * instance member, but until that happens, the cache is static.
	 */
	private static LruCache<Hash, Avatar> LRU_CACHE = new LruCache<Hash, Avatar>(100);

	private ContentManager mContentManager;

	public AvatarStorage(ContentManager contentManager) {
		this.mContentManager = contentManager;
	}

	public HashReference<Avatar> store(Avatar avatar) throws IOException {
		Hash hash = mContentManager.store(avatar.toByteArray(),
				avatar.getMimeType());
		return new SimpleHashReference<Avatar>(hash, avatar);
	}

	public HashReference<Avatar> retrieve(HashReference<Avatar> avatarReference) throws IOException {
		return retrieve(avatarReference.getHash());
	}

	public HashReference<Avatar> retrieve(Hash avatarHash) throws IOException {
		Avatar avatar = LRU_CACHE.get(avatarHash);

		if (avatar == null) {
			try {
				avatar = loadAvatar(avatarHash);
				LRU_CACHE.put(avatarHash, avatar);
			} catch (IllegalArgumentException e) {
				Log.w(TAG, "Treating undecodable avatar as missing.", e);
			} catch (NotFoundException e) {
				// Ignore.
			}
		}

		return new SimpleHashReference<Avatar>(avatarHash, avatar);
	}

	private Avatar loadAvatar(Hash avatarHash) throws NotFoundException, IllegalArgumentException,
			IOException {
		Content content = mContentManager.retrieve(avatarHash);
		return new Avatar(content.getData(), content.getMimeType());
	}
}
