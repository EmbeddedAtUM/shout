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

import android.app.Activity;
import android.util.Log;

public class ShoutCreator {

	static final String TAG = "******ShoutCreator******";

	Activity callerActivity;
	NetworkInterface networkIf;
	SignatureUtility signUtility;
	User user;

	public ShoutCreator(Activity callerActivity)
			throws Exception {
		this.callerActivity = callerActivity;
		this.networkIf = new NetworkInterface(this.callerActivity);
		this.signUtility = new SignatureUtility(this.callerActivity);
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
			Shout shoutOri) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException, SignatureException,
			UserNotInitiatedException {

		// generate a new shout with its signature
		byte[] signature;
		try {
			signature = signUtility.genShoutSignature(timestamp, user, content,
					shoutOri);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
			return false;
		} catch (Exception e) {
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
