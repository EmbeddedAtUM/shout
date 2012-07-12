
package org.whispercomm.shout.id;

import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.Me;

public class MeImpl implements Me {

	private KeyPair keyPair;
	private String username;

	public MeImpl(String username, KeyPair keyPair) {
		this.username = username;
		this.keyPair = keyPair;
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
