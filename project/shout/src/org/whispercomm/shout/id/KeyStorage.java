
package org.whispercomm.shout.id;

import java.security.KeyPair;

/**
 * Package private interface for storing/retrieving identity information, e.g.,
 * user name and key pair.
 * 
 * @author David Adrian
 * @author Yue Liu
 */
interface KeyStorage {

	/**
	 * Write a key-pair, user ID tuple representing the user of this
	 * application.
	 * 
	 * @param userId
	 * @param keyPair
	 */
	public void writeKeyPair(int userId, KeyPair keyPair);

	/**
	 * Read the key pair
	 * 
	 * @return {@code null} on failure
	 */
	public KeyPair readKeyPair();

	/**
	 * Read the user ID
	 * 
	 * @return {@code -1} on failure
	 */
	public int getId();

	/**
	 * Check whether any values are stored in this key storage.
	 * 
	 * @return {@code true} if the store is empty.
	 */
	public boolean isEmpty();

}
