
package org.whispercomm.shout.notification;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.provider.ShoutProviderContract.Shouts;
import org.whispercomm.shout.ui.AbstractShoutActivity;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class ShoutContentObserver extends ContentObserver {

	private Context context;
	private ShoutNotificationManager notificationManager;

	public ShoutContentObserver(Handler h, Context context) {
		super(h);
		this.context = context;
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
				notificationManager.sendNotification(shout);
			}
			cursor.close();
		}
	}
}
