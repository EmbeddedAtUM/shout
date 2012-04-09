package org.whispercomm.shout;

import java.security.interfaces.ECPublicKey;


public interface User {
	
	public static final String USER_ID = "_ID";
	public static final String USER_NAME = "Username";
	public static final String USER_KEY = "Public Key";
	
	public String getUsername();
	public ECPublicKey getPublicKey();

}
