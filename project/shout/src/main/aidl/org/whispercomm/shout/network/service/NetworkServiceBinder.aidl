package org.whispercomm.shout.network.service;

import org.whispercomm.shout.Hash;

import org.whispercomm.shout.network.service.ErrorCode;
import org.whispercomm.shout.network.service.ManesStatusCallback;

interface NetworkServiceBinder {
	ErrorCode send(in Hash hash);
	
	void register(ManesStatusCallback callback);
	
	void unregister(ManesStatusCallback callback);	

}