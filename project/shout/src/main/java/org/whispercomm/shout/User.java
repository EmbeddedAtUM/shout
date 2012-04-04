package org.whispercomm.shout;


public interface User {
	
	public static final String USER_ID = "_ID";
	public static final String USER_NAME = "Username";
	public static final String USER_KEY = "Public Key";
	
	public long getId();
	public String getUsername();
	public String getPublicKey();

}
