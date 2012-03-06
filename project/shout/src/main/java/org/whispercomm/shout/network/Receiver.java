package org.whispercomm.shout.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.whispercomm.manes.client.maclib.ManesInterface;

import android.content.Context;
import android.util.Log;

/**
 * Listens and handles received shouts from MANES network
 * 
 * @author Yue Liu
 * 
 */
public class Receiver implements Runnable {

	public static final String TAG = "shout.network.receiverunnable";

	/**
	 * Period to return from ManesInterface.receive() and check for whether
	 * ShoutService is still running
	 */
	static final long CHECK_PERIOD = 10 * 60 * 1000;

	ManesInterface manesIf;
	Context callerContext;
	ShoutService.ServiceStateKeeper state;

	public Receiver(ManesInterface manesIf, Context callerContext,
			ShoutService.ServiceStateKeeper state) {
		this.manesIf = manesIf;
		this.callerContext = callerContext;
		this.state = state;
	}

	/**
	 * 
	 * Runnable that updates content provider if this shout has not been
	 * received before.
	 * 
	 */
	class contentProviderUpdater implements Runnable {

		@Override
		public void run() {
			// TODO Upadate the various tables in the content provider

		}

	}

	@Override
	public void run() {
		byte[] shoutBytes = null;
		while (state.isRunning()) {
			try {
				shoutBytes = manesIf.receive(CHECK_PERIOD);
			} catch (IOException e) {
				// TODO What if receive is not successful? For now sleep and try
				// again.
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
				Shout shout = new Shout(shoutBytes);
				if (shout.isValid() == false)
					continue;
				// verify signature
				if (shout.verySignature() == false) {
					Log.i(TAG, "Received unauthenticated shout.");
					continue;
				}
				// update content provider in a new thread
				new Thread(new contentProviderUpdater()).run();
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.getMessage());
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

}
