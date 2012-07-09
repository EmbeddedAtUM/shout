
package org.whispercomm.shout.provider;

import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.LocalUser;

import android.content.Context;

public class LazyLocalUserImpl implements LocalUser {

	private boolean loaded = false;

	private Context context;
	private int id;
	private String username = null;
	private ECPublicKey key = null;

	public LazyLocalUserImpl(Context context, int databaseId) {
		this.context = context;
		this.id = databaseId;
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
		if (!loaded) {
			loadSelf();
		}
		return key;
	}

	@Override
	public int getDatabaseId() {
		return id;
	}

	private void loadSelf() {
		LocalUser self = ShoutProviderContract.retrieveUserById(context, id);
		username = self.getUsername();
		key = self.getPublicKey();
		loaded = true;
	}

}
