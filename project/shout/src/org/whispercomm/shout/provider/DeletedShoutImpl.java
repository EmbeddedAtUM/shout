
package org.whispercomm.shout.provider;

import org.joda.time.DateTime;
import org.whispercomm.shout.DeletedShout;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.User;
import org.whispercomm.shout.crypto.DsaSignature;

import android.util.Base64;

public class DeletedShoutImpl implements DeletedShout {

	private byte[] hashBytes = null;
	private int id;

	DeletedShoutImpl(String encodedHash, int id) {
		this.hashBytes = Base64.decode(encodedHash, Base64.DEFAULT);
		this.id = id;
	}

	@Override
	public User getSender() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DateTime getTimestamp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Shout getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getHash() {
		return hashBytes;
	}

	@Override
	public ShoutType getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DsaSignature getSignature() {
		throw new UnsupportedOperationException();
	}

	public int getId() {
		return id;
	}
}
