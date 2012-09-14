
package org.whispercomm.shout.id;

import org.whispercomm.shout.Me;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.util.Validators;

import android.content.Context;

public class IdManager {
	public static final String TAG = IdManager.class.getSimpleName();

	private KeyStorage keyStorage;

	private KeyGenerator keyGenerator;

	public IdManager(Context context) {
		this(new KeyStorageSharedPrefs(context));
	}

	public IdManager(KeyStorage keyStorage) {
		this.keyStorage = keyStorage;
		this.keyGenerator = new KeyGenerator();
	}

	public void resetUser(String newUsername) throws UserNameInvalidException {
		// Validate the new username
		boolean isValid = Validators.validateUsername(newUsername);
		if (!isValid) {
			throw new UserNameInvalidException();
		}
		newUsername = Validators.removeTrailingSpaces(newUsername);
		// Generate a new key pair
		ECKeyPair newKeyPair = keyGenerator.generateKeyPair();
		// Write the username, key pair tuple
		keyStorage.writeMe(newUsername, newKeyPair);
		return;
	}

	public Me getMe() throws UserNotInitiatedException {
		String username = keyStorage.readUsername();
		ECKeyPair keyPair = keyStorage.readKeyPair();
		return new MeImpl(username, keyPair);
	}

	public boolean userIsNotSet() {
		return keyStorage.isEmpty();
	}

}
