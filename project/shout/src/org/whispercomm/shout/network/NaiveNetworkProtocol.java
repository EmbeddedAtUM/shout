
package org.whispercomm.shout.network;

import java.util.Timer;
import java.util.TimerTask;

import org.whispercomm.manes.client.maclib.ManesInterface;
import org.whispercomm.manes.client.maclib.NotRegisteredException;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.BadShoutVersionException;
import org.whispercomm.shout.serialization.InvalidShoutSignatureException;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;
import org.whispercomm.shout.serialization.ShoutPacket;
import org.whispercomm.shout.serialization.ShoutPacket.PacketBuilder;
import org.whispercomm.shout.serialization.ShoutPacketException;

import android.content.Context;
import android.util.Log;

/**
 * Simple implementation of network logic for Shout.
 * <p>
 * Received shouts that are valid (self-signatures check and
 * parents/grandparents exist) are stored in the content provider. Those with
 * invalid signatures are dropped.
 * <p>
 * Each outgoing shout is broadcast 20 times over a 10 hour at 30 minute
 * intervals.
 * 
 * @author Yue Liu
 * @author David R. Bild
 */
public class NaiveNetworkProtocol implements NetworkProtocol {
	public static final String TAG = NaiveNetworkProtocol.class.getSimpleName();

	/**
	 * rebroadcast period in millisecond
	 */
	public static long PERIOD = 30 * 60 * 1000;

	/**
	 * total number of re-sends for each message
	 */
	public static int RESEND_NUM = 20;
	/**
	 * timer for scheduling periodic re-broadcast
	 */
	Timer sendScheduler;
	ManesInterface manes;
	Context context;

	public NaiveNetworkProtocol(ManesInterface manes, Context context) {
		this.manes = manes;
		this.context = context;
		this.sendScheduler = new Timer();
	}

	@Override
	public void initialize() {
		// Nothing to do here.
	}

	@Override
	public void cleanup() {
		sendScheduler.cancel();
	}

	@Override
	public void sendShout(Shout shout) throws ShoutChainTooLongException, NotRegisteredException {
		final byte[] shoutBytes;
		PacketBuilder builder = new ShoutPacket.PacketBuilder();
		builder.addShout(shout);
		ShoutPacket packet = builder.build();
		shoutBytes = packet.getPacketBytes();

		// Send once now, and then queue for further deliveries
		manes.send(shoutBytes);

		// schedule periodic re-broadcast up to RESEND_NUM times.
		long delay = PERIOD;
		for (int i = 1; i < RESEND_NUM; i++) {
			sendScheduler.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						manes.send(shoutBytes);
					} catch (NotRegisteredException e) {
						Log.e(TAG, "send() failed because not registered.", e);
					}
				}
			}, delay);
			delay += PERIOD;
		}
	}

	@Override
	public void receivePacket(byte[] data) {
		try {
			ShoutPacket packet = ShoutPacket.wrap(data);
			Shout shout = packet.decodeShout();
			Shout current = shout;
			while (current != null) {
				if (!SignatureUtility.verifySignature(SerializeUtility.serializeShoutData(current),
						current.getSignature(), current.getSender().getPublicKey())) {
					throw new InvalidShoutSignatureException();
				}
				current = current.getParent();
			}
			ShoutProviderContract.saveShout(context, shout);
		} catch (BadShoutVersionException e) {
			Log.v(TAG, e.getMessage());
		} catch (ShoutPacketException e) {
			Log.v(TAG, e.getMessage());
		} catch (InvalidShoutSignatureException e) {
			Log.v(TAG, e.getMessage());
		}
	}

}
