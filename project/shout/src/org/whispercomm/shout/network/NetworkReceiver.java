package org.whispercomm.shout.network;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.whispercomm.manes.client.maclib.ManesInterface;

import android.util.Log;

/**
 * Listens for incoming Shout packets, passing them to the
 * {@link NetworkProtocol} as received.
 * 
 * @author Yue Liu
 * @author David R. Bild
 * 
 */
public class NetworkReceiver {
	private static final String TAG = NetworkReceiver.class.getSimpleName();

	/**
	 * Time to block while checking for a new incoming packet in milliseconds.
	 */
	private static final int BLOCK_INTERVAL_MS = 10;

	private final ManesInterface manes;
	private final NetworkProtocol networkProtocol;

	private Thread thread;
	private volatile boolean running;

	public NetworkReceiver(ManesInterface manes, NetworkProtocol networkProtocol) {
		this.manes = manes;
		this.networkProtocol = networkProtocol;
		this.running = false;
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
		try {
			// TODO: manes.receive returns null immediately if the manes service
			// is not bound, so this will busy idle until it is bound.
			// manes.receive should probably wait the full BLOCK_INTERVAL_MS
			// before returning, even if it is not initially bound.
			byte[] data = manes.receive(BLOCK_INTERVAL_MS);
			if (data != null) {
				networkProtocol.receiveShout(new NetworkShout(data));
			}
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (SignatureException e) {
			Log.e(TAG, e.getMessage());
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage());
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
		} catch (AuthenticityFailureException e) {
			Log.e(TAG, e.getMessage());
		}
	}
}
