
package org.whispercomm.shout.serialization;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.util.Arrays;

/**
 * Java object representation of a packet that conforms to the Shout packet
 * standard and is ready to be sent over the network.
 * 
 * @author David Adrian
 */
public class ShoutPacket {

	private byte[] header;
	private byte[] body;
	private int version;
	private int count;

	private ShoutPacket(int version, int count, byte[] header, byte[] body) {
		this.version = version;
		this.count = count;
		this.header = header;
		this.body = body;
	}

	public static ShoutPacket wrap(byte[] contents) throws ShoutPacketException {
		return PacketBuilder.wrap(contents);
	}

	/**
	 * Get the encoding version on this packet.
	 * 
	 * @return The encoding version used when building this packet.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Get how many Shouts were put into this packet.
	 */
	public int getShoutCount() {
		return count;
	}

	/**
	 * Get the encoded packet header. Do not send this over the network.
	 * Instead, use {@link #getPacketBytes()}.
	 * 
	 * @return The packet header as a byte array.
	 */
	public byte[] getHeaderBytes() {
		return header;
	}

	/**
	 * Get the encoded packet body. Do not send this over the network. Instead,
	 * use {@link #getPacketBytes()}.
	 * 
	 * @return The packet body as a byte array.
	 */
	public byte[] getBodyBytes() {
		return body;
	}

	/**
	 * Deserialize the Shout described by this ShoutPacket
	 * 
	 * @return The Shout stored in this packet.
	 * @throws BadShoutVersionException
	 * @throws ShoutPacketException
	 * @throws InvalidShoutSignatureException
	 */
	public Shout decodeShout() throws BadShoutVersionException, ShoutPacketException,
			InvalidShoutSignatureException {
		return SerializeUtility.deserializeSequenceOfShouts(count, body);
	}

	/**
	 * Get the packet contents as bytes. This includes both the header and the
	 * body. Use this to send a Shout over the network.
	 * 
	 * @return A well-formed byte array of packet contents, ready to be
	 *         transmitted via MANES.
	 */
	public byte[] getPacketBytes() {
		int size = header.length + body.length;
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(header);
		buffer.put(body);
		return buffer.array();
	}

	/**
	 * Use a PacketBuilder to build a ShoutPacket
	 * 
	 * @author David Adrian
	 */
	public static class PacketBuilder {

		public static final int VERSION_0 = 0;
		public static final int CURRENT = VERSION_0;
		private static final int HEADER_SIZE = 2;
		private static final int MAX_PACKET_SIZE = HEADER_SIZE + 3
				* SerializeUtility.SHOUT_SIGNED_SIZE_MAX;

		private static final int MASK = 0x00FF;

		private int version;
		private int count;

		private List<Shout> shouts;

		private static ShoutPacket wrap(byte[] contents) throws ShoutPacketException {
			try {
				ByteBuffer buffer = ByteBuffer.wrap(contents);
				byte[] header = new byte[HEADER_SIZE];
				buffer.get(header);
				byte[] body = new byte[contents.length - HEADER_SIZE];
				buffer.get(body);
				byte versionByte = header[0];
				int version = (((int) versionByte) & MASK);
				byte countByte = header[1];
				int count = (((int) countByte) & MASK);
				return new ShoutPacket(version, count, header, body);
			} catch (BufferUnderflowException e) {
				throw new ShoutPacketException();
			}
		}

		/**
		 * Construct a PacketBuilder for the current Shout version
		 */
		public PacketBuilder() {
			this.version = CURRENT;
			initialize();
		}

		/**
		 * Construct a PacketBuilder with the specified version
		 * 
		 * @param version
		 * @throws BadShoutVersionException If the version specified does not
		 *             exist.
		 */
		public PacketBuilder(int version) throws BadShoutVersionException {
			this.version = version;
			if (this.version != VERSION_0) {
				throw new BadShoutVersionException();
			}
			initialize();
		}

		/**
		 * Add a Shout, including its parents, to the {@link ShoutPacket}
		 * 
		 * @param shout Shout to add to the packet
		 * @return {@code this} PacketBuilder for method chaining
		 * @throws ShoutChainTooLongException If more than 3 Shouts have been
		 *             added, including parents.
		 */
		public PacketBuilder addShout(Shout shout) throws ShoutChainTooLongException {
			while (shout != null) {
				if (count == 3) {
					throw new ShoutChainTooLongException();
				}
				shouts.add(shout);
				count++;
				shout = shout.getParent();
			}
			return this;
		}

		/**
		 * Build a ShoutPacket out of the inputs given to this PacketBuilder.
		 * 
		 * @return A ShoutPacket representation of the data in this builder.
		 */
		public ShoutPacket build() {
			byte[] header = buildHeader();
			byte[] body = buildBody();
			return new ShoutPacket(version, count, header, body);
		}

		/**
		 * Constructor helper method
		 */
		private void initialize() {
			this.count = 0;
			this.shouts = new ArrayList<Shout>(3);
		}

		/**
		 * Build the packet header
		 */
		private byte[] buildHeader() {
			byte versionByte = (byte) version;
			byte countByte = (byte) count;
			return new byte[] {
					versionByte, countByte
			};
		}

		/**
		 * Build the packet body
		 */
		private byte[] buildBody() {
			ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE - HEADER_SIZE);
			int size = 0;
			for (Shout shout : shouts) {
				byte[] data = SerializeUtility.serializeShoutData(shout);
				buffer.put(data);
				size += data.length;
				byte[] signature = shout.getSignature();
				byte length = (byte) signature.length;
				buffer.put(length);
				size += 1;
				buffer.put(signature);
				size += signature.length;
			}
			return Arrays.copyOfRange(buffer.array(), 0, size);
		}

	}

}
