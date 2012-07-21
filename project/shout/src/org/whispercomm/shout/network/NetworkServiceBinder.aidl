package org.whispercomm.shout.network;

import org.whispercomm.shout.network.ErrorCode;
import org.whispercomm.shout.network.ManesStatusCallback;

interface NetworkServiceBinder {
	ErrorCode send(in byte[] hash);
	
	void register(ManesStatusCallback callback);
	
	void unregister(ManesStatusCallback callback);	

}