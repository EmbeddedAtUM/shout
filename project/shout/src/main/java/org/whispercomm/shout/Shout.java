package org.whispercomm.shout;

import org.joda.time.DateTime;

public class Shout {

	/**
	 * Holds the newest shout id. Used for creating IDs for new shouts.
	 */
	static long idCount = 0;

	/**
	 * The username of the original sender of the shout
	 */
	String origSender;

	/**
	 * The username of the sender
	 */
	String sender;

	/**
	 * The message contained by the Shout
	 */
	String content;

	/**
	 * The time when this shout was received
	 */
	DateTime date;

	/**
	 * Unique ID of shout
	 */
	long id;

	public Shout(String origSender, String sender, String content, DateTime date) {
		this.origSender = origSender;
		this.sender = sender;
		this.content = content;
		this.date = date;
		this.id = idCount++;
	}

	/**
	 * Gets the original sender of this shout
	 * 
	 * @return the username of the original sender
	 */
	String getOrigSender() {
		return origSender;
	}

	/**
	 * Gets the sender of this shout
	 * 
	 * @return the username of the sender of this shout
	 */
	String getSender() {
		return sender;
	}

	/**
	 * Gets the message contained by this Shout
	 * 
	 * @return message contained by this Shout
	 */
	String getContent() {
		return content;
	}

	/**
	 * Gets the ID of this shout
	 * 
	 * @return ID of this shout
	 */
	long getID() {
		return id;
	}

	/**
	 * Gets the time passed since the message has been received and converts it
	 * to a message using the correct units for time.
	 * 
	 * @return a message giving the time passed in the most accurate units
	 */

	
	String getAgeMessage() {
		long timePassed;
		String unit;

		// Get the time since the message was received
		DateTime time = new DateTime(DateTime.now().getMillis()
				- date.getMillis());

		// Convert the time passed to a message
		if (time.isAfter(60 * 1000)) {
			timePassed = time.getMinuteOfHour();
			unit = "minute";
		} else if (time.isAfter(60 * 60 * 1000)) {
			timePassed = time.getHourOfDay();
			unit = "hour";
		} else if (time.isAfter(60 * 60 * 24 * 1000)) {
			timePassed = time.getDayOfWeek();
			unit = "day";
		} else {
			timePassed = time.getSecondOfMinute();
			unit = "second";
		}
		
		return String.format("%d %s%s ago.", timePassed, unit,
				timePassed == 1 ? "" : "s");
	}

}
