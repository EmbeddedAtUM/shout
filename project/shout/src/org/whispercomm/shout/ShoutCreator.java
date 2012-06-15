
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
	
	private Context context;
	private SignatureUtility signUtility;
	
	public ShoutCreator(Context context, SignatureUtility signUtility) {
		this.context = context;
		this.signUtility = signUtility;
	}

	/**
	 * Create a new shout given user-generated message. Store the Shout in the
	 * database and send it out over the network.
	 * 
	 * @param timestamp
	 * @param content
	 * @param shoutOri
	 * @param context TODO
	 * @return
	 */
	public boolean createShout(DateTime timestamp, String content,
			Shout shoutOri) {
		Shout shout = saveShout(timestamp, content, shoutOri);
		return sendShout(shout);
	}

	public Shout saveShout(DateTime timestamp, String content, Shout shoutOri) {
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

	public boolean sendShout(Shout shout) {
		int shoutId = ShoutProviderContract.storeShout(context, shout);
		if (shoutId >= 0) {
			NetworkInterface networkIf = NetworkInterface.getInstance(context);
			return networkIf.send(shoutId);
		} else {
			return false;
		}
	}
}
