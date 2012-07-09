package org.whispercomm.shout;

import java.util.List;

import org.joda.time.DateTime;

public interface LocalShout extends Shout {

	public LocalShout getParent();

	public LocalUser getSender();

	public int getCommentCount();

	public int getReshoutCount();

	public DateTime getReceivedTime();

	@Deprecated
	public int getDatabaseId();

	public List<LocalUser> getReshouters();

	public List<LocalShout> getComments();

}
