package org.whispercomm.shout.network.service;

import org.whispercomm.shout.network.service.ErrorCode;
import org.whispercomm.shout.network.service.ManesStatusCallback;

interface NetworkServiceBinder {
	ErrorCode send(in byte[] hash);
	
	void register(ManesStatusCallback callback);
	
	void unregister(ManesStatusCallback callback);	

}