
package org.whispercomm.shout;

import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.util.ShoutMessageUtility;

public abstract class AbstractShout implements Shout {

	private Hash hash;

	@Override
	public Hash getHash() {
		if (hash == null) {
			this.hash = SerializeUtility.generateHash(this);
		}
		return hash;
	}

	@Override
	public ShoutType getType() {
		return ShoutMessageUtility.getShoutType(this);
	}

}
