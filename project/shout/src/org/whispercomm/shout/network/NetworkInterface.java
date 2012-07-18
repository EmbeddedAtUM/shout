
package org.whispercomm.shout.network;

import org.whispercomm.shout.LocalShout;

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
	 * @returns a status code indicating success or reason for failure
	 */
	public ErrorCode send(LocalShout shout) {
		if (shoutService == null) {
			return ErrorCode.IO_ERROR;
		}

		try {
			return shoutService.send(shout.getHash());
		} catch (RemoteException e) {
			return ErrorCode.IO_ERROR;
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

}
