
package org.whispercomm.shout.network;

import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class NetworkService extends Service {
	public static final String TAG = NetworkService.class.getSimpleName();

	public static final int NEW_SHOUT = 1;
	public static final int APP_ID = 74688;// "shout" on a phone keyboard

	private ManesInterface manes;
	private Messenger appMessenger;
	private NetworkProtocol networkProtocol;
	private NetworkReceiver networkReceiver;

	@Override
	public final void onCreate() {
		Log.i(TAG, "Starting service.");
		initialize();
		Log.i(TAG, "Service started.");
	}

	private synchronized void initialize() {
		if (manes == null) {
			Log.i(TAG, "Starting initialization.");
			try {
				this.manes = new ManesInterface(APP_ID, getApplicationContext());

				this.networkProtocol = new NaiveNetworkProtocol(manes,
						getApplicationContext());
				this.appMessenger = new Messenger(new AppShoutHandler(getApplicationContext(),
						networkProtocol));
				this.networkReceiver = new NetworkReceiver(this.manes,
						this.networkProtocol);

				this.networkProtocol.initialize();
				this.networkReceiver.initialize();

				Log.i(TAG, "Finishing initialization.");
			} catch (ManesNotInstalledException e) {
				Log.w(TAG,
						"MANES is not installed.  Service will not be fully functional until it is installed.");
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (appMessenger != null) {
			return appMessenger.getBinder();
		} else {
			return null;
		}
	}

	@Override
	public final void onDestroy() {
		Log.i(TAG, "Stopping service.");
		if (manes != null) {
			manes.disconnect();
		}
		if (networkProtocol != null) {
			networkProtocol.cleanup();
		}
		if (networkReceiver != null) {
			networkReceiver.cleanup();
		}
		Log.i(TAG, "Service stopped.");
	}

	/**
	 * Handler wrapper for processing incoming shouts from application (e.g.,
	 * UI)
	 */
	private static class AppShoutHandler extends Handler {

		private Context context;
		private NetworkProtocol networkProtocol;

		public AppShoutHandler(Context context, NetworkProtocol networkProtocol) {
			super();
			this.context = context;
			this.networkProtocol = networkProtocol;
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == NEW_SHOUT) {
				byte[] shoutHash = (byte[]) msg.obj;
				Shout shout = ShoutProviderContract.retrieveShoutByHash(context,
						shoutHash);
				networkProtocol.sendShout(shout);
			}
		}
	}

}
