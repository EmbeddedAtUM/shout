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
	
	public ProviderUser(String username, String publicKey) {
		this.username = username;
		try {
			byte[] byteKey = Base64.decode(publicKey, Base64.DEFAULT);
			this.key = SignatureUtility.getPublicKeyFromBytes(byteKey);
			// TODO Fix these exceptions
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
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
