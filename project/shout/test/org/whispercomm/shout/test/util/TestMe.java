
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.Me;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.ECPrivateKey;
import org.whispercomm.shout.crypto.ECPublicKey;

public class TestMe implements Me {

	public String username;
	public ECPublicKey publicKey;
	public ECPrivateKey privateKey;

	public TestMe(String username, ECPublicKey publicKey, ECPrivateKey privateKey) {
		this.username = username;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public TestMe(String username, ECKeyPair keyPair) {
		this(username, keyPair.getPublicKey(), keyPair.getPrivateKey());
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return publicKey;
	}

	@Override
	public ECPrivateKey getPrivateKey() {
		return privateKey;
	}

}
