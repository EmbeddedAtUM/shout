
package org.whispercomm.shout.test.util;

import org.joda.time.DateTime;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.DsaSignature;

public class TestShout extends TestUnsignedShout implements Shout {

	public byte[] hash = null;
	public DsaSignature signature = null;

	public TestShout(User sender, Shout parent, String message,
			DateTime timestamp, DsaSignature signature, byte[] hash) {
		super(sender, parent, message, timestamp);
		this.signature = signature;
		this.hash = hash;
	}

	@Override
	public byte[] getHash() {
		return hash;
	}

	@Override
	public DsaSignature getSignature() {
		return signature;
	}

}
