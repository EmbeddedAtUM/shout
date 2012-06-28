
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

	private byte[] packet;
	private int version;
	private int count;

	private ShoutPacket(byte[] packet, int version, int count) {
		this.packet = packet;
		this.version = version;
		this.packet = packet;
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
	 * Get the packet contents as bytes
	 * 
	 * @return A well-formed byte array of packet contents
	 */
	public byte[] getContents() {
		return packet;
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
				* SerializeUtility.MAX_SHOUT_SIZE;

		private int version;
		private int count;

		private List<Shout> shouts;

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
			ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
			int size = 0;
			buffer.put(buildHeader());
			size += HEADER_SIZE;
			byte[] body = buildBody();
			buffer.put(body);
			size += body.length;
			byte[] packet = Arrays.copyOfRange(buffer.array(), 0, size);
			return new ShoutPacket(packet, version, count);
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
			ByteBuffer buffer = ByteBuffer.allocate(SerializeUtility.MAX_SHOUT_SIZE * count);
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
