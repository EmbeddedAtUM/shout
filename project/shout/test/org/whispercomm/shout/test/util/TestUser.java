
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.ECPublicKey;

public class TestUser implements User {

	public String username;
	public ECPublicKey ecPubKey;
	public HashReference<Avatar> avatar;

	public TestUser(String username, ECPublicKey ecPubKey, HashReference<Avatar> avatar) {
		this.username = username;
		this.ecPubKey = ecPubKey;
		this.avatar = avatar;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return ecPubKey;
	}

	@Override
	public HashReference<Avatar> getAvatar() {
		return avatar;
	}
}
