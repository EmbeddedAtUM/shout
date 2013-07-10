
package org.whispercomm.shout.tasks;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.ShoutEraser;

import android.content.Context;

/**
 * Delete the shout and save it to the content provider
 * 
 * @author Bowen Xu
 */

public class DeleteTask extends AsyncTaskCallback<Void, Void, Void> {

	private Context mContext;
	private LocalShout mTarget;

	public DeleteTask(Context context, AsyncTaskCompleteListener<Void> completeListener,
			LocalShout target) {
		super(completeListener);
		mContext = context;
		mTarget = target;

	}

	@Override
	protected Void doInBackground(Void... params) {

		ShoutEraser shoutEraser = new ShoutEraser(mContext);
		/*
		 * if (mTarget.getParent() == null) { return shoutEraser.deleteShout();
		 * } else { return shoutEraser.deleteComment(); }
		 */
		shoutEraser.deleteShout(mTarget);
		return null;
	}

}
