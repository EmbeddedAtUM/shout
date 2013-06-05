
package org.whispercomm.shout.util;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class for displaying formatted ages. One can register with this
 * object to be informed when the displayed age has changed.
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

	private static Timer DEFAULT_TIMER = new Timer();

	public static String format(DateTime dateTime) {
		return new FormattedAge(dateTime).toString();
	}

	public static String formatAbsolute(DateTime dateTime) {
		return FormattedAge.getAbsoluteDateTime(dateTime);
	}

	public static FormattedAge create(AgeListener ageListener) {
		return new FormattedAge(DEFAULT_TIMER, ageListener);
	}

	private Timer mTimer;

	private AgeListener mAgeListener;

	private TimerTask mTimerTask;

	private DateTime mDateTime;

	private Duration mAge;

	private DateTime mNextChange;

	private boolean mAbsoluteNoTime;

	private FormattedAge(DateTime dateTime) {
		mAbsoluteNoTime = false;
		setDateTime(dateTime);
	}

	public FormattedAge(Timer timer, AgeListener ageListener) {
		mAbsoluteNoTime = false;
		mTimer = timer;
		mAgeListener = ageListener;
	}

	public synchronized void setDateTime(DateTime dateTime) {
		mDateTime = dateTime;
		update();
	}

	/**
	 * If {@code true}, the absolute form will not include time. E.g.,
	 * "Dec 21, 2012" instead of "Dec 21, 2012 at 3:42PM".
	 * 
	 * @param notime
	 */
	public void setAbsoluteNoTime(boolean noTime) {
		mAbsoluteNoTime = noTime;
	}

	/**
	 * Stops notifying the {@link AgeListener} when the formatted age changes
	 * and releases any references to the {@code AgeListener} help by the
	 * {@link Timer}.
	 */
	public synchronized void stop() {
		cancelTimerTask();
	}

	/**
	 * Restarts notifying the {@link AgeListener} when the formatted age
	 * changes.
	 */
	public synchronized void restart() {
		update();
	}

	private void update() {
		mAge = new Duration(mDateTime, null);
		updateNextChange();
		if (mAgeListener != null)
			mAgeListener.update(this.toString());
		/*
		 * The interaction between the main thread, the timer thread, and the
		 * locks are leading to all sorts of freezes in the UI. Don't schedule
		 * update tasks for now.
		 */
		// updateTimerTask();
	}

	private synchronized void updateIfCurrent(TimerTask timerTask) {
		if (mTimerTask == timerTask)
			update();
	}

	private void updateNextChange() {
		switch (TimeUnit.get(mAge)) {
			case WEEK:
				mNextChange = mDateTime.plus(mAge).plusDays((int) mAge.getStandardDays() % 7);
				break;
			case DAY:
				mNextChange = mDateTime.plus(mAge).plusHours((int) mAge.getStandardHours() % 24);
				break;
			case HOUR:
				mNextChange = mDateTime.plus(mAge)
						.plusMinutes((int) mAge.getStandardMinutes() % 60);
				break;
			case MINUTE:
				mNextChange = mDateTime.plus(mAge)
						.plusSeconds((int) mAge.getStandardSeconds() % 60);
				break;
			case SECOND:
				mNextChange = mDateTime.plus(mAge).plusSeconds(1);
				break;
			default:
				break;
		}
	}

	@SuppressWarnings("unused")
	private void updateTimerTask() {
		cancelTimerTask();
		if (mNextChange != null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					updateIfCurrent(this);
				}
			};
			mTimer.schedule(mTimerTask, mNextChange.toDate());
		}

	}

	private void cancelTimerTask() {
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	@Override
	public String toString() {
		TimeUnit unit = TimeUnit.get(mAge);
		long unitsPassed = 0;
		switch (unit) {
			case ABSOLUTE:
				if (mAbsoluteNoTime) {
					return getAbsoluteDate(mDateTime);
				} else {
					return getAbsoluteDateTime(mDateTime);
				}
			case WEEK:
				unitsPassed = mAge.getStandardDays() / 7;
				break;
			case HOUR:
				unitsPassed = mAge.getStandardHours();
				break;
			case DAY:
				unitsPassed = mAge.getStandardDays();
				break;
			case MINUTE:
				unitsPassed = mAge.getStandardMinutes();
				break;
			case SECOND:
				unitsPassed = mAge.getStandardSeconds();
				break;
		}

		return String.format("%d %s%s ago.", unitsPassed, unit,
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

	/**
	 * Listener called when the formatted age has changed
	 */
	public static interface AgeListener {
		public void update(String age);
	}
}
