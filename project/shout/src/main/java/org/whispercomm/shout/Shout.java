package org.whispercomm.shout;

import org.joda.time.DateTime;

public interface Shout {
	
	/**
	 * String encode/decode charset
	 */
	public static String CHARSET_NAME = "UTF-8";

	public byte[] getHash();
	
	public User getSender();

	public String getContent();

	public DateTime getTimestamp();
	
	/**
	 * @return null if no original shout
	 */
	public Shout getOriginalShout();
	
	public byte[] getSignature();
}