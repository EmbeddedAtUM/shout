package org.whispercomm.shout;

import org.joda.time.DateTime;

public class SimpleShout extends SimpleUnsignedShout implements Shout{

	private byte[] signature;

	public SimpleShout(DateTime timestamp, User sender, String message,
			Shout parent, byte[] signature) {
		super(timestamp, sender, message, parent);
		this.signature = signature;
	}

	@Override
	public byte[] getSignature() {
		return this.signature;
	}

}
