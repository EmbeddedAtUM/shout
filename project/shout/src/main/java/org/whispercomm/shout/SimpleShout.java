
package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.whispercomm.shout.crypto.DsaSignature;

public class SimpleShout extends AbstractShout implements Shout {

	private int version;
	private DateTime timestamp;
	private User sender;
	private String message;
	private Location location;
	private Shout parent;
	private DsaSignature signature;

	public SimpleShout(int version, DateTime timestamp, User sender, String message,
			Location location,
			Shout parent, DsaSignature signature) {
		this.version = version;
		this.timestamp = timestamp;
		this.sender = sender;
		this.message = message;
		this.location = location;
		this.parent = parent;
		this.signature = signature;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public DsaSignature getSignature() {
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

}
