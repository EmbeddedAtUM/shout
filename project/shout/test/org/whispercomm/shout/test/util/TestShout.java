
package org.whispercomm.shout.test.util;

import org.joda.time.DateTime;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;

public class TestShout extends TestUnsignedShout implements Shout {

	public byte[] hash = null;
	public byte[] signature = null;

	public TestShout(User sender, Shout parent, String message,
			DateTime timestamp, byte[] signature, byte[] hash) {
		super(sender, parent, message, timestamp);
		this.signature = signature;
		this.hash = hash;
	}

	@Override
	public byte[] getHash() {
		return hash;
	}

	@Override
	public byte[] getSignature() {
		return signature;
	}

}
