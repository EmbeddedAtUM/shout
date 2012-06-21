
package org.whispercomm.shout.tasks;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.id.SignatureUtility;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Asynchronously send a {@link Shout} over the network
 * 
 * @author David Adrian
 */
public class OutgoingShoutTask extends AsyncTask<Integer, Void, Boolean> {

	private Context context;
	private int successResId;
	private int failResId;

	/**
	 * @param context Application context
	 * @param successStringId Resource ID of a string to show in a Toast upon
	 *            success
	 * @param failureStringId Resource ID of a string to show in a Toast upon
	 *            failure
	 */
	public OutgoingShoutTask(Context context, int successStringId, int failureStringId) {
		this.context = context;
		this.successResId = successStringId;
		this.failResId = failureStringId;
	}

	@Override
	protected Boolean doInBackground(Integer... shouts) {
		if (shouts.length < 1) {
			return false;
		}
		SignatureUtility utility = new SignatureUtility(context);
		ShoutCreator creator = new ShoutCreator(context, utility);
		return creator.sendShout(shouts[0]);
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
