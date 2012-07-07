package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;

import android.content.Context;
import android.os.AsyncTask;

public class CommentTask extends AsyncTask<String, Void, LocalShout> {

	private Context context;
	private Shout parent;
	private Me me;

	public CommentTask(Context context, Shout parent) {
		this.context = context;
		this.parent = parent;
		try {
			this.me = new IdManager(context).getMe();
		} catch (UserNotInitiatedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected LocalShout doInBackground(String... params) {
		String message = params[0];

		ShoutCreator creator = new ShoutCreator(context);
		return creator.createComment(DateTime.now(), message, parent, me);
	}

	@Override
	protected void onPostExecute(LocalShout comment) {
		SendShoutTask sendTask = new SendShoutTask(context,
				R.string.commentSuccess, R.string.commentFail);
		sendTask.execute(comment);
	}
}
