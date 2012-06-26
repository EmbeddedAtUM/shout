package org.whispercomm.shout;

import org.joda.time.DateTime;

public class SimpleUnsignedShout extends AbstractShout implements UnsignedShout {

	private User sender;
	private String message;
	private Shout parent;
	private DateTime timestamp;

	public SimpleUnsignedShout(DateTime timestamp, User sender, String message,
			Shout parent) {
		this.timestamp = timestamp;
		this.sender = sender;
		this.message = message;
		this.parent= parent;
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
