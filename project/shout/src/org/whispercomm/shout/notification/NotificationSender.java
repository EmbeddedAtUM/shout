
package org.whispercomm.shout.notification;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ui.DetailsActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class NotificationSender {
	private NotificationManager notificationManager;
	private Context context;
	public static final int SHOUT_RECEIVED_NOTIFICATION_ID = 1;

	private static final int icon = R.drawable.ic_notification;

	private static final CharSequence contentTitle = "Shout Received";

	public NotificationSender(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void sendNotification(Shout shout) {
		String author = shout.getSender().getUsername();
		String message = shout.getMessage();
		String shoutText = author + ": " + message;
		Notification notification = constructNotification(shoutText, shoutText,
				createIntent(shout));
		notificationManager.notify(SHOUT_RECEIVED_NOTIFICATION_ID, notification);
	}

	private Notification constructNotification(String tickerText, String contentText,
			PendingIntent pIntent) {
		// Show the notification now
		long when = System.currentTimeMillis();

		// Construct the notification
		NotificationCompat.Builder nb = new NotificationCompat.Builder(context);

		// @formatter: off
		nb.setContentText(contentText)
				.setContentTitle(contentTitle)
				.setTicker(tickerText)
				.setSmallIcon(icon)
				.setWhen(when)
				.setContentIntent(pIntent)
				.getNotification();
		// @formatter: on

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean notificationSound = preferences.getBoolean("notification_sound", false);
		boolean notificationLed = preferences.getBoolean("notification_led", true);

		int defaultFlags = 0;
		if (notificationSound) {
			defaultFlags |= Notification.DEFAULT_SOUND;
		}
		if (notificationLed) {
			defaultFlags |= Notification.DEFAULT_LIGHTS;
		}
		nb.setDefaults(defaultFlags);

		Notification notification = nb.getNotification();

		// Remove the notification after it is clicked
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		return notification;
	}

	private PendingIntent createIntent(Shout shout) {
		// The notification for a comment should go to the parent
		if (shout.getParent() != null) {
			shout = shout.getParent();
		}
		Intent intent = new Intent(context, DetailsActivity.class);
		intent.putExtra(DetailsActivity.SHOUT_ID, shout.getHash());

		/* Build the history */
		TaskStackBuilder stackBuilder = TaskStackBuilder.from(context);
		stackBuilder.addParentStack(DetailsActivity.class);
		stackBuilder.addNextIntent(intent);

		return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
