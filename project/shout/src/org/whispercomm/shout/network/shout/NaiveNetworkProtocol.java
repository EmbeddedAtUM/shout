
package org.whispercomm.shout.network.shout;

import java.util.Timer;
import java.util.TimerTask;

import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.util.Log;

/**
 * Simple implementation of network logic for Shout.
 * <p>
 * Received shouts that are valid (self-signatures check and
 * parents/grandparents exist) are stored in the content provider. Those with
 * invalid signatures are dropped.
 * <p>
 * Each outgoing shout is broadcast 20 times over a 10 hour at 30 minute
 * intervals.
 * 
 * @author Yue Liu
 * @author David R. Bild
 */
public class NaiveNetworkProtocol implements NetworkProtocol {
	public static final String TAG = NaiveNetworkProtocol.class.getSimpleName();

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
	private Timer sendScheduler;

	private Context context;

	private ShoutProtocol shoutProtocol;

	public NaiveNetworkProtocol(ShoutProtocol shoutProtocol, Context context) {
		this.context = context;
		this.shoutProtocol = shoutProtocol;
		this.sendScheduler = new Timer();
	}

	@Override
	public void initialize() {
		// Nothing to do here.
	}

	@Override
	public void cleanup() {
		sendScheduler.cancel();
	}

	@Override
	public void sendShout(final Shout shout) throws ShoutChainTooLongException,
			ManesNotRegisteredException {
		// Send once now, and then queue for further deliveries
		shoutProtocol.send(shout);

		// schedule periodic re-broadcast up to RESEND_NUM times.
		long delay = PERIOD;
		for (int i = 1; i < RESEND_NUM; i++) {
			sendScheduler.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						shoutProtocol.send(shout);
					} catch (ManesNotRegisteredException e) {
						Log.e(TAG, "send() failed because not registered.", e);
					}
				}
			}, delay);
			delay += PERIOD;
		}
	}

	@Override
	public void receive(Shout shout) {
		ShoutProviderContract.saveShout(context, shout);
	}

}
