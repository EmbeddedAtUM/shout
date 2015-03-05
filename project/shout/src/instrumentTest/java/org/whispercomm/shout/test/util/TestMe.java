
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.ECPrivateKey;
import org.whispercomm.shout.crypto.ECPublicKey;

public class TestMe implements Me {

	public String username;
	public ECPublicKey publicKey;
	public ECPrivateKey privateKey;
	public HashReference<ShoutImage> avatar;

	public TestMe(String username, ECPublicKey publicKey, ECPrivateKey privateKey,
			HashReference<ShoutImage> avatar) {
		this.username = username;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.avatar = avatar;
	}

	public TestMe(String username, ECKeyPair keyPair, HashReference<ShoutImage> avatar) {
		this(username, keyPair.getPublicKey(), keyPair.getPrivateKey(), avatar);
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

	@Override
	public HashReference<ShoutImage> getAvatar() {
		return avatar;
	}

}
