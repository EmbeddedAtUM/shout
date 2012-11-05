
package org.whispercomm.shout.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.LocalUser;
import org.whispercomm.shout.Location;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.content.Context;
import android.database.Cursor;
import android.util.Base64;

public class LocalShoutImpl implements LocalShout {

	private int version;
	private LocalUser sender;
	private String message;
	private Location location;
	private DsaSignature signature;
	private byte[] hashBytes;
	private DateTime sentTime;
	private DateTime receivedTime;

	private byte[] parentHash = null;
	private LocalShout parent = null;

	private int commentCount;
	private int reshoutCount;

	private Context context;

	public LocalShoutImpl(Context context, int version, LocalUser sender, String message,
			Location location,
			String encodedSig, String encodedHash, Long sentTime, Long receivedTime,
			int commentCount, int reshoutCount, String encodedParentHash) {
		this.version = version;
		this.context = context;
		this.sender = sender;
		this.message = message;
		this.location = location;
		this.signature = DsaSignature.decode(Base64.decode(encodedSig, Base64.DEFAULT));
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
	public int getVersion() {
		return this.version;
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
	public Location getLocation() {
		return this.location;
	}

	@Override
	public DateTime getTimestamp() {
		return this.sentTime;
	}

	@Override
	public DsaSignature getSignature() {
		return this.signature;
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
		// TODO Collapse users with the same public key
		Cursor cursor = ShoutProviderContract.getCursorOverReshouts(context, hashBytes);
		List<LocalUser> users = new ArrayList<LocalUser>(cursor.getCount());
		Set<Integer> userIds = new HashSet<Integer>();
		int userIdIndex = cursor.getColumnIndex(ShoutProviderContract.Shouts.USER_PK);
		while (cursor.moveToNext()) {
			int userId = cursor.getInt(userIdIndex);
			userIds.add(userId);
		}
		for (int userId : userIds) {
			users.add(new LazyLocalUserImpl(context, userId));
		}
		cursor.close();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(hashBytes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalShoutImpl other = (LocalShoutImpl) obj;
		if (!Arrays.equals(hashBytes, other.hashBytes))
			return false;
		return true;
	}

}
