
package org.whispercomm.shout.content;

import java.io.IOException;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.errors.NotFoundException;

import android.util.Log;

public class AvatarStorage {
	private static final String TAG = AvatarStorage.class.getSimpleName();

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
		Content content;
		try {
			content = mContentManager.retrieve(avatarHash);
		} catch (NotFoundException e) {
			return new SimpleHashReference<Avatar>(avatarHash, null);
		}

		Avatar avatar;
		try {
			avatar = new Avatar(content.getData(), content.getMimeType());
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "Treating undecodable avatar as missing.", e);
			return new SimpleHashReference<Avatar>(avatarHash, null);
		}

		return new SimpleHashReference<Avatar>(avatarHash, avatar);
	}
}
