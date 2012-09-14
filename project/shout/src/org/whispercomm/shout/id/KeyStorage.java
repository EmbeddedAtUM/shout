
package org.whispercomm.shout.id;

import org.whispercomm.shout.crypto.ECKeyPair;

/**
 * Package private interface for storing/retrieving identity information, e.g.,
 * user name and key pair.
 * 
 * @author David Adrian
 * @author Yue Liu
 */
interface KeyStorage {

	/**
	 * Write a username, key-pair tuple representing the user of this
	 * application.
	 * 
	 * @param userId
	 * @param keyPair
	 * @return {@code true} if write was successful
	 */
	public boolean writeMe(String username, ECKeyPair keyPair);

	/**
	 * Read the key pair
	 * 
	 * @return {@code null} on failure
	 */
	public ECKeyPair readKeyPair() throws UserNotInitiatedException;

	/**
	 * Read the username from storage.
	 * 
	 * @return {@code null} on failure
	 */
	public String readUsername() throws UserNotInitiatedException;

	/**
	 * Check whether any values are stored in this key storage.
	 * 
	 * @return {@code true} if the store is empty.
	 */
	public boolean isEmpty();

}
