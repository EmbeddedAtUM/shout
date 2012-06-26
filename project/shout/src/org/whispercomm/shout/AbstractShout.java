package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;

import org.whispercomm.shout.id.SignatureUtility;

import android.util.Log;

public abstract class AbstractShout implements UnsignedShout {

	private static final String TAG = AbstractShout.class.getSimpleName();
	
	private byte[] hashCode;

	@Override
	public byte[] getHash() {
		if (hashCode == null) {
			try {
				this.hashCode = SignatureUtility.genShoutHash(this);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return hashCode;
	}
	
	@Override
	public ShoutType getType() {
		return ShoutMessageUtility.getShoutType(this);
	}

}
