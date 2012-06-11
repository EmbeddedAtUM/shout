package org.whispercomm.shout.network;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.whispercomm.manes.client.maclib.ManesInterface;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class NetworkUtility extends Service {

	public static final String TAG = NetworkUtility.class.getSimpleName();
	public static final int NEW_SHOUT = 1;
	public static final int APP_ID = 74688;// "shout" on a phone keyboard

	Messenger appMessenger;
	ManesInterface manesIf;
	boolean isRunning;
	NetworkProtocol networkProtocol;

	@Override
	public final void onCreate() {
		this.appMessenger = new Messenger(new AppShoutHandler());
		this.manesIf = new ManesInterface(APP_ID, getApplicationContext());
		Thread register = new Thread() {
			@Override
			public void run() {
				Future<Boolean> initTask = manesIf.initialize();
				try {
					boolean status = initTask.get();
					if (status) {
						networkProtocol = new NaiveBroadcast(manesIf,
								getApplicationContext());
						new Thread(new NetworkReceiver()).start();
					} else {
						// TODO
					}
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
				} catch (ExecutionException e) {
					Log.e(TAG, e.getMessage());
				}

			}
		};
		register.start();
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
				networkProtocol.handleOutgoingAppShout(shoutIdInt);
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
