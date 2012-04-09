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
public class NaiveBroadcast implements NetworkProtocol {

	public static final String TAG = "******NaiveBroadcast******";
	/**
	 * instance of sendScheduler
	 */
	SendScheduler sendScheduler;
	ManesInterface manesIf;
	
	public NaiveBroadcast(ManesInterface manesIf){
		this.manesIf = manesIf;
		this.sendScheduler = new SendScheduler(manesIf);
	}

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
	public void clearUp() {
		sendScheduler.cancel();
	}

	@Override
	public void handleOutgoingAppShout(long ShoutId) {
		sendScheduler.schedule(new OneBroadcast(sendScheduler, ShoutId, 0),
				OneBroadcast.PERIOD);
	}

	@Override
	public void handleIncomingNetworkShout(NetworkShout shout) {
		ShoutProviderContract.storeShout(shout);
	}

}
