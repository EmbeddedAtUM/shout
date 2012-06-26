
package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.ShoutMessageUtility;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Asynchronously construct the reshout, and then start an
 * {@link OutgoingShoutTask} to send the reshout.
 * 
 * @author David Adrian 
 */
public class ReshoutTask extends AsyncTask<Integer, Void, Integer> {

	private Context context;

	public ReshoutTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		Shout parent = ShoutProviderContract.retrieveShoutById(context, params[0]);
		ShoutType type = ShoutMessageUtility.getShoutType(parent);
		switch (type) {
			case RESHOUT:
			case RECOMMENT:
				parent = parent.getParent();
			default:
				break;
		}
		IdManager idManager = new IdManager(context);
		if (idManager.userIsNotSet()) {
			return null;
		}
		Me me = idManager.getMe();
		if (parent.getSender().getPublicKey().equals(me.getPublicKey())) {
			return params[0];
		} else {
			ShoutCreator creator = new ShoutCreator(context);
			int reshoutId = creator.saveShout(DateTime.now(), null, parent);
			return reshoutId;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (result == null) {
			// TODO Make this situation not happen
			Toast.makeText(context, "Make a user before you Shout!", Toast.LENGTH_LONG).show();
		} else {
			OutgoingShoutTask sendTask = new OutgoingShoutTask(context, R.string.reshoutSuccess,
					R.string.reshoutFail);
			sendTask.execute(result);
		}
	}

}
