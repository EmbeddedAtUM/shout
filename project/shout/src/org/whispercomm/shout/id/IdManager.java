
package org.whispercomm.shout.id;

import java.security.KeyPair;

import org.whispercomm.shout.Me;
import org.whispercomm.shout.util.Validators;

import android.content.Context;

public class IdManager {

	public static final String TAG = IdManager.class.getSimpleName();
	public static final String ECC_PARAMS = "secp256r1";
	public static final String CRYPTO_ALGO = "ECDSA";
	public static final String CRYPTO_PROVIDER = "SC";

	private KeyStorage keyStorage;

	public IdManager(Context context) {
		this.keyStorage = new KeyStorageSharedPrefs(context);
	}

	public IdManager(KeyStorage keyStorage) {
		this.keyStorage = keyStorage;
	}

	public void resetUser(String newUsername) throws UserNameInvalidException {
		// Validate the new username
		boolean isValid = Validators.validateUsername(newUsername);
		if (!isValid) {
			throw new UserNameInvalidException();
		}
		newUsername = Validators.removeTrailingSpaces(newUsername);
		// Generate a new key pair
		KeyPair newKeyPair = SignatureUtility.generateECKeyPair();
		// Write the username, key pair tuple
		keyStorage.writeMe(newUsername, newKeyPair);
		return;
	}

	public Me getMe() throws UserNotInitiatedException {
		String username = keyStorage.readUsername();
		KeyPair keyPair = keyStorage.readKeyPair();
		return new MeImpl(username, keyPair);
	}

	public boolean userIsNotSet() {
		return keyStorage.isEmpty();
	}

}
