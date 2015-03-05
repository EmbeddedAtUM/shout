
package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.whispercomm.shout.util.ShoutMessageUtility;

public class SimpleUnsignedShout implements UnsignedShout {

	private User sender;
	private Location location;
	private String message;
	private Shout parent;
	private DateTime timestamp;

	public SimpleUnsignedShout(DateTime timestamp, User sender, String message, Location location,
			Shout parent) {
		this.timestamp = timestamp;
		this.sender = sender;
		this.message = message;
		this.location = location;
		this.parent = parent;
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
	public Location getLocation() {
		return this.location;
	}

	@Override
	public DateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public Shout getParent() {
		return this.parent;
	}

	@Override
	public ShoutType getType() {
		return ShoutMessageUtility.getShoutType(this);
	}

}
