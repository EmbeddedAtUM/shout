
package org.whispercomm.shout.test.util;

import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.Me;

public class TestMe implements Me {

	public String username;
	public KeyPair keyPair;
	public int id = 1;

	public TestMe(String username) {
		this.keyPair = TestFactory.genKeyPair();
	}

	@Override
	public int getDatabaseId() {
		return id;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return (ECPublicKey) keyPair.getPublic();
	}

	@Override
	public KeyPair getKeyPair() {
		return keyPair;
	}

}
