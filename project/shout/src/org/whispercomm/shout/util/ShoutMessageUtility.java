
package org.whispercomm.shout.util;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.UnsignedShout;

public class ShoutMessageUtility {

	private static final DateTimeFormatter previousYear = DateTimeFormat
			.forPattern("MMM d',' yyyy 'at' h:m a");
	private static final DateTimeFormatter thisYear = DateTimeFormat
			.forPattern("MMM d 'at' h:mm a");
	private static final DateTimeFormatter today = DateTimeFormat.forPattern("'Today at' h:mm a");

	public static ShoutType getShoutType(UnsignedShout shout) {
		if (shout.getParent() == null) {
			return ShoutType.SHOUT;
		} else if (shout.getMessage() != null) {
			return ShoutType.COMMENT;
		} else if (shout.getParent().getParent() != null) {
			return ShoutType.RECOMMENT;
		} else {
			return ShoutType.RESHOUT;
		}
	}

	public static String getDateTimeAge(DateTime dt) {
		long timePassed;
		String unit;
		Duration age = new Duration(dt, null);
		if (dt.isAfterNow()) {
			if (age.getStandardDays() <= -7) {
				timePassed = age.getStandardDays() / 7;
				unit = "week";
			} else if (age.getStandardHours() <= -24) {
				timePassed = age.getStandardDays();
				unit = "day";
			} else if (age.getStandardMinutes() <= -60) {
				timePassed = age.getStandardHours();
				unit = "hour";
			} else if (age.getStandardSeconds() <= -60) {
				timePassed = age.getStandardMinutes();
				unit = "minute";
			} else {
				timePassed = age.getStandardSeconds();
				unit = "second";
			}
		} else {
			if (age.getStandardDays() >= 14) {
				return getReadableDateTime(dt);
			}
			else if (age.getStandardDays() >= 7) {
				timePassed = age.getStandardDays() / 7;
				unit = "week";
			} else if (age.getStandardHours() >= 24) {
				timePassed = age.getStandardDays();
				unit = "day";
			} else if (age.getStandardMinutes() >= 60) {
				timePassed = age.getStandardHours();
				unit = "hour";
			} else if (age.getStandardSeconds() >= 60) {
				timePassed = age.getStandardMinutes();
				unit = "minute";
			} else {
				timePassed = age.getStandardSeconds();
				unit = "second";
			}
		}

		return String.format("%d %s%s ago.", timePassed, unit,
				timePassed == 1 ? "" : "s");
	}

	public static String getCountAsText(int count) {
		switch (count) {
			case 0:
				return "never";
			case 1:
				return "once";
			case 2:
				return "twice";
			default:
				return String.format("%d times", count);
		}
	}

	public static String getReadableDateTime(DateTime time) {
		DateTime now = DateTime.now();
		if (time.getDayOfYear() == now.getDayOfYear()) {
			return today.print(time);
		} else if (time.getYear() == now.getYear()) {
			return thisYear.print(time);
		} else {
			return previousYear.print(time);
		}
	}
}
