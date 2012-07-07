package org.whispercomm.shout.tasks;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.NetworkInterface;

/**
 * Asynchronously send a {@link Shout} over the network
 * 
 * @author David Adrian
 */
public class SendShoutTask extends AsyncTaskCallback<LocalShout, Void, Boolean> {

	private NetworkInterface network;

	/**
	 * @param context
	 *            Application context
	 */
	public SendShoutTask(NetworkInterface network,
			AsyncTaskCompleteListener<Boolean> completeListener) {
		super(completeListener);
		this.network = network;
	}

	@Override
	protected Boolean doInBackground(LocalShout... shouts) {
		if (shouts.length < 1) {
			return false;
		}
		LocalShout shout = shouts[0];
		return network.send(shout);
	}

}
