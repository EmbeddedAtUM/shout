
package org.whispercomm.shout.id;

import java.io.IOException;

import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.SimpleHashReference;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.content.ShoutImageStorage;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.KeyGenerator;
import org.whispercomm.shout.util.Validators;

import android.content.Context;

public class IdManager {
	public static final String TAG = IdManager.class.getSimpleName();

	private KeyStorage keyStorage;

	private KeyGenerator keyGenerator;

	private ShoutImageStorage avatarStorage;

	public IdManager(Context context) {
		this(new KeyStorageSharedPrefs(context), new ShoutImageStorage(new ContentManager(context)));
	}

	public IdManager(KeyStorage keyStorage, ShoutImageStorage avatarStorage) {
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

	public void setAvatar(ShoutImage avatar) throws IOException {
		HashReference<ShoutImage> ref = avatarStorage.store(avatar);
		keyStorage.writeAvatarHash(ref.getHash());
	}

	public Me getMe() throws UserNotInitiatedException {
		String username = keyStorage.readUsername();
		ECKeyPair keyPair = keyStorage.readKeyPair();
		HashReference<ShoutImage> avatarRef = new SimpleHashReference<ShoutImage>(
				keyStorage.readAvatarHash());
		return new MeImpl(username, keyPair, avatarRef);
	}

	public boolean userIsNotSet() {
		return keyStorage.isEmpty();
	}

}
