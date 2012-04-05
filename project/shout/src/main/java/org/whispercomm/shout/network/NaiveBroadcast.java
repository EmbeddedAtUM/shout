package org.whispercomm.shout.network;

import java.util.Timer;

import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;

/**
 * Service in charge of sending and receiving shouts from the MANES network
 * 
 * @author Yue Liu
 * 
 */
public class NaiveBroadcast extends NetworkUtility {

	public static final String TAG = "******NaiveBroadcast******";
	/**
	 * instance of sendScheduler
	 */
	SendScheduler sendScheduler;

	/**
	 * scheduler that pushes shouts out into MANES network
	 */
	class SendScheduler extends Timer {
		ManesInterface manesIf;

		public SendScheduler(ManesInterface manesIf) {
			this.manesIf = manesIf;
		}
	}

	@Override
	protected boolean initialize() {
		this.sendScheduler = new SendScheduler(manesIf);
		return true;
	}

	@Override
	protected void clearUp() {
		sendScheduler.cancel();
	}

	@Override
	protected void handleIncomingAppShout(long ShoutId) {
		sendScheduler.schedule(new OneBroadcast(sendScheduler, ShoutId, 0),
				OneBroadcast.PERIOD);
	}

	@Override
	protected void handleIncomingNetworkShout(NetworkShout shout) {
		ShoutProviderContract.storeShout(shout);
	}

}
