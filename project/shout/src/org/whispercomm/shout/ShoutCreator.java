
package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;

import org.joda.time.DateTime;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.util.Log;

public class ShoutCreator {

	static final String TAG = ShoutCreator.class.getSimpleName();

	/**
	 * Create a new shout given user-generated message. Store the Shout in the
	 * database and send it out over the network.
	 * 
	 * @param timestamp
	 * @param content
	 * @param shoutOri
	 * @return
	 */
	public static boolean createShout(DateTime timestamp, String content,
			Shout shoutOri) {
		Context context = SingletonContext.getContext();
		Shout shout = saveShout(timestamp, content, shoutOri, context);
		return sendShout(shout, context);
	}

	public static Shout saveShout(DateTime timestamp, String content, Shout shoutOri,
			Context context) {
		SignatureUtility signUtility = SignatureUtility.getInstance();
		User user = signUtility.getUser();
		try {
			byte[] signature = signUtility.genShoutSignature(timestamp, user, content, shoutOri);
			Shout shout = new SimpleShout(timestamp, user, content, shoutOri, signature);
			int shoutId = ShoutProviderContract.storeShout(context, shout);
			if (shoutId >= 0) {
				return shout;
			} else {
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}

	public static boolean sendShout(Shout shout, Context context) {
		int shoutId = ShoutProviderContract.storeShout(context, shout);
		if (shoutId >= 0) {
			NetworkInterface networkIf = NetworkInterface.getInstance();
			return networkIf.send(shoutId);
		} else {
			return false;
		}
	}
}
