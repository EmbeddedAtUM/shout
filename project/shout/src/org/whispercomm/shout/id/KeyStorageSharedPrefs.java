
package org.whispercomm.shout.id;

import java.security.spec.InvalidKeySpecException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.ECPrivateKey;
import org.whispercomm.shout.crypto.ECPublicKey;
import org.whispercomm.shout.crypto.KeyGenerator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

public class KeyStorageSharedPrefs implements KeyStorage {

	private SharedPreferences sharedPrefs;

	private static final String PREFS_NAME = "shout_key_storage";

	private static final String KEY_PUBLIC = "base64_public_key";
	private static final String KEY_PRIVATE = "base64_private_key";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_AVATAR_HASH = "avatar_hash";
	private static final String KEY_VALID = "valid";

	/**
	 * Returned when an actual hash has not been set.
	 */
	private static final String DEFAULT_AVATAR_HASH_STRING = Base64.encodeToString(new byte[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0,
	}, Base64.DEFAULT);

	public KeyStorageSharedPrefs(Context context) {
		this.sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public boolean writeMe(String username, ECKeyPair keyPair) {
		String encodedPublicKey = Base64.encodeToString(
				KeyGenerator.encodePublic(keyPair.getPublicKey()), Base64.DEFAULT);
		String encodedPrivateKey = Base64.encodeToString(
				KeyGenerator.encodePrivate(keyPair.getPrivateKey()), Base64.DEFAULT);

		Editor editor = sharedPrefs.edit();

		editor.putString(KEY_USERNAME, username);
		editor.putString(KEY_PUBLIC, encodedPublicKey);
		editor.putString(KEY_PRIVATE, encodedPrivateKey);
		editor.putBoolean(KEY_VALID, true);

		return editor.commit();
	}

	@Override
	public boolean writeAvatarHash(Hash avatarHash) {
		Editor editor = sharedPrefs.edit();
		editor.putString(KEY_AVATAR_HASH,
				Base64.encodeToString(avatarHash.toByteArray(), Base64.DEFAULT));
		return editor.commit();
	}

	@Override
	public ECKeyPair readKeyPair() throws UserNotInitiatedException {
		if (isEmpty()) {
			throw new UserNotInitiatedException();
		}

		ECPublicKey publicKey;
		try {
			byte[] encodedPublicKey = Base64.decode(sharedPrefs.getString(KEY_PUBLIC, ""),
					Base64.DEFAULT);
			publicKey = KeyGenerator.generatePublic(encodedPublicKey);
		} catch (IllegalArgumentException e) {
			// TODO: Figure out what to do about this
			throw new RuntimeException("Could not decode the stored public key.", e);
		} catch (InvalidKeySpecException e) {
			// TODO: Figure out what to do about this.
			throw new RuntimeException("Could not decode the stored public key.", e);
		}

		ECPrivateKey privateKey;
		try {
			byte[] encodedPrivateKey = Base64.decode(sharedPrefs.getString(KEY_PRIVATE, ""),
					Base64.DEFAULT);
			privateKey = KeyGenerator.generatePrivate(encodedPrivateKey);
		} catch (InvalidKeySpecException e) {
			// TODO: Figure out what to do about this.
			throw new RuntimeException("Could not decode the stored private key.", e);
		} catch (IllegalArgumentException e) {
			// TODO: Figure out what to do about this
			throw new RuntimeException("Could not decode the stored public key.", e);
		}

		return new ECKeyPair(publicKey, privateKey);
	}

	@Override
	public String readUsername() throws UserNotInitiatedException {
		if (isEmpty()) {
			throw new UserNotInitiatedException();
		}
		String username = sharedPrefs.getString(KEY_USERNAME, null);
		return username;
	}

	@Override
	public Hash readAvatarHash() {
		String encoded = sharedPrefs.getString(KEY_AVATAR_HASH, DEFAULT_AVATAR_HASH_STRING);
		return new Hash(Base64.decode(encoded, Base64.DEFAULT));
	}

	@Override
	public boolean isEmpty() {
		return !sharedPrefs.getBoolean(KEY_VALID, false);
	}

}
