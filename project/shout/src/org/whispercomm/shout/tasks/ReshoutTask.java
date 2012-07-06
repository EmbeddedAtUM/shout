package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Asynchronously construct the reshout, and then start an
 * {@link OutgoingShoutTask} to send the reshout.
 * 
 * @author David Adrian
 */
public class ReshoutTask extends AsyncTask<LocalShout, Void, LocalShout> {

	private Context context;

	public ReshoutTask(Context context) {
		this.context = context;
	}

	@Override
	protected LocalShout doInBackground(LocalShout... shouts) {
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
		return creator.createReshout(DateTime.now(), parent);
	}

	@Override
	protected void onPostExecute(LocalShout reshout) {
		if (reshout == null) {
			// TODO Make this situation not happen
			Toast.makeText(context, "Make a user before you Shout!",
					Toast.LENGTH_LONG).show();
		} else {
			OutgoingShoutTask sendTask = new OutgoingShoutTask(context,
					R.string.reshoutSuccess, R.string.reshoutFail);
			sendTask.execute(reshout);
		}
	}

}
