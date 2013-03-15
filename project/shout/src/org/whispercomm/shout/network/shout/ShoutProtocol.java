
package org.whispercomm.shout.network.shout;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.whispercomm.manes.client.maclib.ManesFrameTooLargeException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutType;
import org.whispercomm.shout.network.ObjectProtocol;
import org.whispercomm.shout.network.ObjectType;
import org.whispercomm.shout.network.PacketProtocol;
import org.whispercomm.shout.network.UnsupportedVersionException;
import org.whispercomm.shout.serialization.SerializeUtility;
import org.whispercomm.shout.serialization.SerializeUtility.BuildableShout;
import org.whispercomm.shout.serialization.ShoutPacketException;

import android.util.Log;

/**
 * The protocol for shout objects. This class is responsible for deserializing
 * incoming shouts and serializing outing shouts. Deserialized shouts are passed
 * to registered {@link NetworkProtocol} instances for further handling.
 * 
 * @author David R. Bild
 */
public class ShoutProtocol implements ObjectProtocol {
	private static final String TAG = ShoutProtocol.class.getSimpleName();

	private final List<NetworkProtocol> protocols;

	private final ShoutChainReconstructor reconstructor;

	private final PacketProtocol packetProtocol;

	public ShoutProtocol(PacketProtocol packetProtocol) {
		this.packetProtocol = packetProtocol;
		this.protocols = new CopyOnWriteArrayList<NetworkProtocol>();
		this.reconstructor = new ShoutChainReconstructor();
	}

	/**
	 * Registers a new network protocol to receive incoming shouts.
	 * 
	 * @param protocol the protocol to receive incoming shouts.
	 */
	public void register(NetworkProtocol protocol) {
		protocols.add(protocol);
	}

	/**
	 * Unregisters a network protocol.
	 * 
	 * @param protocol the protocol to unregister
	 */
	public void unregister(NetworkProtocol protocol) {
		protocols.remove(protocol);
	}

	@Override
	public void receive(ObjectType type, ByteBuffer data) {
		Shout shout = null;
		try {
			shout = reconstructor.processShout(SerializeUtility.deserializeShout(data));
		} catch (UnsupportedVersionException e) {
			Log.v(TAG, "Dropping shout with invalid version", e);
			return;
		} catch (ShoutPacketException e) {
			Log.v(TAG, "Dropping shout with invalid packet", e);
			return;
		} catch (InvalidShoutSignatureException e) {
			Log.v(TAG, "Dropping shout with invalid signature", e);
			return;
		} catch (ShoutChainTooLongException e) {
			Log.v(TAG, "Dropping shout with too many ancestors", e);
			return;
		}

		if (shout != null) {
			deliverShout(shout);
		}
	}

	/**
	 * Broadcast a shout (and its ancestors) over the network.
	 * 
	 * @param shout the shout to send
	 * @throws ManesFrameTooLargeException if the shout and ancestors do not fit
	 *             in a Manes frame.
	 * @throws ManesNotRegisteredException if the client is not registered with
	 *             Manes.
	 */
	public void send(Shout shout) throws ManesFrameTooLargeException, ManesNotRegisteredException {
		ByteBuffer buffer = PacketProtocol.createPacket();
		while (shout != null) {
			SerializeUtility.serializeShout(buffer, shout);
			shout = shout.getParent();
		}
		packetProtocol.send(buffer);
	}

	private void deliverShout(Shout shout) {
		for (NetworkProtocol protocol : protocols) {
			try {
				protocol.receive(shout);
			} catch (RuntimeException e) {
				Log.w(TAG, "Error in Shout object protocol: " + protocol, e);
				continue;
			}
		}
	}

	/**
	 * Reconstructs a shout hierarchy from incoming shouts. This version assumes
	 * that related shouts are delivered consecutively in
	 * root-parent-grandparent order. If the expected ancestor is not received
	 * next, the descendants are dropped and the unexpected shout is processed
	 * as a new root.
	 * 
	 * @author David R. Bild
	 */
	private static class ShoutChainReconstructor {

		private enum State {
			Empty, NeedParent, NeedGrandparent
		}

		private State state;
		private BuildableShout root;

		public ShoutChainReconstructor() {
			this.state = State.Empty;
			this.root = null;
		}

		/**
		 * @param shout
		 * @return the root of the shout chain, if fully reconstructed or
		 *         {@code null} if waiting on ancestors are needed
		 * @throws ShoutChainTooLongException if the grandparent-level shout
		 *             requires a parent
		 */
		public Shout processShout(BuildableShout shout) throws ShoutChainTooLongException {
			switch (state) {
				case Empty:
					return processEmpty(shout);
				case NeedParent:
					return processNeedParent(shout);
				case NeedGrandparent:
					return processNeedGrandparent(shout);
				default:
					throw new IllegalStateException();
			}
		}

		private BuildableShout processEmpty(BuildableShout shout) {
			if (shout.parentHash != null) {
				root = shout;
				state = State.NeedParent;
				return null;
			} else {
				return shout;
			}
		}

		private BuildableShout processNeedParent(BuildableShout shout) {
			if (Arrays.equals(root.parentHash, shout.hash)) {
				// This is parent we need.
				root.parent = shout;
				if (shout.parentHash != null) {
					state = State.NeedGrandparent;
					return null;
				} else {
					state = State.Empty;
					return root;
				}
			} else {
				// This is not the parent we need; drop previous shout and
				// process incoming as new child
				state = State.Empty;
				return processEmpty(shout);
			}
		}

		private BuildableShout processNeedGrandparent(BuildableShout shout)
				throws ShoutChainTooLongException {
			if (Arrays.equals(root.parent.parentHash, shout.hash)) {
				// This is the grandparent we need.
				root.parent.parent = shout;
				if (shout.parentHash != null) {
					state = State.Empty;
					throw new ShoutChainTooLongException();
				} else {
					state = State.Empty;
					pruneInvalidChain();
					return root;
				}
			} else {
				// This is not the grandparent we need; drop previous shout and
				// process incoming as new child
				state = State.Empty;
				return processEmpty(shout);
			}
		}

		/**
		 * Removes any invalid shouts from a length-3 chain of shouts.
		 * <p>
		 * The only valid length-3 chain is
		 * <ul>
		 * <li>reshout -> comment -> shout</li>
		 * </ul>
		 * The invalid chains are
		 * <ul>
		 * <li>reshout -> reshout -> shout</li>
		 * <li>comment -> reshout -> shout</li>
		 * <li>comment -> comment -> shout</li>
		 * </ul>
		 * This method prunes the invalid leaf from these chains, leaving a
		 * valid length-2 chain
		 * <ul>
		 * <li>reshout -> shout</li>
		 * <li>comment -> shout</li>
		 * </ul>
		 */
		private void pruneInvalidChain() {
			/* A reshout can only be root */
			if (root.parent.getType().equals(ShoutType.RESHOUT)) {
				Log.i(TAG, "Dropping invalid reshout of a reshout.");
				root = root.parent;
				return;
			}

			/* A comment cannot be root of a length-3 chain */
			if (root.getType().equals(ShoutType.COMMENT)) {
				Log.i(TAG, "Dropping invalid comment on a comment or reshout.");
				root = root.parent;
				return;
			}
		}

	}

}
