package org.whispercomm.shout.test.util;

import org.joda.time.DateTime;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;

public class TestShout implements Shout {
	
	public byte[] hash = null;
	public User sender = null;
	public String message = null;
	public DateTime timestamp = null;
	public Shout parent = null;
	public byte[] signature = null;
	
	
	public TestShout() {
		
	}

	public TestShout(User sender, Shout parent, String message,
			DateTime timestamp, byte[] signature, byte[] hash) {
		this.sender = sender;
		this.parent = parent;
		this.message = message;
		this.timestamp = timestamp;
		this.signature = signature;
		this.hash = hash;
	}

	@Override
	public byte[] getHash() {
		return hash;
	}

	@Override
	public User getSender() {
		return sender;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public DateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public Shout getParent() {
		return parent;
	}

	@Override
	public byte[] getSignature() {
		return signature;
	}

}
