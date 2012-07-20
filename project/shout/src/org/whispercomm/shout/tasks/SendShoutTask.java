
package org.whispercomm.shout.tasks;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.ErrorCode;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.network.NetworkInterface.NotConnectedException;

/**
 * Asynchronously send a {@link Shout} over the network
 * 
 * @author David Adrian
 */
public class SendShoutTask extends AsyncTaskCallback<LocalShout, Void, ErrorCode> {

	private NetworkInterface network;

	/**
	 * @param context Application context
	 */
	public SendShoutTask(NetworkInterface network,
			AsyncTaskCompleteListener<ErrorCode> completeListener) {
		super(completeListener);
		this.network = network;
	}

	@Override
	protected ErrorCode doInBackground(LocalShout... shouts) {
		if (shouts.length < 1) {
			return ErrorCode.SUCCESS;
		}
		LocalShout shout = shouts[0];
		try {
			return network.send(shout);
		} catch (NotConnectedException e) {
			return ErrorCode.IO_ERROR;
		}
	}

}
