package org.whispercomm.shout;

import org.joda.time.DateTime;

public interface LocalShout extends Shout {
	
	public LocalShout getParent();
	
	public LocalUser getSender();
	
	public int getCommentCount();
	
	public int getReshoutCount();
	
	public DateTime getReceivedTime();
	
	public LocalShout getReshout();

	public int getDatabaseId();
	
}
