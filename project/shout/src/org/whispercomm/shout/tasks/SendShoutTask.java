
package org.whispercomm.shout.tasks;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.service.NetworkInterface;

/**
 * Asynchronously send a {@link Shout} over the network
 * 
 * @author David Adrian
 */
public class SendShoutTask extends AsyncTaskCallback<LocalShout, Void, SendResult> {

	private NetworkInterface network;

	/**
	 * @param context Application context
	 */
	public SendShoutTask(NetworkInterface network,
			AsyncTaskCompleteListener<SendResult> completeListener) {
		super(completeListener);
		this.network = network;
	}

	@Override
	protected SendResult doInBackground(LocalShout... shouts) {
		if (shouts.length < 1) {
			return new SendResult();
		}

		LocalShout shout = shouts[0];
		return SendResult.encapsulateSend(network, shout);
	}

}
