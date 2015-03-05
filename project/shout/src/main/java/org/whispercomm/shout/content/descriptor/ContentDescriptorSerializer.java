
package org.whispercomm.shout.content.descriptor;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.MimeType;
import org.whispercomm.shout.errors.InvalidFormatException;
import org.whispercomm.shout.network.ObjectType;
import org.whispercomm.shout.network.UnsupportedVersionException;

public final class ContentDescriptorSerializer {

	public static final ObjectType TYPE = ObjectType.ContentDescriptor;

	public static boolean serialize(ByteBuffer buffer, ContentDescriptor descriptor) {
		byte[] root = descriptor.getObjectRoot().toByteArray();
		String mimetype = descriptor.getMimeType().toString();

		if (buffer.remaining() < 1 + 2 + 1 + Hash.LENGTH + 1 + mimetype.length())
			return false;

		buffer.put(TYPE.getIdAsByte());
		buffer.putShort((short) (1 + Hash.LENGTH + 1 + mimetype.length()));
		buffer.put(ContentDescriptor.TYPE);
		buffer.put(root);
		buffer.put((byte) mimetype.length());
		for (int i = 0; i < mimetype.length(); ++i) {
			// This is safe, since all characters are ASCII-encodable.
			buffer.put((byte) mimetype.charAt(i));
		}
		return true;
	}

	public static byte[] serialize(ContentDescriptor descriptor) {
		ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 1 + Hash.LENGTH + 1
				+ descriptor.getMimeType().length());
		serialize(buffer, descriptor);
		return buffer.array();
	}

	public static ContentDescriptor deserialize(ByteBuffer buffer)
			throws UnsupportedVersionException, InvalidFormatException {
		try {
			int typeId = 0xFF & buffer.get();
			if (typeId != TYPE.getId())
				throw new InvalidFormatException(String.format(
						"Incorrect object type for content descriptor.  Got %d. Expected %d.",
						typeId,
						TYPE.getId()));

			int contentLength = buffer.getShort();

			byte type = buffer.get();
			switch (type) {
				case ContentDescriptor.TYPE:
					byte[] root = new byte[Hash.LENGTH];
					buffer.get(root);
					int len = 0xFF & buffer.get();

					if (contentLength != 1 + Hash.LENGTH + 1 + len)
						throw new InvalidFormatException(
								String.format(
										"Invalid content length for content descriptor. Got %d. Expected %d.",
										contentLength, 1 + Hash.LENGTH + 1 + len));

					byte[] mimetype = new byte[len];
					buffer.get(mimetype);
					try {
						return new ContentDescriptor(new Hash(root), new MimeType(mimetype));
					} catch (IllegalArgumentException e) {
						throw new InvalidFormatException("Invalid encoding in subfield.", e);
					}
				default:
					throw new UnsupportedVersionException(String.format(
							"Unrecognized content descriptor version: %02X", type));
			}
		} catch (BufferUnderflowException e) {
			throw new InvalidFormatException("Data too short.");
		}
	}

	public static ContentDescriptor deserialize(byte[] data, int offset)
			throws UnsupportedVersionException,
			InvalidFormatException {
		return deserialize(ByteBuffer.wrap(data, offset, data.length - offset));
	}

	public static ContentDescriptor deserialize(byte[] data) throws UnsupportedVersionException,
			InvalidFormatException {
		return deserialize(data, 0);
	}

	private ContentDescriptorSerializer() {
	}
}
