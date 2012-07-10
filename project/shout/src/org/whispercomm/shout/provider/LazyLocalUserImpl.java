
package org.whispercomm.shout.provider;

import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.id.SignatureUtility;

import android.content.Context;
import android.util.Base64;

public class LazyLocalUserImpl implements LocalUser {

	private boolean loaded = false;

	private Context context;
	private int id = -1;
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

	@Override
	public int getDatabaseId() {
		if (!loaded) {
			loadSelf();
		}
		return id;
	}

	private void loadSelf() {
		LocalUser self = ShoutProviderContract.retrieveUserByKey(context, key);
		username = self.getUsername();
		id = self.getDatabaseId();
		loaded = true;
	}

}
