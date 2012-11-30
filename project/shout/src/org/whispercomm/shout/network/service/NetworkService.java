
package org.whispercomm.shout.network.service;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import org.whispercomm.manes.client.maclib.ManesInstallationListener;
import org.whispercomm.manes.client.maclib.ManesInstallationReceiver;
import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.manes.client.maclib.ManesInterface.ManesConnection;
import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.network.NetworkReceiver;
import org.whispercomm.shout.network.ObjectType;
import org.whispercomm.shout.network.PacketProtocol;
import org.whispercomm.shout.network.content.ContentProtocol;
import org.whispercomm.shout.network.content.SimpleContentRequestHandler;
import org.whispercomm.shout.network.shout.NaiveNetworkProtocol;
import org.whispercomm.shout.network.shout.NetworkProtocol;
import org.whispercomm.shout.network.shout.ShoutChainTooLongException;
import org.whispercomm.shout.network.shout.ShoutProtocol;
import org.whispercomm.shout.notification.ShoutContentObserver;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.provider.ShoutProviderContract.Shouts;
import org.whispercomm.shout.util.AlarmExecutorService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NetworkService extends Service implements ManesConnection, ManesInstallationListener {
	public static final String TAG = NetworkService.class.getSimpleName();

	public static final int APP_ID = 74688;// "shout" on a phone keyboard

	public static final String ACTION_REQUEST_CONTENT = "org.whispercomm.shout.REQUEST_CONTENT";
	public static final String EXTRA_HASH = "hash";

	private ManesInterface manes;

	// Packets are passed from socket to NetworkReceiver to PacketReceiver to
	// ShoutReceiver to NetworkProtocol.
	private NetworkReceiver networkReceiver;
	private PacketProtocol packetProtocol;
	private ShoutProtocol shoutProtocol;
	private ContentProtocol contentProtocol;
	private SimpleContentRequestHandler contentRequestHandler;
	private NetworkProtocol networkProtocol;

	private LocalBroadcastManager localBroadcastManager;
	private ContentHashReceiver contentHashReceiver;
	private ContentManager contentManager;

	// Starts the service once MANES client is installed.
	private ManesInstallationReceiver manesInstallReceiver;

	private CopyOnWriteArrayList<ManesStatusCallback> callbacks;

	private boolean manesConnected;

	private ShoutContentObserver shoutContentObserver;

	@Override
	public final void onCreate() {
		Log.i(TAG, "Starting service.");
		manesConnected = false;
		callbacks = new CopyOnWriteArrayList<ManesStatusCallback>();
		manesInstallReceiver = ManesInstallationReceiver.start(this, this);
		shoutContentObserver = new ShoutContentObserver(new Handler(), this);
		localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
		contentHashReceiver = new ContentHashReceiver();
		localBroadcastManager.registerReceiver(contentHashReceiver, new IntentFilter(
				ACTION_REQUEST_CONTENT));
		getApplicationContext().getContentResolver().registerContentObserver(Shouts.CONTENT_URI,
				true, shoutContentObserver);
		initialize();
		Log.i(TAG, "Service started.");
	}

	@Override
	public final void onDestroy() {
		Log.i(TAG, "Stopping service.");
		getApplicationContext().getContentResolver()
				.unregisterContentObserver(shoutContentObserver);
		uninitialize();
		manesInstallReceiver.stop();
		Log.i(TAG, "Service stopped.");
	}

	private synchronized void initialize() {
		if (manes == null) {
			Log.i(TAG, "Starting initialization.");
			try {
				manes = new ManesInterface(APP_ID, getApplicationContext(), this);

				networkReceiver = new NetworkReceiver(this.manes);

				packetProtocol = new PacketProtocol(manes);
				networkReceiver.register(packetProtocol);

				shoutProtocol = new ShoutProtocol(packetProtocol);
				packetProtocol.register(ObjectType.Shout, shoutProtocol);

				contentManager = new ContentManager(this);
				contentProtocol = new ContentProtocol(packetProtocol, contentManager);
				contentRequestHandler = new SimpleContentRequestHandler(new AlarmExecutorService(
						this, Executors.newSingleThreadExecutor(), "SHOUT_CONTENT_PROTOCOL"),
						packetProtocol,
						contentProtocol, contentManager.getObjectStorage(), contentManager);
				contentProtocol.setContentRequestHandler(contentRequestHandler);

				packetProtocol.register(ObjectType.ContentDescriptor, contentProtocol);
				packetProtocol.register(ObjectType.MerkleNode, contentProtocol);
				packetProtocol.register(ObjectType.ContentRequest, contentProtocol);

				networkProtocol = new NaiveNetworkProtocol(shoutProtocol, getApplicationContext());
				shoutProtocol.register(networkProtocol);
				shoutProtocol.register(contentRequestHandler);

				this.networkProtocol.initialize();
				this.networkReceiver.initialize();
				this.contentRequestHandler.initialize();

				notifyInstalled(true);
				Log.i(TAG, "Finishing initialization.");
			} catch (ManesNotInstalledException e) {
				notifyInstalled(false);
				Log.w(TAG,
						"MANES is not installed.  Service will not be fully functional until it is installed.");
			}
		}
	}

	private synchronized void uninitialize() {
		if (manes != null) {
			manes.disconnect();
		}
		if (contentRequestHandler != null) {
			contentRequestHandler.cleanup();
		}
		if (networkProtocol != null) {
			networkProtocol.cleanup();
		}
		if (networkReceiver != null) {
			networkReceiver.cleanup();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void manesInstalled() {
		initialize();
	}

	@Override
	public void onManesServiceConnected() {
		Log.i(TAG, "Connection to Manes service established.");
		manesConnected = true;
		notifyRegistered(manes.registered());
	}

	@Override
	public void onManesServiceDisconnected() {
		manesConnected = false;
		Log.i(TAG, "Connection to Manes service lost.");
	}

	private void addCallback(ManesStatusCallback callback) {
		try {
			if (manes == null) {
				callback.installed(false);
			} else {
				callback.installed(true);
			}

			if (manesConnected) {
				if (manes.registered()) {
					callback.registered(true);
				} else {
					callback.registered(false);
				}
			}

			callbacks.add(callback);
		} catch (RemoteException e) {
			Log.w(TAG, "callback invokation failed. Removing callback instance.", e);
		}
	}

	private void removeCallback(ManesStatusCallback callback) {
		callbacks.remove(callback);
	}

	private void notifyInstalled(boolean installed) {
		for (ManesStatusCallback callback : callbacks) {
			try {
				callback.installed(installed);
			} catch (RemoteException e) {
				Log.w(TAG, "callback invokation failed.  Removing callback", e);
				removeCallback(callback);
			}
		}
	}

	private void notifyRegistered(boolean registered) {
		for (ManesStatusCallback callback : callbacks) {
			try {
				callback.registered(registered);
			} catch (RemoteException e) {
				Log.w(TAG, "callback invokation failed.  Removing callback", e);
				removeCallback(callback);
			}
		}
	}

	private final NetworkServiceBinder.Stub binder = new
			NetworkServiceBinder.Stub() {
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
					} catch (ManesNotRegisteredException e) {
						return ErrorCode.MANES_NOT_REGISTERED;
					}
				}

				@Override
				public void register(ManesStatusCallback callback) throws RemoteException {
					addCallback(callback);
				}

				@Override
				public void unregister(ManesStatusCallback callback) throws RemoteException {
					removeCallback(callback);
				}
			};

	private class ContentHashReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Hash hash = new Hash(intent.getByteArrayExtra(EXTRA_HASH));
			contentRequestHandler.request(hash);
		}

	}

}
