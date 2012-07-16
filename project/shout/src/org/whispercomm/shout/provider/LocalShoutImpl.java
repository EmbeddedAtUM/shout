
package org.whispercomm.shout.provider;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
import android.database.Cursor;
import android.util.Base64;

public class LocalShoutImpl implements LocalShout {

	private int id;
	private LocalUser sender;
	private String message;
	private byte[] signatureBytes;
	private byte[] hashBytes;
	private DateTime sentTime;
	private DateTime receivedTime;

	private byte[] parentHash = null;
	private LocalShout parent = null;

	private int commentCount;
	private int reshoutCount;

	private Context context;

	public LocalShoutImpl(Context context, int id, LocalUser sender, String message,
			String encodedSig, String encodedHash, Long sentTime, Long receivedTime,
			int commentCount, int reshoutCount, String encodedParentHash) {
		this.context = context;
		this.id = id;
		this.sender = sender;
		this.message = message;
		this.signatureBytes = Base64.decode(encodedSig, Base64.DEFAULT);
		this.hashBytes = Base64.decode(encodedHash, Base64.DEFAULT);
		this.sentTime = new DateTime(sentTime);
		this.receivedTime = new DateTime(receivedTime);
		this.commentCount = commentCount;
		this.reshoutCount = reshoutCount;
		if (encodedParentHash != null) {
			this.parentHash = Base64.decode(encodedParentHash, Base64.DEFAULT);
		}
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
	public int getDatabaseId() {
		return this.id;
	}

	@Override
	public LocalShout getParent() {
		if (parent != null || parentHash == null) {
			return parent;
		}
		else {
			parent = ShoutProviderContract.retrieveShoutByHash(context, parentHash);
			return parent;
		}
	}

	@Override
	public LocalUser getSender() {
		return this.sender;
	}

	@Override
	public List<LocalUser> getReshouters() {
		Cursor cursor = ShoutProviderContract.getCursorOverReshouts(context, parentHash);
		List<LocalUser> users = new ArrayList<LocalUser>(cursor.getCount());
		int authorIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.AUTHOR);
		while (cursor.moveToNext()) {
			String author = cursor.getString(authorIndex);
			LocalUser user = new LazyLocalUserImpl(context, author);
			users.add(user);
		}
		return users;
	}

	@Override
	public List<LocalShout> getComments() {
		Cursor cursor = ShoutProviderContract.getComments(context, hashBytes);
		List<LocalShout> comments = new ArrayList<LocalShout>(cursor.getCount());
		while (cursor.moveToNext()) {
			comments.add(ShoutProviderContract.retrieveShoutFromCursor(context, cursor));
		}
		cursor.close();
		return comments;
	}

}
