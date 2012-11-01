
package org.whispercomm.shout.notification;

import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.provider.ShoutProviderContract.Shouts;
import org.whispercomm.shout.ui.AbstractShoutActivity;
import org.whispercomm.shout.ui.DetailsActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class ShoutContentObserver extends ContentObserver {

	private static final int SHOUT_RECEIVED_NOTIFICATION_ID = 1;

	private static final int icon = R.drawable.notification_icon;

	private static final CharSequence contentTitle = "Shout Received";

	private Context context;
	private NotificationManager notificationManager;

	public ShoutContentObserver(Handler h, Context context) {
		super(h);
		this.context = context;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		context.getContentResolver().registerContentObserver(Shouts.CONTENT_URI, true, this);
	}

	private PendingIntent createIntent(Shout shout) {
		Intent intent = new Intent(context, DetailsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(DetailsActivity.SHOUT_ID, shout.getHash());
		return PendingIntent.getActivity(context, 0, intent, 0);
	}

	// TODO Ideally, we want to call something like
	// ShoutProviderContract.getCursorOverAllShouts(context);, but this only
	// gets original shouts, not comments
	private Cursor getShoutCursor() {
		String sortOrder = Shouts.TIME_RECEIVED + " DESC";
		Uri uri = Shouts.CONTENT_URI;
		String selection = null;
		Cursor result = context.getContentResolver().query(uri, null,
				selection, null, sortOrder);
		return result;
	}

	// TODO We should probably limit what causes a notification. If a message is
	// reshouted 100 times, they shouldn't get 100 notifications
	@Override
	public void onChange(boolean selfChange) {
		if (!selfChange && !AbstractShoutActivity.isVisible()) {
			Cursor cursor = getShoutCursor();
			cursor.moveToFirst();
			Shout shout = ShoutProviderContract.retrieveShoutFromCursor(context, cursor);
			// Only notify if it is not a reshout
			if (shout.getMessage() != null) {
				sendNotification(shout);
			}
			cursor.close();
		}
	}

	public void sendNotification(Shout shout) {
		String author = shout.getSender().getUsername();
		String message = shout.getMessage();
		String shoutText = author + ": " + message;
		Notification notification = constructNotification(shoutText, shoutText, createIntent(shout));
		notificationManager.notify(SHOUT_RECEIVED_NOTIFICATION_ID, notification);
	}

	public Notification constructNotification(String tickerText, String contentText,
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
}
