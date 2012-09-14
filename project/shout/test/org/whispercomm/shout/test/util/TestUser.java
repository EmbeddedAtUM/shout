
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.ECPublicKey;

public class TestUser implements User {

	public String username;
	public ECPublicKey ecPubKey;

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
