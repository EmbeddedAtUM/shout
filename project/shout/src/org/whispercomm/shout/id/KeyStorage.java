package org.whispercomm.shout.id;

import java.security.KeyPair;

/**
 * Package private interface for storing/retrieving identity information, e.g.,
 * user name and key pair.
 * 
 * @author Yue Liu
 * 
 */
interface KeyStorage {

	public void writeKeyPair(KeyPair keyPair);
	
	public KeyPair readKeyPair();
	
	public void writeId(int id);
	
	public int getId();
	
	public boolean isEmpty();
	
}
