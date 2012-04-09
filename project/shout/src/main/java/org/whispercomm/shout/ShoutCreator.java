package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;

import org.joda.time.DateTime;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.app.Activity;
import android.util.Log;

public class ShoutCreator {

	static final String TAG = "******ShoutCreator******";
	
	NetworkInterface networkIf;
	SignatureUtility signUtility;
	User user;
	
	public ShoutCreator(Activity callerActivity){
		this.networkIf = new NetworkInterface(callerActivity);
		this.signUtility = new SignatureUtility(callerActivity);
		this.user = signUtility.getUser();
	}
	
	public boolean createShout(DateTime timestamp, String content, Shout shoutOri){
		
		//generate a new shout with its signature
		byte[] signature;
		try {
			signature = signUtility.genShoutSignature(timestamp, user, content, shoutOri);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		Shout shout = new SimpleShout(timestamp, user, content, shoutOri, signature);
	
		// insert the shout to database and get its shout_id back
		long shoutId = ShoutProviderContract.storeShout(shout);
		
		//call networkUtility to send the new shout out
		networkIf.send(shoutId);
		return true;
	}
	
}
