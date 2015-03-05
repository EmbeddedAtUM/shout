
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.ECPublicKey;

public class TestUser implements User {

	public String username;
	public ECPublicKey ecPubKey;
	public HashReference<ShoutImage> avatar;

	public TestUser(String username, ECPublicKey ecPubKey, HashReference<ShoutImage> avatar) {
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
	public HashReference<ShoutImage> getAvatar() {
		return avatar;
	}
}
