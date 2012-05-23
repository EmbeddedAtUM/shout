
package org.whispercomm.shout.test.util;

import org.whispercomm.shout.Tag;

public class TestTag implements Tag {

	public String name = null;

	public TestTag() {

	}

	public TestTag(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
