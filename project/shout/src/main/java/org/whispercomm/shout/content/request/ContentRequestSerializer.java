
package org.whispercomm.shout.content.request;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.errors.InvalidFormatException;
import org.whispercomm.shout.network.ObjectType;
import org.whispercomm.shout.network.UnsupportedVersionException;

public class ContentRequestSerializer {

	public static final ObjectType TYPE = ObjectType.ContentRequest;

	public static boolean serialize(ByteBuffer buffer, ContentRequest request) {
		byte[] hash = request.getObjectHash().toByteArray();

		if (buffer.remaining() < 1 + 2 + 32)
			return false;

		buffer.put(TYPE.getIdAsByte());
		buffer.put((byte) 0);
		buffer.put((byte) (1 + Hash.LENGTH));
		buffer.put(ContentRequest.TYPE);
		buffer.put(hash);
		return true;
	}

	public static ContentRequest deserialize(ByteBuffer buffer)
			throws UnsupportedVersionException, InvalidFormatException {
		try {
			int typeId = 0xFF & buffer.get();
			if (typeId != TYPE.getId())
				throw new InvalidFormatException(String.format(
						"Incorrect object type for content descriptor.  Got %d. Expected %d.",
						typeId,
						TYPE.getId()));

			int contentLength = buffer.getShort();
			if (contentLength != 1 + Hash.LENGTH)
				throw new InvalidFormatException(
						String.format(
								"Invalid content length for content request. Got %d. Expected %d.",
								contentLength, 1 + Hash.LENGTH));

			byte type = buffer.get();
			switch (type) {
				case ContentRequest.TYPE:
					byte[] hash = new byte[Hash.LENGTH];
					buffer.get(hash);
					return new ContentRequest(new Hash(hash));
				default:
					throw new UnsupportedVersionException(String.format(
							"Unrecognized content request version: %02X", type));
			}
		} catch (BufferUnderflowException e) {
			throw new InvalidFormatException("Data too short.");
		}
	}

	private ContentRequestSerializer() {
	}

}
