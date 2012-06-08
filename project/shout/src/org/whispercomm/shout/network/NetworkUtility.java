package org.whispercomm.shout.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.whispercomm.manes.client.maclib.ManesInterface;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class NetworkUtility extends Service {

	public static final String TAG = "******NetworkUtility******";
	public static final int NEW_SHOUT = 1;
	public static final int APP_ID = 74688;// "shout" on a phone keyboard

	Messenger appMessenger;
	ManesInterface manesIf;
	boolean isRunning;
	NetworkProtocol networkProtocol;

	@Override
	public final void onCreate() {
		this.appMessenger = new Messenger(new AppShoutHandler());
		this.manesIf = new ManesInterface(APP_ID, this);
		Thread register = new Thread() {
			@Override
			public void run() {
				manesIf.initialize(APP_ID);
				// start receiver thread
				new Thread(new NetworkReceiver()).start();
			}
		};
		register.start();
		// initialize the network protocol
		networkProtocol = (NetworkProtocol) new NaiveBroadcast(manesIf);
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

		public static final String TAG = "******NetworkReceiver******";

		/**
		 * Period to return from ManesInterface.receive() and check for whether
		 * ShoutService is still running
		 */
		static final long CHECK_PERIOD = 10 * 60 * 1000;

		@Override
		public void run() {
			byte[] shoutBytes = null;
			while (isRunning) {
				try {
					shoutBytes = manesIf.receive(CHECK_PERIOD);
				} catch (IOException e) {
					// TODO What if receive is not successful? For now sleep and
					// try
					// again????
					Log.i(TAG, e.getMessage());
					try {
						Thread.sleep(CHECK_PERIOD);
					} catch (InterruptedException e1) {
						Log.e(TAG, e1.getMessage());
					}
					continue;
				}

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
