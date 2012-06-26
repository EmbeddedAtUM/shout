package org.whispercomm.shout.network;

import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.shout.R;
import org.whispercomm.shout.SettingsActivity;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class NetworkService extends Service {
	public static final String TAG = NetworkService.class.getSimpleName();

	public static final int NEW_SHOUT = 1;
	public static final int STOP_FOREGROUND = 2;
	public static final int APP_ID = 74688;// "shout" on a phone keyboard
	private static final int ONGOING_NOTIFICATION = 1;

	private ManesInterface manes;
	private Messenger appMessenger;
	private NetworkProtocol networkProtocol;
	private NetworkReceiver networkReceiver;

	private boolean inForeground;

	@Override
	public final void onCreate() {
		Log.i(TAG, "Starting service.");
		try {
			this.manes = new ManesInterface(APP_ID, getApplicationContext());
		} catch (ManesNotInstalledException e) {
			// TODO Handle this
			throw new RuntimeException(e);
		}

		this.appMessenger = new Messenger(new AppShoutHandler());
		this.networkProtocol = new NaiveNetworkProtocol(manes,
				getApplicationContext());
		this.networkReceiver = new NetworkReceiver(this.manes,
				this.networkProtocol);

		this.networkProtocol.initialize();
		this.networkReceiver.initialize();

		Notification notification = new Notification(R.drawable.icon,
				getText(R.string.serviceTickerText), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, SettingsActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this,
				getText(R.string.serviceTitleText),
				getText(R.string.serviceDescriptionText), pendingIntent);
		startForeground(ONGOING_NOTIFICATION, notification);
		this.inForeground = true;

		Log.i(TAG, "Service started.");
	}

	/**
	 * Create the channel for accepting incoming shouts from applications, e.g.,
	 * UI
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return appMessenger.getBinder();
	}

	@Override
	public final void onDestroy() {
		Log.i(TAG, "Stopping service.");
		manes.disconnect();
		networkProtocol.cleanup();
		networkReceiver.cleanup();
		Log.i(TAG, "Service stopped.");
	}

	/**
	 * Handler wrapper for processing incoming shouts from application (e.g.,
	 * UI)
	 */
	class AppShoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == NEW_SHOUT) {
				long shoutId = (Long) msg.obj; // FIXME
				int shoutIdInt = (int) shoutId;
				Shout shout = ShoutProviderContract
						.retrieveShoutById(
								NetworkService.this.getApplicationContext(),
								shoutIdInt);
				// TODO Find out why networkProtocol gets nulled
				networkProtocol.sendShout(shout);
			} else if (msg.what == STOP_FOREGROUND) {
				if (inForeground) {
					stopForeground(true);
					inForeground = false;
				}
			}
		}
	}

}
