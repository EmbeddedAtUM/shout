
package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.provider.ShoutProviderContract;
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
public class ReshoutTask extends AsyncTask<Integer, Void, Integer> {

	private Context context;

	public ReshoutTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		LocalShout parent = ShoutProviderContract.retrieveShoutById(context, params[0]);
		ShoutType type = ShoutMessageUtility.getShoutType(parent);
		switch (type) {
			case RESHOUT:
			case RECOMMENT:
				parent = parent.getParent();
			default:
				break;
		}
		IdManager idManager = new IdManager(context);
		Me me;
		try {
			me = idManager.getMe();
		} catch (UserNotInitiatedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (parent.getSender().getPublicKey().equals(me.getPublicKey())) {
			return params[0];
		} else {
			LocalShout reshout = parent.getReshout(me);
			int reshoutId;
			if (reshout == null) {
				ShoutCreator creator = new ShoutCreator(context);
				reshoutId = creator.saveShout(DateTime.now(), null, parent);
			} else {
				reshoutId = reshout.getDatabaseId();
			}
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
