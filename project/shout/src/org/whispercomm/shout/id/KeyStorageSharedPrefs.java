
package org.whispercomm.shout.id;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

public class KeyStorageSharedPrefs implements KeyStorage {

	private SharedPreferences sharedPrefs;

	private static final String PREFS_NAME = "shout_key_storage";

	private static final String KEY_PUBLIC = "base64_public_key";
	private static final String KEY_PRIVATE = "base64_private_key";
	private static final String KEY_ID = "database_user_id";

	public KeyStorageSharedPrefs(Context context) {
		this.sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public void writeKeyPair(int userId, KeyPair keyPair) {
		if (userId < 1) {
			return;
		}
		byte[] publicKey = keyPair.getPublic().getEncoded();
		byte[] privateKey = keyPair.getPrivate().getEncoded();
		String encodedPublicKey = Base64.encodeToString(publicKey, Base64.DEFAULT);
		String encodedPrivateKey = Base64.encodeToString(privateKey, Base64.DEFAULT);
		Editor editor = sharedPrefs.edit();
		editor.putString(KEY_PUBLIC, encodedPublicKey);
		editor.putString(KEY_PRIVATE, encodedPrivateKey);
		editor.putInt(KEY_ID, userId);
		editor.commit();
	}

	@Override
	public KeyPair readKeyPair() {
		if (isEmpty()) {
			return null;
		}
		String encodedPublicKey = sharedPrefs.getString(KEY_PUBLIC, null);
		String encodedPrivateKey = sharedPrefs.getString(KEY_PRIVATE, null);
		byte[] publicKeyBytes = Base64.decode(encodedPublicKey, Base64.DEFAULT);
		byte[] privateKeyBytes = Base64.decode(encodedPrivateKey, Base64.DEFAULT);
		ECPublicKey publicKey = SignatureUtility.getPublicKeyFromBytes(publicKeyBytes);
		ECPrivateKey privateKey = SignatureUtility.getPrivateKeyFromBytes(privateKeyBytes);
		if (publicKey == null || privateKey == null) {
			return null;
		}
		return new KeyPair(publicKey, privateKey);
	}

	@Override
	public int getId() {
		int id = sharedPrefs.getInt(KEY_ID, -1);
		return id;
	}

	@Override
	public boolean isEmpty() {
		return getId() == -1;
	}

}
