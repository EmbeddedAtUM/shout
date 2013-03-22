
package org.whispercomm.shout.provider;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.content.AvatarStorage;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.errors.InvalidEncodingException;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

public class LocalUserImpl implements LocalUser {
	private static final String TAG = LocalUserImpl.class.getSimpleName();

	private Context context;

	private String username;
	private ECPublicKey publicKey;
	private HashReference<Avatar> avatar;

	public LocalUserImpl(Context context, String username, String encodedKey,
			String encodedAvatarHash) {
		this.context = context.getApplicationContext();

		this.username = username;
		try {
			this.publicKey = KeyGenerator.generatePublic(Base64.decode(encodedKey, Base64.DEFAULT));
		} catch (InvalidKeySpecException e) {
			// TODO: Figure out what to do about this
			throw new RuntimeException(e);
		}
		try {
			this.avatar = new SimpleHashReference<Avatar>(new Hash(Base64.decode(encodedAvatarHash,
					Base64.DEFAULT)));
		} catch (InvalidEncodingException e) {
			// TODO: Figure out what to do about this
			throw e;
		}
	}

	private void updateAvatar(Hash avatarHash) {
		AvatarStorage storage = (AvatarStorage) context
				.getSystemService(AvatarStorage.SHOUT_AVATAR_SERVICE);
		try {
			avatar = storage.retrieve(avatarHash);
		} catch (IOException e) {
			Log.w(TAG, "Unable to retrieve avatar.  Treating as missing.", e);
			avatar = new SimpleHashReference<Avatar>(avatarHash);
		}
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return publicKey;
	}

	@Override
	public HashReference<Avatar> getAvatar() {
		if (!avatar.isAvailable()) {
			updateAvatar(avatar.getHash());
		}
		return avatar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalUserImpl other = (LocalUserImpl) obj;
		if (publicKey == null) {
			if (other.publicKey != null)
				return false;
		} else if (!publicKey.equals(other.publicKey))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
