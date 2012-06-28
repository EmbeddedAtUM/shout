
package org.whispercomm.shout;

import org.joda.time.DateTime;

public class SimpleShout extends AbstractShout implements Shout {

	private DateTime timestamp;
	private User sender;
	private String message;
	private Shout parent;
	private byte[] signature;

	public SimpleShout(DateTime timestamp, User sender, String message,
			Shout parent, byte[] signature) {
		this.timestamp = timestamp;
		this.sender = sender;
		this.message = message;
		this.parent = parent;
		this.signature = signature;
	}

	@Override
	public byte[] getSignature() {
		return this.signature;
	}

	@Override
	public User getSender() {
		return this.sender;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public DateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public Shout getParent() {
		return this.parent;
	}

}
