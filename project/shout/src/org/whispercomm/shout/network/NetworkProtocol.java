
package org.whispercomm.shout.network;

import org.whispercomm.manes.client.maclib.NotRegisteredException;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;

/**
 * Interface for accessing the network logic of Shout. The network logic has two
 * primary
 * 
 * @author Yue Liu
 * @author David R. Bild
 */
public interface NetworkProtocol {

	/**
	 * Called when the NetworkProtocol should begin operation.
	 */
	public void initialize();

	/**
	 * Called when the NetworkProtocol should stop operation. The object should
	 * be safe for garbage collection (e.g., all acquired resources released)
	 * after this method returns.
	 */
	public void cleanup();

	/**
	 * Send a shout from the local device to the one-hop neighbors.
	 */
	public void sendShout(Shout shout) throws ShoutChainTooLongException,
			NotRegisteredException;

	/**
	 * Handle a Shout received from the network interface.
	 */
	public void receivePacket(byte[] data);

}
