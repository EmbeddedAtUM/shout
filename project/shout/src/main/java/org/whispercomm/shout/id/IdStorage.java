package org.whispercomm.shout.id;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.whispercomm.shout.User;

/**
 * Package private interface for storing/retrieving identity information, e.g.,
 * user name and key pair.
 * 
 * @author Yue Liu
 * 
 */
interface IdStorage {

	public void updateKeyPair(KeyPair keyPair);

	public ECPublicKey getPublicKey() throws Exception;

	public ECPrivateKey getPrivateKey() throws Exception;

	public void updateUserName(String userName);

	public User getUser() throws Exception;
	
	public void clear();

}
