package org.whispercomm.shout.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.whispercomm.manes.client.maclib.ManesInterface;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


public abstract class NetworkUtility extends Service {

	public static final String TAG = "******NetworkUtility******";
	public static final int NEW_SHOUT = 1;
	public static final int APP_ID = 74688;// "shout" on a phone keyboard
	
	Messenger appMessenger;
	ManesInterface manesIf;
	boolean isRunning;
	
	@Override
	public final void onCreate() {
		this.appMessenger = new Messenger(new AppShoutHandler());
		try {
			this.manesIf = new ManesInterface(APP_ID, this);
		} catch (RemoteException e) {
			// TODO  what to do if cannot connect to ManesInterface?
			Log.e(TAG, e.getMessage());
			stopSelf();
		}
		this.isRunning = true;
		// start receiver thread
		new Thread(new NetworkReceiver()).run();
		// Any other initialization specific to the particular subclass
		if (initialize() == false){
			Log.e(TAG, "Initialization not successful.");
			stopSelf();
		}
	}
	
	/**
	 * Create the channel for accepting incoming shouts from applications, e.g., UI
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return appMessenger.getBinder();
	}
	
	@Override
	public final void onDestroy() {
		isRunning = false;
		manesIf.unregister();
		// Any other clear-up specific to the particular subclass
		clearUp();
	}
	
	/**
	 * Any specific initialization of the particular subclass
	 * 
	 * @return whether the initialization is successful
	 */
	abstract protected boolean initialize();
	
	/**
	 * Any specific clear-up of the particular subclass
	 */
	abstract protected void clearUp();
	
	/**
	 * Handle incoming shout from from application (e.g., UI)
	 */
	abstract protected void handleIncomingAppShout(Uri shoutUri);
	
	/**
	 * Handle incoming shout from from the network
	 */
	abstract protected void handleIncomingNetworkShout(NetworkShout shout);
	
	/**
	 * Handler wrapper for processing incoming shouts from application (e.g., UI)
	 */
	class AppShoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == NEW_SHOUT) {
				handleIncomingAppShout((Uri) msg.obj);
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
					// TODO What if receive is not successful? For now sleep and try
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

				try {
					NetworkShout shout = new NetworkShout(shoutBytes);
					if (shout.isValid() == false)
						continue;
					// verify signature
					if (shout.verySignature() == false) {
						Log.i(TAG, "Received unauthenticated shout.");
						continue;
					}
					// Handle this incoming shout
					handleIncomingNetworkShout(shout);
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, e.getMessage());
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}

	}

}
