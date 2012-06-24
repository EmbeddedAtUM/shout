package org.whispercomm.shout;

import org.joda.time.DateTime;

public interface Shout {

	/**
	 * String encode/decode character set
	 */
	public static final String CHARSET_NAME = "UTF-8";

	/**
	 * This hash is a full hash of all included Shouts that were sent with this
	 * message as re-shouts or originals with comment.
	 * 
	 * @return The hash for the entire network packet this Shout arrived on.
	 */
	public byte[] getHash();

	/**
	 * @return The sender of this Shout
	 */
	public User getSender();

	/**
	 * @return The stand-alone content of this specific Shout
	 */
	public String getMessage();

	/**
	 * @return The sent time on this specific Shout
	 */
	public DateTime getTimestamp();

	/**
	 * Get a Shout object for any referenced / included Shout within this Shout
	 * as a stand-alone Shout object.
	 * 
	 * @return {@code null} if no original shout
	 */
	public Shout getParent();

	/**
	 * Get the signature for this specific stand-alone Shout without parents.
	 * 
	 * @return The ECC signature for this Shout object
	 */
	public byte[] getSignature();

	/**
	 * Returns the type of the shout (SHOUT, RESHOUT, COMMENT, RECOMMENT).
	 * 
	 * @return the type of shout.
	 */
	public ShoutType getType();
}