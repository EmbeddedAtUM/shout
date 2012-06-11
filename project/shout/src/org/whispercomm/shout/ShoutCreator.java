package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.joda.time.DateTime;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;
import android.util.Log;

public class ShoutCreator {

	static final String TAG = "******ShoutCreator******";

	Context context;
	NetworkInterface networkIf;
	SignatureUtility signUtility;
	User user;

	public ShoutCreator(Context context) {
		this.context = context;
		this.networkIf = new NetworkInterface(context);
		this.signUtility = new SignatureUtility(context);
		this.user = signUtility.getUser();
	}

	/**
	 * Create a new shout given user-generated message.
	 * 
	 * @param timestamp
	 * @param content
	 * @param shoutOri
	 * 
	 * @return
	 * 
	 * @throws UserNotInitiatedException
	 * @throws SignatureException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public boolean createShout(DateTime timestamp, String content,
			Shout shoutOri) {

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
		long shoutId = ShoutProviderContract.storeShout(null, shout);

		// call networkUtility to send the new shout out
		networkIf.send(shoutId);
		return true;
	}

}
