
package org.whispercomm.shout.content;

import org.whispercomm.shout.MimeType;

public class Content {

	private final byte[] mData;
	private final MimeType mMimetype;

	public Content(byte[] data, MimeType mimetype) {
		this.mData = data;
		this.mMimetype = mimetype;
	}

	public byte[] getData() {
		return mData;
	}

	public MimeType getMimeType() {
		return mMimetype;
	}

}
