
package org.whispercomm.shout.content.descriptor;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.MimeType;

public class ContentDescriptor {

	static final byte TYPE = 0x00;

	private final Hash hash;

	private final Hash root;

	private final MimeType mimetype;

	public ContentDescriptor(Hash root, MimeType mimetype) {
		this.root = root;
		this.mimetype = mimetype;
		this.hash = computeHash();
	}

	private Hash computeHash() {
		byte type = ContentDescriptorSerializer.TYPE.getIdAsByte();
		byte[] rootHash = root.toByteArray();
		String mimetypeStr = mimetype.toString();
		int len = mimetypeStr.length();
		int contentLen = 1 + rootHash.length + 1 + len;

		SHA256Digest digest = new SHA256Digest();
		digest.update(type);
		digest.update((byte) (0xFF00 & contentLen >> 8));
		digest.update((byte) (0x00FF & contentLen));
		digest.update(TYPE);
		digest.update(rootHash, 0, rootHash.length);
		digest.update((byte) len);
		for (int i = 0; i < len; ++i) {
			digest.update((byte) mimetypeStr.charAt(i));
		}

		byte[] hash = new byte[Hash.LENGTH];
		digest.doFinal(hash, 0);
		return new Hash(hash);
	}

	public Hash getHash() {
		return hash;
	}

	public Hash getObjectRoot() {
		return root;
	}

	public MimeType getMimeType() {
		return mimetype;
	}

}
