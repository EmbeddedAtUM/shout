
package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class DateTimeConvert {
	public static String dtToString(DateTime dt) {
		long timePassed;
		String unit;

		Duration age = new Duration(dt, null);
		if (age.getStandardDays() >= 7) {
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

		return String.format("%d %s%s ago.", timePassed, unit,
				timePassed == 1 ? "" : "s");
	}
}
