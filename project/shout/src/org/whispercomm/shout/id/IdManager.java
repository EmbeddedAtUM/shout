
package org.whispercomm.shout.id;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.SimpleUser;
import org.whispercomm.shout.User;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.util.Log;

public class IdManager {

	public static final String TAG = IdManager.class.getSimpleName();
	public static final String ECC_PARAMS = "secp256r1";
	public static final String CRYPTO_ALGO = "ECDSA";
	public static final String CRYPTO_PROVIDER = "SC";

	private KeyStorage keyStorage;
	private Context context;
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public IdManager(Context context) {
		this.keyStorage = new KeyStorageSharedPrefs(context);
		this.context = context;
	}

	public IdManager(Context context, KeyStorage keyStorage) {
		this.keyStorage = keyStorage;
		this.context = context;
	}

	public void resetUser(String newUsername) {
		KeyPair oldKeyPair = keyStorage.readKeyPair();
		int oldId = keyStorage.getId();
		try {
			// Generate a new key pair
			ECGenParameterSpec ecParamSpec = new ECGenParameterSpec(ECC_PARAMS);
			KeyPairGenerator kpg;
			kpg = KeyPairGenerator.getInstance(CRYPTO_ALGO,
					CRYPTO_PROVIDER);
			kpg.initialize(ecParamSpec);
			KeyPair newKeyPair = kpg.generateKeyPair();
			ECPublicKey publicKey = (ECPublicKey) newKeyPair.getPublic();
			// Make a new user with the new public key and username
			User newUser = new SimpleUser(newUsername, publicKey);
			int id = ShoutProviderContract.storeUser(context, newUser);
			// Write the new one
			if (id > 0) {
				// Successful
				keyStorage.writeKeyPair(id, newKeyPair);
				return;
			}
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(TAG, e.getMessage());
		}
		// Some sort of failure, reset to old state
		if (! keyStorage.isEmpty()) {
			keyStorage.writeKeyPair(oldId, oldKeyPair);
		}
		return;
	}

	public Me getMe() {
		if (keyStorage.isEmpty()) {
			return null;
		}
		int id = keyStorage.getId();
		LocalUser user = ShoutProviderContract.retrieveUserById(context, id);
		KeyPair keyPair = keyStorage.readKeyPair();
		return new MeImpl(user, keyPair);
	}

	public boolean userIsNotSet() {
		return keyStorage.isEmpty();
	}
}
