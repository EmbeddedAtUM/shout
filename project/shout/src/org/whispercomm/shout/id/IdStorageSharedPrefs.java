package org.whispercomm.shout.id;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.whispercomm.shout.SimpleUser;
import org.whispercomm.shout.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.util.Log;

/**
 * Use SharedPrefences to implement identity storage.
 * 
 * @author Yue Liu
 * 
 */
class IdStorageSharedPrefs implements IdStorage {

	static final String SHARED_PREFS = "shout_user_keys";
	static final int SHARED_PREFS_ACESS_MODE = 0;
	static final String USER_NAME = "user_name";
	static final String USER_PUB_KEY = "user_pub_key";
	static final String USER_PRIV_KEY = "user_priv_key";
	private static final String TAG = IdStorageSharedPrefs.class
			.getSimpleName();

	SharedPreferences sharedPrefs;

	public IdStorageSharedPrefs(Context context) {
		this.sharedPrefs = context.getSharedPreferences(SHARED_PREFS,
				SHARED_PREFS_ACESS_MODE);
	}

	@Override
	public void updateKeyPair(KeyPair kpA) {
		byte[] pubKeyBytes = kpA.getPublic().getEncoded();
		String pubStr = Base64.encodeToString(pubKeyBytes, 0,
				pubKeyBytes.length, Base64.DEFAULT);
		byte[] privKeyBytes = kpA.getPrivate().getEncoded();
		String privStr = Base64.encodeToString(privKeyBytes, 0,
				privKeyBytes.length, Base64.DEFAULT);
		// store key pair in sharedPrefs
		SharedPreferences.Editor prefsEditor = sharedPrefs.edit();

		prefsEditor.putString(USER_PUB_KEY, pubStr);
		prefsEditor.putString(USER_PRIV_KEY, privStr);
		prefsEditor.commit();
	}

	/**
	 * @return the current user's public key from the sharedPrefs
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws UserNotInitiatedException
	 */
	@Override
	public ECPublicKey getPublicKey() {
		try {
			String pubKeyString = sharedPrefs.getString(USER_PUB_KEY, null);
			if (pubKeyString == null)
				return null;
			byte[] pubKeyBytes = Base64.decode(pubKeyString, Base64.DEFAULT);

			KeyFactory kf = KeyFactory.getInstance("ECDSA", "SC");
			X509EncodedKeySpec x509ks = new X509EncodedKeySpec(pubKeyBytes);
			ECPublicKey pubKey;
			pubKey = (ECPublicKey) kf.generatePublic(x509ks);
			return pubKey;
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return the current user's private key from the sharedPrefs
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws UserNotInitiatedException
	 */
	@Override
	public ECPrivateKey getPrivateKey() {
		try {
			String privKeyString = sharedPrefs.getString(USER_PRIV_KEY, null);
			if (privKeyString == null)
				return null;
			byte[] privKeyBytes = Base64.decode(privKeyString, Base64.DEFAULT);

			KeyFactory kf = KeyFactory.getInstance("ECDSA", "SC");
			PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(privKeyBytes);
			ECPrivateKey privKey = (ECPrivateKey) kf.generatePrivate(p8ks);

			return privKey;
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}

	/**
	 * Allow UI to create or update user name.
	 * 
	 * @param userName
	 */
	@Override
	public void updateUserName(String userName) {
		// TODO for now just replace the old user name
		Editor editor = sharedPrefs.edit();
		editor.putString(USER_NAME, userName);
		editor.commit();
	}

	/**
	 * @return information of current user in form of User object.
	 * 
	 */
	@Override
	public User getUser() {
		String userName = sharedPrefs.getString(USER_NAME, null);
		if (userName == null) {
			return null;
		}
		ECPublicKey pubKey = getPublicKey();
		User sender = new SimpleUser(userName, pubKey);
		return sender;
	}

	@Override
	public void clear() {
		sharedPrefs.edit().clear();
		sharedPrefs.edit().commit();
	}
}
