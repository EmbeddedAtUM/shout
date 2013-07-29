
package org.whispercomm.shout.test.util;

import org.joda.time.DateTime;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.DsaSignature;

public class TestShout extends TestUnsignedShout implements Shout {

	public int version;
	public Hash hash = null;
	public DsaSignature signature = null;

	public TestShout(User sender, Shout parent, String message,
			DateTime timestamp, DsaSignature signature, Hash hash, Location location) {
		super(sender, parent, message, location, timestamp);
		this.signature = signature;
		this.hash = hash;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public Hash getHash() {
		return hash;
	}

	@Override
	public DsaSignature getSignature() {
		return signature;
	}

}
