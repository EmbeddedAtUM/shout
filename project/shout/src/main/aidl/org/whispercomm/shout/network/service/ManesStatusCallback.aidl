package org.whispercomm.shout.network.service;

import org.whispercomm.shout.network.service.ErrorCode;

/**
 * Interface for the Shout network service to notify the {@code NetworkInterface}
 * with the Manes client installation and registration status.
 */
interface ManesStatusCallback {

    /**
      * Called when the Manes installation state is first available.
      * 
      * @param status {@code true} if Manes client is installed and 
      *                {@code false} if not.
      */
    void installed(boolean status);

    /**
      * Called when the Manes registration state is first available.
      * 
      * @param status {@code true} if Manes client is registered and 
      *                {@code false} if not.
      */
	void registered(boolean status);

}