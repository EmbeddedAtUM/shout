
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

	public static enum TimeUnit {
		SECOND("second"), MINUTE("minute"), HOUR("hour"), DAY("day"), WEEK("week"), ABSOLUTE;

		private String label;

		private TimeUnit() {
			this.label = null;
		}

		private TimeUnit(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			if (label != null)
				return label;
			else
				return super.toString();
		}

	}

	public static TimeUnit getAgeUnit(DateTime dt) {
		return getAgeUnit(new Duration(dt, null));
	}

	public static TimeUnit getAgeUnit(Duration age) {
		long days = age.getStandardDays();
		long hours = age.getStandardHours();
		long mins = age.getStandardMinutes();
		long secs = age.getStandardSeconds();

		if (days <= -14 || days >= 14)
			return TimeUnit.ABSOLUTE;
		else if (days <= -7 || days >= 7)
			return TimeUnit.WEEK;
		else if (hours <= -24 || hours >= 24)
			return TimeUnit.DAY;
		else if (mins <= -60 || mins >= 60)
			return TimeUnit.HOUR;
		else if (secs <= -60 || secs >= 60)
			return TimeUnit.MINUTE;
		else
			return TimeUnit.SECOND;
	}

	public static String getDateTimeAge(DateTime dt) {
		Duration age = new Duration(dt, null);
		TimeUnit unit = getAgeUnit(age);
		long unitsPassed = 0;
		switch (unit) {
			case ABSOLUTE:
				return getReadableDateTime(dt);
			case WEEK:
				unitsPassed = age.getStandardDays() / 7;
				break;
			case HOUR:
				unitsPassed = age.getStandardHours();
				break;
			case DAY:
				unitsPassed = age.getStandardDays();
				break;
			case MINUTE:
				unitsPassed = age.getStandardMinutes();
				break;
			case SECOND:
				unitsPassed = age.getStandardSeconds();
				break;
		}

		return String.format("%d %s%s ago.", unitsPassed, unit,
				unitsPassed == 1 ? "" : "s");
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
