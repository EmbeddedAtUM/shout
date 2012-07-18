
package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.network.ErrorCode;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Asynchronously construct and broadcast the reshout, informing the user of
 * success or failure via a Toast.
 * 
 * @author David Adrian
 */
public class ReshoutTask extends AsyncTask<LocalShout, Void, ErrorCode> {
	private final static String TAG = ReshoutTask.class.getSimpleName();

	private Context context;
	private NetworkInterface network;
	private Me me;

	public ReshoutTask(Context context, NetworkInterface network, Me me) {
		this.context = context;
		this.network = network;
		this.me = me;
	}

	@Override
	protected ErrorCode doInBackground(LocalShout... shouts) {
		Shout shout = shouts[0];

		Shout parent;
		ShoutType type = ShoutMessageUtility.getShoutType(shout);
		switch (type) {
			case RESHOUT:
			case RECOMMENT:
				parent = shout.getParent();
				break;
			default:
				parent = shout;
				break;
		}

		ShoutCreator creator = new ShoutCreator(context);
		LocalShout reshout = creator.createReshout(DateTime.now(), parent, me);

		return network.send(reshout);
	}

	@Override
	protected void onPostExecute(ErrorCode result) {
		switch (result) {
			case SUCCESS:
				Toast.makeText(context, R.string.reshoutSuccess, Toast.LENGTH_SHORT)
						.show();
				break;
			case MANES_NOT_INSTALLED:
				Toast.makeText(context, "Send failed.  Please install MANES client.",
						Toast.LENGTH_LONG).show();
				break;
			case MANES_NOT_REGISTERED:
				Toast.makeText(context, "Send failed.  Please register with MANES client.",
						Toast.LENGTH_LONG).show();
				break;
			case IO_ERROR:
				Toast.makeText(context, R.string.reshoutFail, Toast.LENGTH_LONG)
						.show();
				break;
			case SHOUT_CHAIN_TOO_LONG:
				Toast.makeText(context, R.string.reshoutFail, Toast.LENGTH_LONG)
						.show();
				Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
				break;
			default:
				break;
		}
	}

}
