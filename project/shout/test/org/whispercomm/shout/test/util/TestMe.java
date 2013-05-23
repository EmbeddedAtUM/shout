
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.Avatar;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.crypto.ECKeyPair;
import org.whispercomm.shout.crypto.ECPrivateKey;
import org.whispercomm.shout.crypto.ECPublicKey;

public class TestMe implements Me {

	public String username;
	public ECPublicKey publicKey;
	public ECPrivateKey privateKey;
	public HashReference<Avatar> avatar;
	public int color;
	public int userCount;

	public TestMe(String username, ECPublicKey publicKey, ECPrivateKey privateKey,
			HashReference<Avatar> avatar) {
		this.username = username;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.avatar = avatar;
	}

	public TestMe(String username, ECKeyPair keyPair, HashReference<Avatar> avatar) {
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
	public HashReference<Avatar> getAvatar() {
		return avatar;
	}
	

}
