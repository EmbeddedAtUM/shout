
package org.whispercomm.shout.util;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class for displaying formatted ages.
 * 
 * @author David R. Bild
 */
public class FormattedAge {
	private static final DateTimeFormatter previousYear = DateTimeFormat
			.forPattern("MMM d',' yyyy 'at' h:mm a");
	private static final DateTimeFormatter thisYear = DateTimeFormat
			.forPattern("MMM d 'at' h:mm a");
	private static final DateTimeFormatter today = DateTimeFormat.forPattern("'Today at' h:mm a");

	private static final DateTimeFormatter thisYearNoTime = DateTimeFormat.forPattern("MMM d");
	private static final DateTimeFormatter previousYearNoTime = DateTimeFormat
			.forPattern("MMM d',' yyyy");

	public static String formatAge(DateTime dateTime, boolean absoluteNoTime) {
		return FormattedAge.getAge(dateTime, absoluteNoTime).toString();
	}

	public static String formatAbsoluteDate(DateTime dateTime) {
		return FormattedAge.getAbsoluteDate(dateTime);
	}

	public static String formatAbsolute(DateTime dateTime) {
		return FormattedAge.getAbsoluteDateTime(dateTime);
	}

	private static String getAge(DateTime dateTime, boolean absoluteNoTime) {
		Duration age = new Duration(dateTime, null);
		TimeUnit unit = TimeUnit.get(age);
		long unitsPassed = 0;
		switch (unit) {
			case ABSOLUTE:
				if (absoluteNoTime) {
					return getAbsoluteDate(dateTime);
				} else {
					return getAbsoluteDateTime(dateTime);
				}
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

		return String.format(Locale.getDefault(), "%d %s%s ago.", unitsPassed, unit,
				unitsPassed == 1 ? "" : "s");
	}

	private static String getAbsoluteDateTime(DateTime time) {
		DateTime now = DateTime.now();
		if (time.getDayOfYear() == now.getDayOfYear()) {
			return today.print(time);
		} else if (time.getYear() == now.getYear()) {
			return thisYear.print(time);
		} else {
			return previousYear.print(time);
		}
	}

	private static String getAbsoluteDate(DateTime time) {
		DateTime now = DateTime.now();
		if (time.getDayOfYear() == now.getDayOfYear()) {
			return "Today";
		} else if (time.getYear() == now.getYear()) {
			return thisYearNoTime.print(time);
		} else {
			return previousYearNoTime.print(time);
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

		public static TimeUnit get(Duration age) {
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
	}

}
