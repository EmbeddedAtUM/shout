
package org.whispercomm.shout.provider;

import org.joda.time.DateTime;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.User;

import android.content.Context;

public class ProviderShout implements Shout {

	private byte[] signature;
	private byte[] hash;
	private String message;
	private DateTime timestamp;

	private User sender;

	private Shout parent;

	public ProviderShout(int senderId, int parentId, String message, long time, byte[] hash,
			byte[] signature, Context context) {
		this.message = message;
		this.timestamp = new DateTime(time); // TODO Time Zone
		this.hash = hash;
		this.signature = signature;
		this.sender = ShoutProviderContract.retrieveUserById(context, senderId);
		if (parentId > 0) {
			this.parent = ShoutProviderContract.retrieveShoutById(context, parentId);
		} else {
			this.parent = null;
		}
	}

	@Override
	public byte[] getHash() {
		return this.hash;
	}

	@Override
	public User getSender() {
		return this.sender; // TODO Determine if we should wait to query the DB
							// until this is called
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
		return this.parent; // TODO Determine if we should wait to query the DB
							// until this is called
	}

	@Override
	public byte[] getSignature() {
		return this.signature;
	}

}
