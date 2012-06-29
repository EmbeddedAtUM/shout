
package org.whispercomm.shout.serialization;

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

	public static ShoutPacket wrap(byte[] contents) {
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
	 * Get the encoded packet header
	 * 
	 * @return The packet header as a byte array.
	 */
	public byte[] getHeaderBytes() {
		return header;
	}

	/**
	 * Get the encoded packet body
	 * 
	 * @return The packet body as a byte array.
	 */
	public byte[] getBodyBytes() {
		return body;
	}
	
	public Shout decodeShout() {
		// TODO
		return null;
	}

	/**
	 * Get the packet contents as bytes. This includes both the header and the
	 * body.
	 * 
	 * @return A well-formed byte array of packet contents
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
				* (SerializeUtility.MAX_SHOUT_SIZE + SerializeUtility.MAX_SIGNATURE_SIZE);

		private int version;
		private int count;

		private List<Shout> shouts;

		private static ShoutPacket wrap(byte[] contents) {
			ByteBuffer buffer = ByteBuffer.wrap(contents);
			byte[] header = new byte[HEADER_SIZE];
			buffer.get(header);
			byte[] body = new byte[contents.length - HEADER_SIZE];
			buffer.get(body);
			byte versionByte = header[0];
			int version = (((int) versionByte) & 0x000F);
			byte countByte = header[1];
			int count = (((int) countByte) & 0x000F);
			return new ShoutPacket(version, count, header, contents);
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
		 * @throws NoSuchVersionException If the version specified does not
		 *             exist.
		 */
		public PacketBuilder(int version) throws NoSuchVersionException {
			this.version = version;
			if (this.version != VERSION_0) {
				throw new NoSuchVersionException();
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
			/*
			 * TODO Have this place the header into a byte buffer passed as a
			 * parameter
			 */
			byte versionByte = (byte) (version & 0x000F);
			byte countByte = (byte) (version & 0x000F);
			return new byte[] {
					versionByte, countByte
			};
		}

		private byte[] buildBody() {
			/*
			 * TODO Have this place the body into a byte buffer passed as a
			 * parameter
			 */
			ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE - HEADER_SIZE);
			int size = 0;
			for (Shout shout : shouts) {
				byte[] data = SerializeUtility.serializeShoutData(shout);
				buffer.put(data);
				size += data.length;
				byte[] signature = shout.getSignature();
				byte length = (byte) (signature.length & 0x000F);
				buffer.put(length);
				size += 1;
				buffer.put(signature);
				size += signature.length;
			}
			return Arrays.copyOfRange(buffer.array(), 0, size);
		}

	}

}
