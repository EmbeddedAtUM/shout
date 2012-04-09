package org.whispercomm.shout;

import org.joda.time.DateTime;

public class SimpleShout extends AbstractShout {

	User sender;
	String content;
	Shout reshoutedShout;
	DateTime timestamp;
	byte[] signature;

	public SimpleShout(DateTime timestamp, User sender, String content,
			Shout shoutOri, byte[] signature) {
		this.timestamp = timestamp;
		this.sender = sender;
		this.content = content;
		this.reshoutedShout = shoutOri;
		this.signature = signature;
	}

	@Override
	public User getSender() {
		return this.sender;
	}

	@Override
	public String getContent() {
		return this.content;
	}

	@Override
	public DateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public Shout getOriginalShout() {
		return this.reshoutedShout;
	}

	@Override
	public byte[] getSignature() {
		return this.signature;
	}

}