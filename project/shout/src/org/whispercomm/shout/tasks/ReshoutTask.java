package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Asynchronously construct and broadcast the reshout, informing the user of
 * success or failure via a Toast.
 * 
 * @author David Adrian
 */
public class ReshoutTask extends AsyncTask<LocalShout, Void, Boolean> {

	private Context context;
	private Me me;

	public ReshoutTask(Context context, Me me) {
		this.context = context;
		this.me = me;
	}

	@Override
	protected Boolean doInBackground(LocalShout... shouts) {
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

		return NetworkInterface.getInstance(context).send(reshout);
	}

	@Override
	protected void onPostExecute(Boolean success) {
		if (success) {
			Toast.makeText(context, R.string.reshoutSuccess, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(context, R.string.reshoutFail, Toast.LENGTH_LONG)
					.show();
		}
	}

}
