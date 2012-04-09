package org.whispercomm.shout.network;

import org.whispercomm.shout.Shout;

import android.app.Activity;
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
 * 
 * UI side interface for establishing channels to notify new shouts to send out.
 * <p>
 * Each UI activity should initiate a NetworkInterface object in order to notify
 * the network protocol.
 * 
 * @author Yue Liu
 */
public class NetworkInterface {

	public static String TAG = "******NetworkInterface******";

	Activity callerActivity;
	Messenger shoutService;
	ServiceConnection connection;
	Boolean isBinded;

	public NetworkInterface(Activity activity,
			@SuppressWarnings("rawtypes") Class networkUtility) {
		this.callerActivity = activity;
		this.isBinded = false;
		this.connection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				shoutService = new Messenger(service);
				isBinded = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				shoutService = null;
				isBinded = false;
			}

		};
		// bind to ShoutService
		callerActivity.bindService(new Intent(callerActivity, networkUtility),
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
	public boolean send(Shout shout) {
		if (isBinded) {
			Message msg = Message.obtain(null, NaiveBroadcast.NEW_SHOUT);
			msg.obj = shout;
			try {
				shoutService.send(msg);
			} catch (RemoteException e) {
				Log.i(TAG, e.getMessage());
				return false;
			}
			return true;
		} else
			return false;
	}

	/**
	 * Unbind to ShoutService, must be called in the calling activity's
	 * onDestroy() method
	 */
	public void unBind() {
		if (isBinded == true) {
			callerActivity.unbindService(connection);
			isBinded = false;
		}
	}

}
