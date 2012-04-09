package org.whispercomm.shout.network;

import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.User;

public class NetwrokUser implements User {
	
	String username;
	ECPublicKey publicKey;
	
	public NetwrokUser(){
		super();
	}
	
	public NetwrokUser(String username, ECPublicKey publicKey) {
		this.username = username;
		this.publicKey = publicKey;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public ECPublicKey getPublicKey() {
		return this.publicKey;
	}

}
