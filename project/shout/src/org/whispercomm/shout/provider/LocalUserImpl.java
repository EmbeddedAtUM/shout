
package org.whispercomm.shout.provider;

import java.security.spec.InvalidKeySpecException;

import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.KeyGenerator;

import android.content.Context;
import android.util.Base64;

public class LocalUserImpl implements LocalUser {

	@SuppressWarnings("unused")
	private Context context;

	private String username;
	private ECPublicKey publicKey;

	public LocalUserImpl(Context context, String username, String encodedKey) {
		this.username = username;
		try {
			this.publicKey = KeyGenerator.generatePublic(Base64.decode(encodedKey, Base64.DEFAULT));
		} catch (InvalidKeySpecException e) {
			// TODO: Figure out what to do about this
			throw new RuntimeException(e);
		}
		this.context = context;
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
