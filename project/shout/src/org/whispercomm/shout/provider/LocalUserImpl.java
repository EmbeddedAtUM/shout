
package org.whispercomm.shout.provider;

import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.id.SignatureUtility;

import android.content.Context;
import android.util.Base64;

public class LocalUserImpl implements LocalUser {

	@SuppressWarnings("unused")
	private Context context;

	private String username;
	private byte[] keyBytes;

	public LocalUserImpl(Context context, String username, String encodedKey) {
		this.username = username;
		this.keyBytes = Base64.decode(encodedKey, Base64.DEFAULT);
		this.context = context;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return SignatureUtility.getPublicKeyFromBytes(keyBytes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(keyBytes);
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
		if (!Arrays.equals(keyBytes, other.keyBytes))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
