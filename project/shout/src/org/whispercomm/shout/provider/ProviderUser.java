package org.whispercomm.shout.provider;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.whispercomm.shout.User;
import org.whispercomm.shout.id.SignatureUtility;

import android.util.Base64;

public class ProviderUser implements User {

	private String username;
	private ECPublicKey key;
	
	public ProviderUser(String username, String encodedPublicKey) {
		this.username = username;
		try {
			byte[] byteKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
			this.key = SignatureUtility.getPublicKeyFromBytes(byteKey);
			// TODO Fix these exceptions
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
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
