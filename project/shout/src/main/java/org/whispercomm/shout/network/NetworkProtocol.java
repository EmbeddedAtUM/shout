package org.whispercomm.shout.network;

/**
 * Interface abstracts the functionalities of network protocols supporting
 * "shout".
 * 
 * @author Yue Liu
 * 
 */
public interface NetworkProtocol {

	/**
	 * Any specific clear-up of the particular subclass
	 */
	public void clearUp();

	/**
	 * Handle incoming shout from from application (e.g., UI)
	 */
	public void handleOutgoingAppShout(long shoutId);

	/**
	 * Handle incoming shout from the network
	 */
	public void handleIncomingNetworkShout(NetworkShout shout);

}
