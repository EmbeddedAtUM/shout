
package org.whispercomm.shout.id;

import org.whispercomm.shout.Me;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.ECPrivateKey;
import org.whispercomm.shout.crypto.ECPublicKey;

public class MeImpl implements Me {

	private ECPublicKey publicKey;
	private ECPrivateKey privateKey;

	private String username;

	public MeImpl(String username, ECPublicKey publicKey, ECPrivateKey privateKey) {
		this.username = username;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public MeImpl(String username, ECKeyPair keyPair) {
		this(username, keyPair.getPublicKey(), keyPair.getPrivateKey());
	}

	@Override
	public ECPublicKey getPublicKey() {
		return this.publicKey;
	}

	@Override
	public ECPrivateKey getPrivateKey() {
		return this.privateKey;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

}
