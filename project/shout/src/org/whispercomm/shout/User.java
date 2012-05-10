package org.whispercomm.shout;

import java.security.interfaces.ECPublicKey;


public interface User {
	
	public String getUsername();
	public ECPublicKey getPublicKey();

}
