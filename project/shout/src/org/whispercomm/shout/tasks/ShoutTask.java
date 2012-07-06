package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutCreator;

import android.content.Context;
import android.os.AsyncTask;

public class ShoutTask extends AsyncTask<String, Void, LocalShout> {

	private Context context;

	public ShoutTask(Context context) {
		this.context = context;
	}

	@Override
	protected LocalShout doInBackground(String... params) {
		String message = params[0];

		ShoutCreator creator = new ShoutCreator(context);
		return creator.createShout(DateTime.now(), message);
	}

	@Override
	protected void onPostExecute(LocalShout shout) {
		OutgoingShoutTask sendTask = new OutgoingShoutTask(context,
				R.string.shoutSuccess, R.string.shoutFail);
		sendTask.execute(shout);
	}

}
