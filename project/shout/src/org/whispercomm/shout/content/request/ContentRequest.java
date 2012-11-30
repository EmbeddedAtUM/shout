
package org.whispercomm.shout.content.request;

import org.whispercomm.shout.Hash;

public class ContentRequest {
	static final byte TYPE = 0x00;

	private final Hash objectHash;

	public ContentRequest(Hash objectHash) {
		this.objectHash = objectHash;
	}

	public Hash getObjectHash() {
		return objectHash;
	}

}
