
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
		SignatureUtility signUtility = SignatureUtility.getInstance();
		User user = signUtility.getUser();

		// generate a new shout with its signature
		byte[] signature;
		try {
			signature = signUtility.genShoutSignature(timestamp, user, content,
					shoutOri);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		Shout shout = new SimpleShout(timestamp, user, content, shoutOri,
				signature);

		// insert the shout to database and get its shout_id back
		int shoutId = ShoutProviderContract.storeShout(context, shout);

		// call networkUtility to send the new shout out
		NetworkInterface networkIf = NetworkInterface.getInstance();
		return networkIf.send(shoutId);
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
