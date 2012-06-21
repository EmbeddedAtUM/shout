
package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.R;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.os.AsyncTask;

public class CommentTask extends AsyncTask<String, Void, Integer> {

	private Context context;
	private int parentId;

	public CommentTask(Context context, int parentId) {
		this.context = context;
		this.parentId = parentId;
	}

	@Override
	protected Integer doInBackground(String... params) {
		SignatureUtility utility = new SignatureUtility(context);
		ShoutCreator creator = new ShoutCreator(context, utility);
		Shout parent = ShoutProviderContract.retrieveShoutById(context, parentId);
		int id = creator.saveShout(DateTime.now(), params[0], parent);
		return id;
	}

	@Override
	protected void onPostExecute(Integer result) {
		OutgoingShoutTask sendTask = new OutgoingShoutTask(context, R.string.commentSuccess,
				R.string.commentFail);
		sendTask.execute(result);
	}

}
