package org.whispercomm.shout;

import org.joda.time.DateTime;

public interface Shout {

	public static final String SHOUT_ID = "_ID";
	public static final String SHOUT_SENDER_ID = "User_ID";
	public static final String SHOUT_CONTENT = "Content";
	public static final String SHOUT_ORIGINAL_ID = "Original_ID";
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
	public DateTime getTimestamp();
}