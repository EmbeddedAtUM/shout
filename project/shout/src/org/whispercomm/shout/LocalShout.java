
package org.whispercomm.shout;

import java.util.List;

import org.joda.time.DateTime;

public interface LocalShout extends Shout {

	public LocalShout getParent();

	public LocalUser getSender();

	public int getCommentCount();

	public int getReshoutCount();

	public int getReshouterCount();

	public DateTime getReceivedTime();

	public List<LocalUser> getReshouters();

	public List<LocalShout> getComments();

}
