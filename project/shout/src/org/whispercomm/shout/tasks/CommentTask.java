package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;

import android.content.Context;
import android.os.AsyncTask;

public class CommentTask extends AsyncTask<String, Void, Integer> {

	private Context context;
	private Shout parent;

	public CommentTask(Context context, Shout parent) {
		this.context = context;
		this.parent = parent;
	}

	@Override
	protected Integer doInBackground(String... params) {
		String message = params[0];

		ShoutCreator creator = new ShoutCreator(context);
		LocalShout comment = creator.createComment(DateTime.now(), message,
				parent);
		return comment.getDatabaseId();
	}

	@Override
	protected void onPostExecute(Integer result) {
		OutgoingShoutTask sendTask = new OutgoingShoutTask(context,
				R.string.commentSuccess, R.string.commentFail);
		sendTask.execute(result);
	}

}
