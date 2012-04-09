package org.whispercomm.shout.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.whispercomm.manes.client.maclib.ManesFrameTooLargeException;
import org.whispercomm.manes.client.maclib.ManesInterface;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * TimerTask of sending a given shout to the MANES network
 * 
 * @author Yue Liu
 * 
 */
public class OneBroadcast extends TimerTask {

	public static final String TAG = "******SendTask******";
	/**
	 * rebroadcast period in millisecond
	 */
	public static long PERIOD = 30 * 60 * 1000;
	/**
	 * total number of re-sends for each message
	 */
	public static int RESEND_NUM = 20;

	Timer sendScheduler;
	ManesInterface manesIf;
	Uri shout;
	Context callerContext;
	/**
	 * Number of times this same shout has been sent
	 */
	int execTime;

	public OneBroadcast(Timer scheduler, ManesInterface manesIf, Uri shout,
			Context context, int execTime) {
		this.sendScheduler = scheduler;
		this.manesIf = manesIf;
		this.shout = shout;
		this.callerContext = context;
		this.execTime = execTime;
	}

	@Override
	public void run() {
		// create a new thread to do the hard work
		new Thread(new SendRunnable()).run();
	}

	/**
	 * 
	 * Runnable to craft and send out the shout
	 * 
	 */
	class SendRunnable implements Runnable {

		@Override
		public void run() {
			// TODO query the content provider for the necessary data
			// send out the shout to MANES network
			try {
				NetworkShout shout = new NetworkShout(0, null, null, 0, null, null);
				byte[] shoutBytes = shout.toBytes();
				manesIf.send(shoutBytes);
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.getMessage());
			} catch (ManesFrameTooLargeException e) {
				// TODO  should try to handle this error
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			// update execTime
			execTime += 1;
			// schedule the next run
			if(execTime >= RESEND_NUM) return;
			else sendScheduler.schedule(new OneBroadcast(sendScheduler, manesIf,
					shout, callerContext, execTime), PERIOD);
		}

	}
}
