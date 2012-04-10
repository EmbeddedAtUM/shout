package org.whispercomm.shout;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public abstract class AbstractShout implements Shout {

	private byte[] hashCode;

	@Override
	public byte[] getHash() {
		if (hashCode == null) {
			try {
				this.hashCode = SignatureUtility.genShoutHash(getTimestamp(),
						getSender(), getContent(), getOriginalShout());
			} catch (UnsupportedEncodingException e) {
				Log.e("AbstractShout",
						"Cannot generate this shout's hash becasue "
								+ e.getMessage());
				return null;
			} catch (NoSuchAlgorithmException e) {
				Log.e("AbstractShout",
						"Cannot generate this shout's hash becasue "
								+ e.getMessage());
				return null;
			}
		}
		return hashCode;
	}

}
