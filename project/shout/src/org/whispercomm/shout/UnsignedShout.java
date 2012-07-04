package org.whispercomm.shout;

import org.joda.time.DateTime;

/**
 * An interface to represent a Shout with no signature
 * 
 * @author David Adrian 
 *
 */
public interface UnsignedShout {

	/**
	 * String encode/decode character set
	 */
	public static final String CHARSET_NAME = "UTF-8";


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
	 * Returns the type of the shout (SHOUT, RESHOUT, COMMENT, RECOMMENT).
	 * 
	 * @return the type of shout.
	 */
	public ShoutType getType();
}
