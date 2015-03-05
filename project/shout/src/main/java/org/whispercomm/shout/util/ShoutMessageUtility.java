
package org.whispercomm.shout.util;

import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.UnsignedShout;

public class ShoutMessageUtility {

	public static ShoutType getShoutType(UnsignedShout shout) {
		if (shout.getParent() == null) {
			return ShoutType.SHOUT;
		} else if (shout.getMessage() != null) {
			return ShoutType.COMMENT;
		} else if (shout.getParent().getParent() != null) {
			return ShoutType.RECOMMENT;
		} else {
			return ShoutType.RESHOUT;
		}
	}

	public static String getCountAsText(int count) {
		switch (count) {
			case 0:
				return "never";
			case 1:
				return "once";
			case 2:
				return "twice";
			default:
				return String.format("%d times", count);
		}
	}

}
