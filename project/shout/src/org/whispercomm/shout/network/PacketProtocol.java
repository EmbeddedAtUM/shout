
package org.whispercomm.shout.network;

import java.nio.BufferOverflowException;
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

	/**
	 * Reserves space in the buffer for an object header. After the object
	 * contents have been written and the length is known, the object header can
	 * be set with {@link #setObjectHeader(ByteBuffer, ObjectType, short)}.
	 * <p>
	 * N.B., this method sets the buffer's mark to record the position of the
	 * object header for the
	 * {@link #setObjectHeader(ByteBuffer, ObjectType, short)} method. Do not
	 * change the mark.
	 * 
	 * @param buffer the buffer in which to reserve the header space
	 * @return the buffer
	 * @throws BufferOverflowException if the buffer does not have room for the
	 *             header
	 */
	public static ByteBuffer reserveObjectHeader(ByteBuffer buffer) throws BufferOverflowException {
		// Set mark at start of header
		buffer.mark();

		buffer.position(buffer.position() + 3);
		return buffer;
	}

	/**
	 * Sets the header previously reserved by
	 * {@link #reserveObjectHeader(ByteBuffer)}.
	 * <p>
	 * N.B., this method relies on the mark set by the
	 * {@link #reserveObjectHeader(ByteBuffer)}. Do not change the mark.
	 * 
	 * @param buffer the buffer in which to set the ehader
	 * @param type the type of the object
	 * @param len the length of the object
	 * @return the buffer
	 * @throws BufferOverflowException if the buffer does not have room for the
	 *             claimed length
	 */
	public static ByteBuffer setObjectHeader(ByteBuffer buffer, ObjectType type, short len)
			throws BufferOverflowException {
		// Reset position to marked start of header
		buffer.reset();

		buffer.put((byte) type.getId());
		buffer.putShort(len);

		// Fast-forward past object contents
		buffer.position(buffer.position() + len);
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
			int id = 0xFF & buffer.get();
			int length = buffer.getShort();

			ByteBuffer objBuffer;
			try {
				// Create sliced view for receiver
				objBuffer = buffer.slice();
				objBuffer.limit(length);

				// Advance the main buffer by the sliced view
				buffer.position(buffer.position() + length);
			} catch (IllegalArgumentException e) {
				// Drop rest of packet. We have no way to find the next object,
				// if the specified length is invalid.
				Log.v(TAG, "Dropping packet with invalid object length field: " + length, e);
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
