
package org.whispercomm.shout;

import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;

/**
 * Helper class for deleting shouts and comments locally. Local change will be
 * stored in content provider.
 * 
 * @author Bowen Xu
 */

public class ShoutEraser {

	private Context mContext;

	public ShoutEraser(Context context) {
		mContext = context;
	}

	public DeletedShout deleteShout(LocalShout target) {
		return ShoutProviderContract.deleteShout(mContext, target);
	}

	public boolean undoDeleteShout(int id) {
		return ShoutProviderContract.undoDeleteShout(mContext, id);
	}
}
