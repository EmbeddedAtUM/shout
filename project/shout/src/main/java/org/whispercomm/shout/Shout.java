package org.whispercomm.shout;

import org.joda.time.DateTime;

public interface Shout {

	public static final String SHOUT_ID = "_ID";
	public static final String SHOUT_SENDER_ID = "User_ID";
	public static final String SHOUT_CONTENT = "Content";
	public static final String SHOUT_ORIGINAL_ID = "Original_ID";

	public long getId();

	public long getOriginalShoutId();

	public long getSenderId();

	public Shout getOriginalShout(long id);

	public String getContent();

	public String getOriginalSenderName();

	public DateTime getTimestamp();
}