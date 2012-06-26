package org.whispercomm.shout;


/**
 * A Shout message.
 * 
 * @author David Adrian
 * 
 */
public interface Shout extends UnsignedShout {

	/**
	 * Get the signature for this specific stand-alone Shout without parents.
	 * 
	 * @return The ECC signature for this Shout object
	 */
	public byte[] getSignature();

}