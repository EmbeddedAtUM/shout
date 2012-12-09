
package org.whispercomm.shout.notification;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ui.DetailsActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

public class NotificationSender {
	private NotificationManager notificationManager;
	private Context context;
	public static final int SHOUT_RECEIVED_NOTIFICATION_ID = 1;

	private static final int icon = R.drawable.notification_icon;

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
		Notification notification = nb.setContentText(contentText)
				.setContentTitle(contentTitle)
				.setTicker(tickerText)
				.setSmallIcon(icon)
				.setWhen(when)
				.setContentIntent(pIntent)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setLights(Color.GREEN, 1000, 4000)
				.getNotification();
		// @formatter: on

		// Remove the notification after it is clicked
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		return notification;
	}

	private PendingIntent createIntent(Shout shout) {
		Intent intent = new Intent(context, DetailsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(DetailsActivity.SHOUT_ID, shout.getHash());
		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
