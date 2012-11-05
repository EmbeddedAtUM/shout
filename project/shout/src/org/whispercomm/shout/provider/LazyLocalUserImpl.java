
package org.whispercomm.shout.provider;

import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.crypto.ECPublicKey;

import android.content.Context;

public class LazyLocalUserImpl implements LocalUser {

	private final int id;
	private Context context;
	private LocalUser self;

	public LazyLocalUserImpl(Context context, int userId) {
		this.context = context;
		this.id = userId;
		this.self = null;
	}

	@Override
	public String getUsername() {
		loadSelf();
		return self.getUsername();
	}

	@Override
	public ECPublicKey getPublicKey() {
		loadSelf();
		return self.getPublicKey();
	}

	private void loadSelf() {
		if (self == null)
			self = ShoutProviderContract.retrieveUserById(context, id);
	}

	@Override
	public int hashCode() {
		loadSelf();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPublicKey() == null) ? 0 : getPublicKey().hashCode());
		result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		loadSelf();

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LazyLocalUserImpl other = (LazyLocalUserImpl) obj;
		if (getPublicKey() == null) {
			if (other.getPublicKey() != null)
				return false;
		} else if (!getPublicKey().equals(other.getPublicKey()))
			return false;
		if (getUsername() == null) {
			if (other.getUsername() != null)
				return false;
		} else if (!getUsername().equals(other.getUsername()))
			return false;
		return true;
	}

}
