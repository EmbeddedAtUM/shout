package org.whispercomm.shout;

import java.security.interfaces.ECPublicKey;

public class SimpleUser implements User {

	String username;
	ECPublicKey publicKey;
	
	public SimpleUser(String username, ECPublicKey publickey){
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

}
