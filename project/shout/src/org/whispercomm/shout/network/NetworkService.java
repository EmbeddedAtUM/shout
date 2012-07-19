
package org.whispercomm.shout.network;

import org.whispercomm.manes.client.maclib.ManesActivityHelper;
import org.whispercomm.manes.client.maclib.ManesActivityHelper.ManesInstallationListener;
import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.NotRegisteredException;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class NetworkService extends Service {
	public static final String TAG = NetworkService.class.getSimpleName();

	public static final int APP_ID = 74688;// "shout" on a phone keyboard

	private ManesInterface manes;
	private NetworkProtocol networkProtocol;
	private NetworkReceiver networkReceiver;

	private BroadcastReceiver installationListener;

	private ErrorCode initialized;

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
				this.networkReceiver = new NetworkReceiver(this.manes,
						this.networkProtocol);

				this.networkProtocol.initialize();
				this.networkReceiver.initialize();

				initialized = ErrorCode.SUCCESS;
				Log.i(TAG, "Finishing initialization.");
			} catch (ManesNotInstalledException e) {
				initialized = ErrorCode.MANES_NOT_INSTALLED;
				listenForManesInstallation();
				Log.w(TAG,
						"MANES is not installed.  Service will not be fully functional until it is installed.");
			}
		}
	}

	private void listenForManesInstallation() {
		// Start listening if not already
		if (installationListener == null) {
			installationListener = ManesActivityHelper.registerForManesInstallation(this,
					new ManesInstallationListener() {
						@Override
						public void installed() {
							// Reinitialize when manes is installed
							initialize();
							unregisterReceiver(installationListener);
							installationListener = null;
						}
					});
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
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
		if (installationListener != null) {
			unregisterReceiver(installationListener);
		}
		Log.i(TAG, "Service stopped.");
	}

	private final NetworkServiceBinder.Stub binder = new
			NetworkServiceBinder.Stub() {
				@Override
				public ErrorCode initialized() throws RemoteException {
					return initialized;
				}

				@Override
				public ErrorCode send(byte[] hash) throws RemoteException {
					if (manes == null) {
						return ErrorCode.MANES_NOT_INSTALLED;
					}

					try {
						Shout shout =
								ShoutProviderContract.retrieveShoutByHash(NetworkService.this,
										hash);
						networkProtocol.sendShout(shout);
						return ErrorCode.SUCCESS;
					} catch (ShoutChainTooLongException e) {
						return ErrorCode.SHOUT_CHAIN_TOO_LONG;
					} catch (NotRegisteredException e) {
						return ErrorCode.MANES_NOT_REGISTERED;
					}
				}
			};

}
