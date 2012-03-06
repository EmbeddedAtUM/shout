package org.whispercomm.shout.network;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * 
 * UI side interface for establishing channels to notify new shout to send out.
 * <p>
 * Each UI activity should initiate a ShoutClient object in order to notify the
 * network protocol.
 * 
 * @author Yue Liu
 */
public class ShoutClient {

	public static String TAG = "ShoutClient";

	Activity callerActivity;
	Messenger shoutService;
	ServiceConnection connection;
	Boolean isBinded;

	public ShoutClient(Activity activity) {
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
		callerActivity.bindService(new Intent(callerActivity,
				ShoutService.class), connection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Notifies ShoutService to shout out the given message.
	 * <p>
	 * The method returns immediately, with indications of whether the
	 * notification is successful. If not, the caller should either wait and try
	 * later, or give up.
	 * 
	 * @param shout
	 *            the uri of the message to be shouted out
	 * @return whether the notification is successful
	 */
	public boolean shoutThisMessage(Uri shout) {
		if (isBinded) {
			Message msg = Message.obtain(null, ShoutService.NEW_SHOUT);
			// TODO Add the _ID of the shout to msg.arg1
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
