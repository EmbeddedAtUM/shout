
package org.whispercomm.shout.provider;

import java.security.interfaces.ECPublicKey;

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

}
