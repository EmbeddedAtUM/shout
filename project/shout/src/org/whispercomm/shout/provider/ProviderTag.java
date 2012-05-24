
package org.whispercomm.shout.provider;

import org.whispercomm.shout.Tag;

public class ProviderTag implements Tag {

	private String name;

	public ProviderTag(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
