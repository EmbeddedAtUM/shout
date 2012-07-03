
package org.whispercomm.shout.test.util;

import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.User;

public class TestUser implements User {

	public String username;
	public ECPublicKey ecPubKey;

	public TestUser(String username) {
		this.username = username;
		this.ecPubKey = TestFactory.genPublicKey();
	}

	public TestUser(String username, ECPublicKey ecPubKey) {
		this.username = username;
		this.ecPubKey = ecPubKey;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return ecPubKey;
	}

}
