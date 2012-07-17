
package org.whispercomm.shout.provider;

import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.id.SignatureUtility;

import android.content.Context;
import android.util.Base64;

public class LazyLocalUserImpl implements LocalUser {

	private boolean loaded = false;

	private Context context;
	private String username = null;
	private ECPublicKey key = null;

	public LazyLocalUserImpl(Context context, String encodedAuthor) {
		this.context = context;
		byte[] keyBytes = Base64.decode(encodedAuthor, Base64.DEFAULT);
		this.key = SignatureUtility.getPublicKeyFromBytes(keyBytes);
	}

	@Override
	public String getUsername() {
		if (!loaded) {
			loadSelf();
		}
		return username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return key;
	}

	private void loadSelf() {
		LocalUser self = ShoutProviderContract.retrieveUserByKey(context, key);
		username = self.getUsername();
		loaded = true;
	}

	@Override
	public int hashCode() {
		if (!loaded) {
			loadSelf();
		}

		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!loaded) {
			loadSelf();
		}

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LazyLocalUserImpl other = (LazyLocalUserImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
