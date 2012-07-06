package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutCreator;

import android.content.Context;
import android.os.AsyncTask;

public class ShoutTask extends AsyncTask<String, Void, Integer> {

	private Context context;

	public ShoutTask(Context context) {
		this.context = context;
	}

	@Override
	protected Integer doInBackground(String... params) {
		String message = params[0];

		ShoutCreator creator = new ShoutCreator(context);
		LocalShout shout = creator.createShout(DateTime.now(), message);
		return shout.getDatabaseId();
	}

	@Override
	protected void onPostExecute(Integer result) {
		OutgoingShoutTask sendTask = new OutgoingShoutTask(context,
				R.string.shoutSuccess, R.string.shoutFail);
		sendTask.execute(result);
	}

}
