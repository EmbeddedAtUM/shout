
package org.whispercomm.shout.network;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.shout.network.shout.NetworkProtocol;

/**
 * Listens for incoming Shout packets, passing them to the
 * {@link NetworkProtocol} as received.
 * 
 * @author Yue Liu
 * @author David R. Bild
 */
public class NetworkReceiver {
	@SuppressWarnings("unused")
	private static final String TAG = NetworkReceiver.class.getSimpleName();

	/**
	 * Time to block while checking for a new incoming packet in milliseconds.
	 */
	private static final int BLOCK_INTERVAL_MS = 1 * 60 * 1000;

	private final ManesInterface manes;
	private final List<PacketProtocol> protocols;

	private Thread thread;
	private volatile boolean running;

	public NetworkReceiver(ManesInterface manes) {
		this.manes = manes;
		this.protocols = new CopyOnWriteArrayList<PacketProtocol>();
		this.running = false;
	}

	public void register(PacketProtocol receiver) {
		protocols.add(receiver);
	}

	public void unregister(PacketProtocol receiver) {
		protocols.remove(receiver);
	}

	public void initialize() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				NetworkReceiver.this.run();
			}
		});

		running = true;
		thread.start();
	}

	public void cleanup() {
		running = false;
		thread.interrupt();
	}

	private void run() {
		while (running) {
			try {
				receivePacket();
			} catch (Exception e) {
				// Ignore. Running will be false and the loop will exit if we
				// should be quitting.
				try {
					// Sleep a hundred milliseconds to avoid busy looping.
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// Ignore
				}
			}
		}
	}

	private void receivePacket() {
		byte[] data = manes.receive(BLOCK_INTERVAL_MS);
		if (data != null) {
			for (PacketProtocol receiver : protocols) {
				try {
					receiver.receive(ByteBuffer.wrap(data));
				} catch (RuntimeException e) {
					// Ignore bad receiver;
				}
			}
		}
	}
}
