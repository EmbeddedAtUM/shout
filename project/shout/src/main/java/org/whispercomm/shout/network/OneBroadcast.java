package org.whispercomm.shout.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.TimerTask;

import org.whispercomm.manes.client.maclib.ManesFrameTooLargeException;
import org.whispercomm.shout.network.NaiveBroadcast.SendScheduler;

import android.util.Log;

/**
 * TimerTask of sending a given shout to the MANES network
 * 
 * @author Yue Liu
 * 
 */
public class OneBroadcast extends TimerTask {

	public static final String TAG = "******OneBroadcast******";
	/**
	 * rebroadcast period in millisecond
	 */
	public static long PERIOD = 30 * 60 * 1000;
	/**
	 * total number of re-sends for each message
	 */
	public static int RESEND_NUM = 20;

	SendScheduler sendScheduler;
	long shoutId;
	/**
	 * Number of times this same shout has been sent
	 */
	int execTime;

	public OneBroadcast(SendScheduler scheduler, long shoutId, int execTime) {
		this.sendScheduler = scheduler;
		this.shoutId = shoutId;
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
			
			// send out the shout to MANES network
			try {
				NetworkShout shout = new NetworkShout(shoutId);
				byte[] shoutBytes = shout.toNetworkBytes();
				sendScheduler.manesIf.send(shoutBytes);
			} catch (ShoutChainTooLongException e) {
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
			else sendScheduler.schedule(new OneBroadcast(sendScheduler, 
					shoutId, execTime), PERIOD);
		}

	}
}
