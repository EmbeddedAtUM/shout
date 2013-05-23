
package org.whispercomm.shout.notification;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutColorContract;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.provider.ShoutProviderContract.Shouts;
import org.whispercomm.shout.ui.AbstractShoutActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;

public class ShoutContentObserver extends ContentObserver {

	private Context context;
	private NotificationSender notificationSender;

	public ShoutContentObserver(Handler h, Context context) {
		super(h);
		this.context = context;
		this.notificationSender = new NotificationSender(context);
		context.getContentResolver().registerContentObserver(Shouts.CONTENT_URI, true, this);
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

	@Override
	public void onChange(boolean selfChange) {
		if (!selfChange && !AbstractShoutActivity.isVisible()) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean showNotification = preferences.getBoolean("show_notifications", true);
			if (showNotification) {
				Cursor cursor = getShoutCursor();
				cursor.moveToFirst();
				Shout shout = ShoutProviderContract.retrieveShoutFromCursor(context, cursor);
				// Only notify if it is not a reshout
				if (shout.getMessage() != null) {
					ShoutColorContract.saveShoutBorder(context, shout.getSender());
					notificationSender.sendNotification(shout);
				}
				cursor.close();
			}
		}
	}
}
