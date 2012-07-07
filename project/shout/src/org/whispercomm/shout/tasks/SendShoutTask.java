package org.whispercomm.shout.tasks;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.NetworkInterface;

import android.content.Context;
import android.widget.Toast;

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
	 * @param successStringId
	 *            Resource ID of a string to show in a Toast upon success
	 * @param failureStringId
	 *            Resource ID of a string to show in a Toast upon failure
	 */
	public SendShoutTask(final Context context, final int successStringId,
			final int failureStringId) {
		super(new AsyncTaskCompleteListener<Boolean>() {
			@Override
			public void onComplete(Boolean result) {
				if (result) {
					Toast.makeText(context, successStringId, Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(context, failureStringId, Toast.LENGTH_LONG)
							.show();
				}
			}
		});
		this.network = NetworkInterface.getInstance(context);
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
