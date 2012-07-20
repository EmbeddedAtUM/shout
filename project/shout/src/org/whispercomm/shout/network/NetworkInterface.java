
package org.whispercomm.shout.network;

import java.io.IOException;

import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.NotRegisteredException;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * UI side interface for establishing channels to notify new shouts to send out.
 * <p>
 * Each UI activity should initiate a NetworkInterface object in order to notify
 * the network protocol.
 * 
 * @author Yue Liu
 * @author David R. Bild
 */
public class NetworkInterface {
	public static final String TAG = NetworkInterface.class.getSimpleName();

	private Context context;

	private NetworkServiceBinder shoutService;
	private ServiceConnection connection;

	public NetworkInterface(Context context) {
		this.context = context;
		this.connection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "Connected to service.");
				shoutService = NetworkServiceBinder.Stub.asInterface(service);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "Disconnected from service unexpectedly.");
			}

		};

		context.bindService(new Intent(context, NetworkService.class),
				connection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Notifies ShoutService to send the given shout.
	 * <p>
	 * The method returns immediately, with indications of whether the
	 * notification is successful. If not, the caller should either wait and try
	 * later, or give up.
	 * 
	 * @param shout the shout to be sent out
	 * @throws NotConnectedException if not connected to the Shout service
	 * @throws ShoutChainTooLongException if the provided shout has too many
	 *             ancestors to be sent
	 * @throws ManesNotInstalledException if the Manes client is not installed
	 * @throws NotRegisteredException if the Manes client is not registered
	 * @throws IOException if an network IO error prevented transmission
	 */
	public void send(LocalShout shout) throws NotConnectedException, ShoutChainTooLongException,
			ManesNotInstalledException, NotRegisteredException, IOException {
		checkBinderNotNull();

		try {
			ErrorCode rc = shoutService.send(shout.getHash());
			switch (rc) {
				case SUCCESS:
					return;
				case SHOUT_CHAIN_TOO_LONG:
					throw new ShoutChainTooLongException();
				case MANES_NOT_INSTALLED:
					throw new ManesNotInstalledException();
				case MANES_NOT_REGISTERED:
					throw new NotRegisteredException();
				case IO_ERROR:
					throw new IOException();
				default:
					Log.w(TAG, "Unknown error code returned by send(): " + rc);
					return;
			}
		} catch (RemoteException e) {
			throw new NotConnectedException("Call to service method send() failed.", e);
		}
	}

	/**
	 * Unbind to ShoutService, must be called in the calling activity's
	 * onDestroy() method
	 */
	public void unbind() {
		if (connection != null) {
			context.unbindService(connection);
		}
	}

	/**
	 * Thrown if a method that delegates to {@link NetworkService} is called,
	 * but the service is not yet bound or if the service call throws a
	 * {@link RemoteException}.
	 * 
	 * @author David R. Bild
	 */
	public static class NotConnectedException extends Exception {
		private static final long serialVersionUID = 2245070951852759576L;

		public NotConnectedException() {
			super();
		}

		public NotConnectedException(String message) {
			super(message);
		}

		public NotConnectedException(String message, Throwable cause) {
			super(message, cause);
		}

		public NotConnectedException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Throws an exception if the Shout service binder reference is null.
	 * 
	 * @throws NotConnectedException if the Shout service binder reference is
	 *             null.
	 */
	private void checkBinderNotNull() throws NotConnectedException {
		if (shoutService == null) {
			throw new NotConnectedException("Shout service binder is null.");
		}
	}

}
