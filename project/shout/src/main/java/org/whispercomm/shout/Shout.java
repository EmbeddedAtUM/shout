package org.whispercomm.shout;

import org.joda.time.DateTime;

public class Shout {

	/**
	 * Holds the newest shout id. Used for creating IDs for new shouts.
	 */
	static long idCount = 0;

	/**
	 * The time when this shout was received
	 */
	DateTime date;

	/**
	 * The message contained by the Shout
	 */
	String content;

	/**
	 * Unique ID of shout
	 */
	long id;

	public Shout(String content, DateTime date) {
		this.content = content;
		this.date = date;
		this.id = idCount++;
	}

	/**
	 * Returns the message contained by this Shout
	 * 
	 * @return message contained by this Shout
	 */
	String getContent() {
		return content;
	}

	long getID() {
		return id;
	}

	/**
	 * Returns {@link DateTime} object representing the time since the shout has
	 * been received
	 * 
	 * @return {@link DateTime} object representing the time since the shout has
	 *         been received
	 */
	DateTime getTimePassed() {
		return new DateTime(DateTime.now().getMillis() - date.getMillis());
	}
}
