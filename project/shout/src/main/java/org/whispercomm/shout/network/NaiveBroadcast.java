package org.whispercomm.shout.network;

import java.util.Timer;

import android.net.Uri;

/**
 * Service in charge of sending and receiving shouts from the MANES network
 * 
 * @author Yue Liu
 * 
 */
public class NaiveBroadcast extends NetworkUtility {

	public static final String TAG = "******NaiveBroadcast******";
	/**
	 * scheduler that actually pushes shouts out into MANES network
	 */
	Timer sendScheduler;

	@Override
	protected boolean initialize() {
		this.sendScheduler = new Timer();
		return true;
	}

	@Override
	protected void clearUp() {
		sendScheduler.cancel();	
	}

	@Override
	protected void handleIncomingAppShout(Uri shoutUri) {
		sendScheduler.schedule(new OneBroadcast(sendScheduler, manesIf,
				shoutUri, NaiveBroadcast.this, 0), OneBroadcast.PERIOD);
	}

	@Override
	protected void handleIncomingNetworkShout(NetworkShout shout) {
		// TODO Update the database with net shout
	}
	
}
