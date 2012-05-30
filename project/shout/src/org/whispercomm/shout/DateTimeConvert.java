package org.whispercomm.shout;

import org.joda.time.DateTime;

public class DateTimeConvert {
	public static String dtToString(DateTime dt)
	{
		long timePassed;
		String unit;

		// Get the time since the message was received
		DateTime time = new DateTime(DateTime.now().getMillis()
				- dt.getMillis());

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
