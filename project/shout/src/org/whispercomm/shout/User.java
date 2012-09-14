
package org.whispercomm.shout;

import org.whispercomm.shout.crypto.ECPublicKey;

/**
 * The public representation of a Shout user.
 * 
 * @author David Adrian
 * @author David R. Bild
 */
public interface User {

	/**
	 * The public alias for the Shout user. This name is not guaranteed to be
	 * globally unique and must be no more than 40 bytes in UTF-8 encoding.
	 * 
	 * @return the public alias
	 */
	public String getUsername();

	/**
	 * The elliptic curve public key for the Shout user. This key serves as the
	 * globally unique identifier for the user. Whoever is in possession of the
	 * corresponding private key is the "owner" of this identity.
	 * 
	 * @return the public key
	 */
	public ECPublicKey getPublicKey();

}
