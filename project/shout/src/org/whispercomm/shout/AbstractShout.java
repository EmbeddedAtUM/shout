
package org.whispercomm.shout;

import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.util.ShoutMessageUtility;

public abstract class AbstractShout implements Shout {

	private byte[] hashCode;

	@Override
	public byte[] getHash() {
		if (hashCode == null) {
			this.hashCode = SerializeUtility.generateHash(this);
		}
		return hashCode;
	}
	
	@Override
	public ShoutType getType() {
		return ShoutMessageUtility.getShoutType(this);
	}

}
