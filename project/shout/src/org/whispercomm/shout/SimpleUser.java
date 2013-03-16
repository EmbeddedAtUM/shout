
package org.whispercomm.shout;

import org.whispercomm.shout.crypto.ECPublicKey;

public class SimpleUser implements User {

	String username;
	ECPublicKey publicKey;
	HashReference<ShoutImage> avatar;

	public SimpleUser(String username, ECPublicKey publickey) {
		this.username = username;
		this.publicKey = publickey;
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
	public HashReference<ShoutImage> getAvatar() {
		return avatar;
	}

}
