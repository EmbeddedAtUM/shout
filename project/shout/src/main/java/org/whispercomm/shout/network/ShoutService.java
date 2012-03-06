package org.whispercomm.shout.network;

import java.util.Timer;

import org.whispercomm.manes.client.maclib.ManesInterface;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Service in charge of sending and receiving shouts from the MANES network
 * 
 * @author Yue Liu
 * 
 */
public class ShoutService extends Service {

	public static final int NEW_SHOUT = 1;
	public static final int APP_ID = 74688;// "shout" on a phone keyboard
	public static final String TAG = "ShoutService";

	/**
	 * scheduler that actually pushes shouts out into MANES network
	 */
	Timer sendScheduler;
	/**
	 * channel for receiving activity shout request
	 */
	Messenger shoutMessenger;
	ManesInterface manesIf;
	ServiceStateKeeper state;

	/**
	 * Handle shouts from UI
	 */
	class ShoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == NEW_SHOUT) {
				// put the send task onto the timer
				sendScheduler.schedule(new SendTask(sendScheduler, manesIf,
						msg.arg1, ShoutService.this, 0), SendTask.PERIOD);
			}
		}
	}

	/**
	 * 
	 * Keeps the state of the service and used for shutting down any child
	 * threads of the service
	 * 
	 */
	protected static class ServiceStateKeeper {
		private boolean running;

		public ServiceStateKeeper() {
			this.running = true;
		}

		public boolean isRunning() {
			return running;
		}
	}

	@Override
	public void onCreate() {
		this.sendScheduler = new Timer(true);
		this.shoutMessenger = new Messenger(new ShoutHandler());
		try {
			this.manesIf = new ManesInterface(APP_ID, this);
		} catch (RemoteException e) {
			// TODO  what to do if cannot connect to manesinterface?
			Log.e(TAG, e.getMessage());
			stopSelf();
		}
		this.state = new ShoutService.ServiceStateKeeper();
		// start receiver thread
		new Thread(new Receiver(manesIf, this, state)).run();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return shoutMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		sendScheduler.cancel();
		state.running = false;
	}
}
