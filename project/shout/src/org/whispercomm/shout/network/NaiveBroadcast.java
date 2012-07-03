package org.whispercomm.shout.network;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.util.Log;

/**
 * Service in charge of sending and receiving shouts from the MANES network
 * 
 * @author Yue Liu
 * 
 */
public class NaiveBroadcast implements NetworkProtocol {

	public static final String TAG = "******NaiveBroadcast******";

	/**
	 * rebroadcast period in millisecond
	 */
	public static long PERIOD = 30 * 60 * 1000;
	/**
	 * total number of re-sends for each message
	 */
	public static int RESEND_NUM = 20;
	/**
	 * timer for scheduling periodic re-broadcast
	 */
	Timer sendScheduler;
	ManesInterface manesIf;
	Context context;

	public NaiveBroadcast(ManesInterface manesIf, Context context) {
		this.manesIf = manesIf;
		this.sendScheduler = new Timer();
		this.context = context;
	}

	@Override
	public void clearUp() {
		sendScheduler.cancel();
	}

	@Override
	public void handleOutgoingAppShout(int shoutId) {
		// get the byte[] representation of the outgoing shout.
		NetworkShout shout = new NetworkShout(shoutId, context);
		final byte[] shoutBytes;
		try {
			shoutBytes = NetworkShout.toNetworkBytes(shout);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
			return;
		} catch (ShoutChainTooLongException e) {
			Log.e(TAG, e.getMessage());
			return;
		}
		// schedule periodic re-broadcast up to RESEND_NUM times.
		long delay = 0;
		for (int i = 1; i <= RESEND_NUM; i++) {
			sendScheduler.schedule(new TimerTask() {
				@Override
				public void run() {
					manesIf.send(shoutBytes);
				}
			}, delay);
			delay += PERIOD;
		}
	}

	@Override
	public void handleIncomingNetworkShout(NetworkShout shout) {
		ShoutProviderContract.storeShout(context, shout);
	}

}
