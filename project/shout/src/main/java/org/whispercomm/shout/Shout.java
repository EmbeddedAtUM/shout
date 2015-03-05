
package org.whispercomm.shout;

import org.whispercomm.shout.crypto.DsaSignature;

/**
 * A Shout message.
 * 
 * @author David Adrian
 */
public interface Shout extends UnsignedShout {

	/**
	 * Get the version of the canonical form used for signing.
	 * 
	 * @return the version of the canonical form used for signing
	 */
	public int getVersion();

	/**
	 * Get the signature for this specific stand-alone Shout without parents.
	 * 
	 * @return The ECC signature for this Shout object
	 */
	public DsaSignature getSignature();

	/**
	 * This hash is a full hash of all included Shouts that were sent with this
	 * message as re-shouts or originals with comment.
	 * 
	 * @return The hash for the entire network packet this Shout arrived on.
	 */
	public Hash getHash();

}
