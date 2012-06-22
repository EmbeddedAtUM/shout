package org.whispercomm.shout.provider;

import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.User;
import org.whispercomm.shout.id.SignatureUtility;

import android.util.Base64;

/**
 * An implementation of {@link User} backed by entries in the Shout
 * ContentProvider database.
 * 
 * @author David Adrian
 * @author David R. Bild
 * 
 */
public class ProviderUser implements User {

	private String username;
	private ECPublicKey key;

	/**
	 * Constructs a new ProviderUser for the given username and encoded EC
	 * public key.
	 * 
	 * @param username
	 * @param encodedPublicKey
	 */
	// TODO If such a User is really backed by an entry in the ContentProvider,
	// this constructor should not be publicly visible. Maybe package-private?
	public ProviderUser(String username, String encodedPublicKey) {
		this.username = username;
		byte[] byteKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
		this.key = SignatureUtility.getPublicKeyFromBytes(byteKey);
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return this.key;
	}

}
