package org.whispercomm.shout.provider;

import org.joda.time.DateTime;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutMessageUtility;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.User;

import android.content.Context;
import android.util.Base64;

/**
 * An implementation of {@link Shout} backed by the Shout ContentProvider
 * database.
 * 
 * @author David Adrian
 * @author David R. Bild
 * 
 */
public class ProviderShout implements Shout {

	private byte[] signature;
	private byte[] hash;
	private String message;
	private DateTime timestamp;

	private User sender;

	private Shout parent;

	/**
	 * Constructs a new ProviderShout from the specified parameters.
	 * 
	 * @param senderId
	 * @param parentId
	 * @param message
	 * @param time
	 * @param hash
	 * @param signature
	 * @param context
	 */
	// TODO If such a Shout is really backed by an entry in the ContentProvider,
	// this constructor should not be publically visible. Maybe package-private?
	public ProviderShout(int senderId, int parentId, String message, long time,
			String hash, String signature, Context context) {
		this.message = message;
		this.timestamp = new DateTime(time); // TODO Time Zone
		this.hash = Base64.decode(hash, Base64.DEFAULT);
		this.signature = Base64.decode(signature, Base64.DEFAULT);
		this.sender = ShoutProviderContract.retrieveUserById(context, senderId);
		if (parentId > 0) {
			this.parent = ShoutProviderContract.retrieveShoutById(context,
					parentId);
			// TODO Lazy load parent
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

	@Override
	public ShoutType getType() {
		return ShoutMessageUtility.getShoutType(this);
	}

}
