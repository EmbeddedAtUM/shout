
package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.id.SignatureUtility;

import android.content.Context;
import android.os.AsyncTask;

public class ShoutTask extends AsyncTask<String, Void, Integer> {

	private Context context;

	public ShoutTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(String... message) {
		SignatureUtility utility = new SignatureUtility(context);
		ShoutCreator creator = new ShoutCreator(context, utility);
		int shoutId = creator.saveShout(DateTime.now(), message[0], null);
		return shoutId;
	}

	@Override
	protected void onPostExecute(Integer result) {
		OutgoingShoutTask sendTask = new OutgoingShoutTask(context, R.string.shoutSuccess,
				R.string.shoutFail);
		sendTask.execute(result);
	}

}
