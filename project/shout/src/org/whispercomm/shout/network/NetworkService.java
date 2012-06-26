package org.whispercomm.shout.network;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.shout.R;
import org.whispercomm.shout.SettingsActivity;

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

	Messenger appMessenger;
	ManesInterface manesIf;
	private boolean isRunning;
	private boolean inForeground;
	NetworkProtocol networkProtocol;

	@Override
	public final void onCreate() {
		// TODO Create ALL THE THINGS
		this.appMessenger = new Messenger(new AppShoutHandler());
		try {
			this.manesIf = new ManesInterface(APP_ID, getApplicationContext());
		} catch (ManesNotInstalledException e) {
			// TODO Handle this
			throw new RuntimeException(e);
		}
		networkProtocol = new NaiveBroadcast(manesIf, getApplicationContext());
		isRunning = true;
		new Thread(new NetworkReceiver()).start();
		// TODO Start / bind all the things in the background based on status
		// response?
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
		isRunning = false;
		manesIf.disconnect();
		// Any other clear-up specific to the particular subclass
		networkProtocol.clearUp();
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
				// TODO Find out why networkProtocol gets nulled
				networkProtocol.handleOutgoingAppShout(shoutIdInt);
			} else if (msg.what == STOP_FOREGROUND) {
				if (inForeground) {
					stopForeground(true);
					inForeground = false;
				}
			}
		}
	}

	/**
	 * Listens and handles received shouts from MANES network
	 */
	class NetworkReceiver implements Runnable {

		public static final String TAG = "NetworkReceiver";

		/**
		 * Period to return from ManesInterface.receive() and check for whether
		 * ShoutService is still running
		 */
		static final long CHECK_PERIOD = 10 * 60 * 1000;

		@Override
		public void run() {
			byte[] shoutBytes = null;
			while (isRunning) {
				shoutBytes = manesIf.receive(CHECK_PERIOD);

				if (shoutBytes == null)
					continue;

				NetworkShout shout;
				try {
					shout = new NetworkShout(shoutBytes);

					// Handle this incoming shout
					networkProtocol.handleIncomingNetworkShout(shout);
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, e.getMessage());
				} catch (AuthenticityFailureException e) {
					Log.e(TAG, e.getMessage());
				} catch (InvalidKeyException e) {
					Log.e(TAG, e.getMessage());
				} catch (NoSuchAlgorithmException e) {
					Log.e(TAG, e.getMessage());
				} catch (SignatureException e) {
					Log.e(TAG, e.getMessage());
				} catch (NoSuchProviderException e) {
					Log.e(TAG, e.getMessage());
				} catch (InvalidKeySpecException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}
}
