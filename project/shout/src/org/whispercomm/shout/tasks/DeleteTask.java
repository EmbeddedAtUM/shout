
package org.whispercomm.shout.tasks;

import org.whispercomm.shout.DeletedShout;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.ShoutEraser;

import android.content.Context;

/**
 * Delete the shout and save it to the content provider
 * 
 * @author Bowen Xu
 */

public class DeleteTask extends AsyncTaskCallback<Void, Void, DeletedShout> {

	private Context mContext;
	private LocalShout mTarget;

	public DeleteTask(Context context, AsyncTaskCompleteListener<DeletedShout> completeListener,
			LocalShout target) {
		super(completeListener);
		mContext = context;
		mTarget = target;

	}

	@Override
	protected DeletedShout doInBackground(Void... params) {

		ShoutEraser shoutEraser = new ShoutEraser(mContext);
		/*
		 * if (mTarget.getParent() == null) { return shoutEraser.deleteShout();
		 * } else { return shoutEraser.deleteComment(); }
		 */
		return shoutEraser.deleteShout(mTarget);
	}

}
