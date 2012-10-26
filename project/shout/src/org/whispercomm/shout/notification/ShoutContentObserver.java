
package org.whispercomm.shout.notification;

import org.whispercomm.shout.R;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.provider.ShoutProviderContract.Shouts;
import org.whispercomm.shout.ui.ShoutActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

public class ShoutContentObserver extends ContentObserver {

	private static final int SHOUT_RECEIVED_NOTIFICATION_ID = 1;
	private static final String TAG = ShoutContentObserver.class.getName();

	private static final int icon = R.drawable.notification_icon;

	private static final CharSequence contentTitle = "Shout Received";

	private Context context;
	private NotificationManager notificationManager;
	private PendingIntent contentIntent;

	public ShoutContentObserver(Handler h, Context context) {
		super(h);
		this.context = context;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(context, ShoutActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		context.getContentResolver().registerContentObserver(Shouts.CONTENT_URI, true, this);
	}

	@Override
	public void onChange(boolean selfChange) {
		if (!selfChange) {
			Cursor cursor = ShoutProviderContract.getCursorOverAllShouts(context);
			cursor.moveToFirst();
			String shoutText = cursor.getString(cursor.getColumnIndex(Shouts.MESSAGE))
					+ cursor.getString(cursor.getColumnIndex(Shouts.AUTHOR));
			sendNotification(shoutText);
		}
	}

	/**
	 * Sends an Shout received notification
	 */
	public void sendNotification(String shoutText) {
		long when = System.currentTimeMillis();

		CharSequence tickerText = shoutText;
		CharSequence contentText = shoutText;

		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager
				.notify(SHOUT_RECEIVED_NOTIFICATION_ID, notification);
	}
}
