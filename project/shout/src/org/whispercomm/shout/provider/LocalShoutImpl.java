package org.whispercomm.shout.provider;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
import android.util.Base64;

public class LocalShoutImpl implements LocalShout {

	private int id;
	private LocalUser sender;
	private String message;
	private byte[] signatureBytes;
	private byte[] hashBytes;
	private DateTime sentTime;
	private DateTime receivedTime;
	
	private int parentId = -1;
	private LocalShout parent = null;
	private LocalShout myReshout = null;
	
	private int commentCount;
	private int reshoutCount;
	
	private Context context;
	
	public LocalShoutImpl(Context context, int id, LocalUser sender, String message, String encodedSig,
			String encodedHash, Long sentTime, Long receivedTime, int commentCount, int reshoutCount, int parentId) {
		this.context = context;
		this.id = id;
		this.sender = sender;
		this.message = message;
		this.signatureBytes = Base64.decode(encodedSig, Base64.DEFAULT);
		this.hashBytes = Base64.decode(encodedHash, Base64.DEFAULT);
		this.sentTime = new DateTime(sentTime);
		this.receivedTime = new DateTime(receivedTime);
		this.parentId = parentId;
		this.commentCount = commentCount;
		this.reshoutCount = reshoutCount;
	}

	@Override
	public byte[] getHash() {
		return this.hashBytes;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public DateTime getTimestamp() {
		return this.sentTime;
	}

	@Override
	public byte[] getSignature() {
		return this.signatureBytes;
	}

	@Override
	public ShoutType getType() {
		return ShoutMessageUtility.getShoutType(this);
	}

	@Override
	public int getCommentCount() {
		return this.commentCount;
	}

	@Override
	public int getReshoutCount() {
		return this.reshoutCount;
	}

	@Override
	public DateTime getReceivedTime() {
		return this.receivedTime;
	}

	@Override
	public LocalShout getReshout(Me me) {
		// TODO Make a reshout if it doesn't exist
		if (myReshout == null) {
			myReshout = ShoutProviderContract.getReshoutIfExists(context, this, me);
		}
		return myReshout;
	}

	@Override
	public int getDatabaseId() {
		return this.id;
	}

	@Override
	public LocalShout getParent() {
		if (parent != null || parentId == -1) {
			return parent;
		}
		else {
			parent = ShoutProviderContract.retrieveShoutById(context, parentId);
			return parent;
		}
	}

	@Override
	public LocalUser getSender() {
		return this.sender;
	}

}
