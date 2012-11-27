
package org.whispercomm.shout.notification;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ui.DetailsActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

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
		Notification notification = constructNotification(shoutText, shoutText, createIntent(shout));
		notificationManager.notify(SHOUT_RECEIVED_NOTIFICATION_ID, notification);
	}

	private Notification constructNotification(String tickerText, String contentText,
			PendingIntent pIntent) {
		// Show the notification now
		long when = System.currentTimeMillis();

		// Construct the notification
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, pIntent);

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
