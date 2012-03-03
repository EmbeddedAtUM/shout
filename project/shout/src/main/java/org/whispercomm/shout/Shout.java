package org.whispercomm.shout;

import org.joda.time.DateTime;

public class Shout {
	DateTime date;
	String content;

	public Shout(String content) {
		this.content = content;
	}

	String getContent() {
		return content;
	}
}
