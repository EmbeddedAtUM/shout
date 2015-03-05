
package org.whispercomm.shout.test.util;

import org.joda.time.DateTime;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.User;

public class TestUnsignedShout implements UnsignedShout {

	public User sender = null;
	public String message = null;
	public Location location = null;
	public DateTime timestamp = null;
	public Shout parent = null;

	public TestUnsignedShout() {

	}

	public TestUnsignedShout(User sender, Shout parent, String message, Location location,
			DateTime timestamp) {
		this.sender = sender;
		this.parent = parent;
		this.message = message;
		this.location = location;
		this.timestamp = timestamp;
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
	public Location getLocation() {
		return location;
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
	public ShoutType getType() {
		return null;
	}

}
