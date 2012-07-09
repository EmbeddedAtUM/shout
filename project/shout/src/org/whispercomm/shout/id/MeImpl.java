
package org.whispercomm.shout.id;

import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.Me;

public class MeImpl implements Me {

	private int id;
	private KeyPair keyPair;
	private String username;

	public MeImpl(int id, String username, KeyPair keyPair) {
		this.id = id;
		this.username = username;
		this.keyPair = keyPair;
	}

	public MeImpl(LocalUser user, KeyPair keyPair) {
		this.id = user.getDatabaseId();
		this.username = user.getUsername();
		this.keyPair = keyPair;
	}

	@Override
	public int getDatabaseId() {
		return id;
	}

	@Override
	public KeyPair getKeyPair() {
		return this.keyPair;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		ECPublicKey ecPubKey = (ECPublicKey) keyPair.getPublic();
		return ecPubKey;
	}

}
