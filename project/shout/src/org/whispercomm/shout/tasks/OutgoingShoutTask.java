package org.whispercomm.shout.tasks;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.network.NetworkInterface;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Asynchronously send a {@link Shout} over the network
 * 
 * @author David Adrian
 */
public class OutgoingShoutTask extends AsyncTask<LocalShout, Void, Boolean> {

	private NetworkInterface network;

	private Context context;
	private int successResId;
	private int failResId;

	/**
	 * @param context
	 *            Application context
	 * @param successStringId
	 *            Resource ID of a string to show in a Toast upon success
	 * @param failureStringId
	 *            Resource ID of a string to show in a Toast upon failure
	 */
	public OutgoingShoutTask(Context context, int successStringId,
			int failureStringId) {
		this.context = context;
		this.successResId = successStringId;
		this.failResId = failureStringId;
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

	@Override
	protected void onPostExecute(Boolean result) {
		if (result.booleanValue()) {
			Toast.makeText(context, successResId, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, failResId, Toast.LENGTH_LONG).show();
		}
	}

}
