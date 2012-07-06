package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;

// TODO Switch to static after sig utility is static
public class ShoutCreator {

	static final String TAG = ShoutCreator.class.getSimpleName();

	private Context context;
	private IdManager idManager;

	public ShoutCreator(Context context) {
		this.context = context;
		idManager = new IdManager(context);
	}

	/**
	 * Create a new shout given user-generated message. Store the Shout in the
	 * database and send it out over the network.
	 * 
	 * @param timestamp
	 * @param content
	 * @param shoutOri
	 * @param context
	 *            TODO
	 * @return
	 */
	@Deprecated
	public boolean createAndSendShout(DateTime timestamp, String content,
			Shout shoutOri) {
		int shoutId = saveShout(timestamp, content, shoutOri);
		return sendShout(shoutId);
	}

	@Deprecated
	public int saveShout(DateTime timestamp, String content, Shout parent) {
		Me me;
		try {
			me = idManager.getMe();
		} catch (UserNotInitiatedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return -1;
		}

		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, me,
				content, parent);
		byte[] signature = SignatureUtility.generateSignature(unsigned, me);
		Shout shout = new SimpleShout(timestamp, me, content, parent, signature);
		int shoutId = ShoutProviderContract.storeShout(context, shout);
		return shoutId;
	}

	@Deprecated
	public boolean sendShout(int shoutId) {
		if (shoutId > 0) {
			NetworkInterface networkIf = NetworkInterface.getInstance(context);
			return networkIf.send(shoutId);
		} else {
			return false;
		}
	}

	@Deprecated
	public boolean sendShout(Shout shout) {
		int shoutId = ShoutProviderContract.storeShout(context, shout);
		if (shoutId > 0) {
			NetworkInterface networkIf = NetworkInterface.getInstance(context);
			return networkIf.send(shoutId);
		} else {
			return false;
		}
	}
}
