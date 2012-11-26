
package org.whispercomm.shout.network;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.whispercomm.manes.client.maclib.ManesFrameTooLargeException;
import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;

import android.util.Log;

/**
 * Protocol handler for Shout packets. This class is responsible for passing the
 * serialized objects contained in packet to the appropriate object protocol
 * handlers and provides methods for constructing Shout packets to contain
 * serialized objects.
 * 
 * @author David R. Bild
 */
public class PacketProtocol {
	private static final String TAG = PacketProtocol.class.getSimpleName();

	public static int MAX_PACKET_LEN = ManesInterface.MANES_MTU;

	public static int OBJECT_HEADER_LEN = 3;

	private final ManesInterface manes;

	/*
	 * ------------------------- Public Methods -------------------------------
	 */
	@SuppressWarnings("unchecked")
	public PacketProtocol(ManesInterface manes) {
		this.manes = manes;
		protocols = new List[ObjectType.MAX_TYPE_ID + 1];
		for (int i = 0; i <= ObjectType.MAX_TYPE_ID; ++i) {
			protocols[i] = new CopyOnWriteArrayList<ObjectProtocol>();
		}
	}

	/**
	 * Registers a new object protocol handler to receive serialized incoming
	 * objects.
	 * 
	 * @param type the object type for the protocol
	 * @param protocol the protocol handler to register
	 */
	public void register(ObjectType type, ObjectProtocol protocol) {
		protocols[type.getId()].add(protocol);
	}

	/**
	 * Unregisters an object protocol handler.
	 * 
	 * @param type the object type for the protocol
	 * @param protocol the protocol handler to unregister
	 */
	public void unregister(ObjectType type, ObjectProtocol protocol) {
		protocols[type.getId()].remove(protocol);
	}

	public void receive(ByteBuffer buffer) {
		try {
			byte flags = buffer.get(buffer.position());
			switch (VERSION(flags)) {
				case 0:
					receiveVersion0(buffer);
					break;
				default:
					// Drop packet with unsupported version
					return;
			}
		} catch (BufferUnderflowException e) {
			// Drop bad packets
			return;
		}
	}

	public void send(ByteBuffer buffer) throws ManesFrameTooLargeException,
			ManesNotRegisteredException {
		buffer = buffer.duplicate();
		buffer.flip();

		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);

		manes.send(data);
	}

	/**
	 * Creates a new {@link ByteBuffer} for building packet and sets the
	 * {@code PacketProtocol} header. Objects can be added with the
	 * {@link #reserveObjectHeader(ByteBuffer)} and
	 * {@link #setObjectHeader(ByteBuffer, ObjectType, short)} methods.
	 * 
	 * @return the created buffer
	 */
	public static ByteBuffer createPacket() {
		ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_LEN);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.put((byte) (VERSION_MASK & VERSION));
		return buffer;
	}

	/*
	 * ---------------------------- Implementation ----------------------------
	 */
	private static final int VERSION_MASK = 0x0F;

	/**
	 * Extracts the version from the flags byte
	 * 
	 * @param flags the flag byte
	 * @return the version number
	 */
	private static final int VERSION(byte flags) {
		return flags & VERSION_MASK;
	}

	/**
	 * Version to use when creating a packet
	 */
	private static final int VERSION = 0;

	private List<ObjectProtocol>[] protocols;

	private void receiveVersion0(ByteBuffer buffer) {
		buffer.order(ByteOrder.BIG_ENDIAN); // Network-byte order

		// Header fields
		@SuppressWarnings("unused")
		byte flags = buffer.get();

		while (buffer.hasRemaining()) {
			// Peek at object header
			int id = 0xFF & buffer.get(buffer.position());
			int contentLength = buffer.getShort(buffer.position() + 1);
			int length = OBJECT_HEADER_LEN + contentLength;

			ByteBuffer objBuffer;
			try {
				// Create sliced view for receiver
				objBuffer = buffer.slice();
				// objBuffer.limit(length);

				// Advance the main buffer by the sliced view
				buffer.position(buffer.position() + length);
			} catch (IllegalArgumentException e) {
				// Drop rest of packet. We have no way to find the next object,
				// if the specified length is invalid.
				Log.v(TAG, "Dropping packet with invalid object length field: " + contentLength, e);
				return;
			}

			if (id > protocols.length) {
				// Drop object if no protocol for type
				Log.v(TAG, "Dropping packet with unrecognized object type: " + id);
				continue;
			}

			for (ObjectProtocol receiver : protocols[id]) {
				try {
					receiver.receive(objBuffer);
				} catch (RuntimeException e) {
					Log.w(TAG, "Ignoring exception thrown by " + receiver, e);
					// Ignore bad protocol
					continue;
				}
			}
		}
	}
}
