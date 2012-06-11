package org.whispercomm.shout;

import org.whispercomm.shout.id.SignatureUtility;

public abstract class AbstractShout implements Shout {

	private byte[] hashCode;

	@Override
	public byte[] getHash() {
		if (hashCode == null) {
			this.hashCode = SignatureUtility.genShoutHash(getTimestamp(),
					getSender(), getMessage(), getParent());
		}
		return hashCode;
	}

}
