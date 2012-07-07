package org.whispercomm.shout.network;

import org.whispercomm.shout.LocalShout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * UI side interface for establishing channels to notify new shouts to send out.
 * <p>
 * Each UI activity should initiate a NetworkInterface object in order to notify
 * the network protocol.
 * 
 * @author Yue Liu
 */
public class NetworkInterface {
	public static final String TAG = NetworkInterface.class.getSimpleName();

	private static NetworkInterface instance = null;
	private Context context;

	private Messenger shoutService;
	private ServiceConnection connection;
	private Boolean isBinded;

	public static NetworkInterface getInstance(Context context) {
		// TODO Move into context / shared state Android thang
		// TODO Multi-context support with Map context->instance
		// TODO Remove this Singleton while still keeping it a singleton
		if (instance == null) {
			instance = new NetworkInterface(context);
		}
		return instance;
	}

	private NetworkInterface(Context context) {
		this.context = context;
		this.isBinded = false;
		this.connection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "Connected to service.");
				shoutService = new Messenger(service);
				isBinded = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				shoutService = null;
				Log.i(TAG, "Disconnected from service unexpectedly.");
				isBinded = false;
			}

		};
		// bind to ShoutService
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
	 * @param shout
	 *            the shout to be sent out
	 * @return whether the notification is successful
	 */
	public boolean send(LocalShout shout) {
		if (isBinded) {
			Message msg = Message.obtain(null, NetworkService.NEW_SHOUT);
			msg.obj = shout.getDatabaseId();
			try {
				shoutService.send(msg);
				return true;
			} catch (RemoteException e) {
				Log.e(TAG, "error sending shout to NetworkService.", e);
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Unbind to ShoutService, must be called in the calling activity's
	 * onDestroy() method
	 */
	public void unBind() {
		if (isBinded == true) {
			context.unbindService(connection);
			isBinded = false;
		}
	}

}
