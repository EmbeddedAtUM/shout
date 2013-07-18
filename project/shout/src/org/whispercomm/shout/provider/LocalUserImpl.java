
package org.whispercomm.shout.provider;

import java.security.spec.InvalidKeySpecException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.errors.InvalidEncodingException;

import android.util.Base64;

public class LocalUserImpl implements LocalUser {

	private String username;
	private ECPublicKey publicKey;
	private String encodedPublicKey;
	private HashReference<ShoutImage> avatar;

	public LocalUserImpl(String username, String encodedPublicKey,
			String encodedAvatarHash) {
		this.username = username;
		this.encodedPublicKey = encodedPublicKey;
		this.publicKey = null; // Delay expensive decoding until first use
		try {
			this.avatar = new SimpleHashReference<ShoutImage>(new Hash(Base64.decode(
					encodedAvatarHash,
					Base64.DEFAULT)));
		} catch (InvalidEncodingException e) {
			// TODO: Figure out what to do about this
			throw e;
		}
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	/**
	 * Ensures that public key has been decoded. Must be called before accessing
	 * the {@link publicKey} field.
	 */
	private void preparePublicKey() {
		if (null != publicKey)
			return;

		try {
			publicKey = KeyGenerator.generatePublic(Base64.decode(encodedPublicKey,
					Base64.DEFAULT));
		} catch (InvalidKeySpecException e) {
			// TODO: Figure out what to do about this
			throw new RuntimeException(e);
		}
	}

	@Override
	public ECPublicKey getPublicKey() {
		preparePublicKey();
		return publicKey;
	}

	@Override
	public HashReference<ShoutImage> getAvatar() {
		return avatar;
	}

	@Override
	public int hashCode() {
		preparePublicKey();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		preparePublicKey();
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
