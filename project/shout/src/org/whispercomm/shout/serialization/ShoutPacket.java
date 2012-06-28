
package org.whispercomm.shout.serialization;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.util.Arrays;

public class ShoutPacket {

	private byte[] packet;
	private int version;
	private int count;

	private ShoutPacket(byte[] packet, int version, int count) {
		this.packet = packet;
		this.version = version;
		this.packet = packet;
	}

	public int getVersion() {
		return version;
	}

	public int getShoutCount() {
		return count;
	}

	public byte[] getContents() {
		return packet;
	}

	public static class PacketBuilder {

		public static final int VERSION_0 = 0;
		public static final int CURRENT = VERSION_0;
		private static final int HEADER_SIZE = 2;
		private static final int MAX_PACKET_SIZE = HEADER_SIZE + 3
				* SerializeUtility.MAX_SHOUT_SIZE;

		private int version;
		private int count;

		private List<Shout> shouts;

		public PacketBuilder() {
			this.version = CURRENT;
			initialize();
		}

		public PacketBuilder(int version) {
			this.version = version;
			initialize();
		}

		public PacketBuilder addShout(Shout shout) {
			while (shout != null) {
				if (count == 3) {
					return null;
				}
				shouts.add(shout);
				count++;
				shout = shout.getParent();
			}
			return this;
		}

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

		private void initialize() {
			this.count = 0;
			this.shouts = new ArrayList<Shout>(3);
		}

		private byte[] buildHeader() {
			byte versionByte = (byte) (version & 0x000F);
			byte countByte = (byte) (version & 0x000F);
			return new byte[] {
					versionByte, countByte
			};
		}

		private byte[] buildBody() {
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
