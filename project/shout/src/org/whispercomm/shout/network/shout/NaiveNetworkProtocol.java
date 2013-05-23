
package org.whispercomm.shout.network.shout;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.provider.ShoutColorContract;
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
	public static int PERIOD = 30 * 60 * 1000;

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
	public void sendShout(final Shout shout) throws
			ManesNotRegisteredException {
		// Send once now, and then queue for further deliveries
		send(shout);

		// schedule periodic re-broadcast up to RESEND_NUM times.
		DateTime now = DateTime.now();
		for (int i = 1; i <= RESEND_NUM; ++i) {
			sendScheduler.schedule(new SendShoutTask(shout), now.plusMillis(i * PERIOD).toDate());
		}
	}

	private void send(Shout shout) throws ManesNotRegisteredException {
		shoutProtocol.send(shout);
	}

	@Override
	public void receive(Shout shout) {
		ShoutColorContract.saveShoutBorder(context, shout.getSender());
		ShoutProviderContract.saveShout(context, shout);
	}

	private class SendShoutTask extends TimerTask {

		private final Shout shout;

		public SendShoutTask(Shout shout) {
			this.shout = shout;
		}

		@Override
		public void run() {
			try {
				send(shout);
			} catch (ManesNotRegisteredException e) {
				Log.w(TAG, "send() failed because not registered.", e);
			}
		}
	}

}
