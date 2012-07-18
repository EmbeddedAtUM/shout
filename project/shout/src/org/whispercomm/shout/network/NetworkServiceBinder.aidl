package org.whispercomm.shout.network;

import org.whispercomm.shout.network.ErrorCode;

interface NetworkServiceBinder {

	ErrorCode initialized();
	
	ErrorCode send(in byte[] hash);	

}