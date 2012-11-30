
package org.whispercomm.shout.id;

import java.io.IOException;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.content.AvatarStorage;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.util.Validators;

import android.content.Context;

public class IdManager {
	public static final String TAG = IdManager.class.getSimpleName();

	private KeyStorage keyStorage;

	private KeyGenerator keyGenerator;

	private AvatarStorage avatarStorage;

	public IdManager(Context context) {
		this(new KeyStorageSharedPrefs(context), new AvatarStorage(new ContentManager(context)));
	}

	public IdManager(KeyStorage keyStorage, AvatarStorage avatarStorage) {
		this.keyStorage = keyStorage;
		this.avatarStorage = avatarStorage;
		this.keyGenerator = new KeyGenerator();
	}

	public void resetUser(String newUsername) throws UserNameInvalidException {
		// Validate the new username
		boolean isValid = Validators.validateUsername(newUsername);
		if (!isValid) {
			throw new UserNameInvalidException();
		}
		newUsername = Validators.removeTrailingSpaces(newUsername);

		// Generate a new key pair if needed
		ECKeyPair keyPair;
		try {
			keyPair = keyStorage.readKeyPair();
		} catch (UserNotInitiatedException e) {
			keyPair = keyGenerator.generateKeyPair();
		}

		// Write the username, key pair tuple
		keyStorage.writeMe(newUsername, keyPair);
		return;
	}

	public void setAvatar(Avatar avatar) throws IOException {
		HashReference<Avatar> ref = avatarStorage.store(avatar);
		keyStorage.writeAvatarHash(ref.getHash());
	}

	public Me getMe() throws UserNotInitiatedException {
		String username = keyStorage.readUsername();
		ECKeyPair keyPair = keyStorage.readKeyPair();
		HashReference<Avatar> avatarRef = new SimpleHashReference<Avatar>(
				keyStorage.readAvatarHash());
		return new MeImpl(username, keyPair, avatarRef);
	}

	public boolean userIsNotSet() {
		return keyStorage.isEmpty();
	}

}
