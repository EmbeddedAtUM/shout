
package org.whispercomm.shout.network.content;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.whispercomm.manes.client.maclib.ManesFrameTooLargeException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.content.descriptor.ContentDescriptor;
import org.whispercomm.shout.content.merkle.MerkleNode;
import org.whispercomm.shout.content.request.ContentRequest;
import org.whispercomm.shout.content.request.ContentRequestSerializer;
import org.whispercomm.shout.content.storage.ObjectStorage;
import org.whispercomm.shout.errors.NotFoundException;
import org.whispercomm.shout.network.PacketProtocol;
import org.whispercomm.shout.network.shout.NetworkProtocol;
import org.whispercomm.shout.network.shout.ShoutChainTooLongException;
import org.whispercomm.shout.util.AlarmExecutorService;

import android.util.Log;

public class SimpleContentRequestHandler implements ContentRequestHandler, NetworkProtocol {
	private static final String TAG = SimpleContentRequestHandler.class.getSimpleName();

	private AlarmExecutorService executor;
	private final PacketProtocol packetProtocol;
	private final ContentProtocol contentProtocol;
	private final ObjectStorage storage;
	private final ContentManager contentManager;

	private final ConcurrentMap<Hash, RequestTask> requests;

	private volatile boolean running;

	public SimpleContentRequestHandler(AlarmExecutorService executor,
			PacketProtocol packetProtocol,
			ContentProtocol contentProtocol, ObjectStorage storage, ContentManager contentManager) {
		this.executor = executor;
		this.packetProtocol = packetProtocol;
		this.contentProtocol = contentProtocol;
		this.storage = storage;
		this.contentManager = contentManager;
		this.requests = new ConcurrentHashMap<Hash, RequestTask>();
		this.running = false;
	}

	public void initialize() {
		if (!running)
			running = true;
	}

	public void cleanup() {
		if (running) {
			running = false;
			executor.shutdown();
		}
	}

	@Override
	public void request(Hash hash) {
		if (!running)
			return;

		OutgoingRequestTask task = new OutgoingRequestTask(hash);
		enqueueIfAbsent(task);
	}

	@Override
	public void receiveRequest(ContentRequest request) {
		if (!running)
			return;

		Hash hash = request.getObjectHash();
		Log.v(TAG, "Received content request for " + hash);
		if (storage.exists(hash)) {
			IncomingRequestTask task = new IncomingRequestTask(hash);
			enqueueIfAbsent(task);
		}
	}

	@Override
	public void receiveContentDescriptor(ContentDescriptor descriptor) {
		if (!running)
			return;
		receive(descriptor.getHash());
	}

	@Override
	public void receiveMerkleNode(MerkleNode node) {
		if (!running)
			return;
		receive(node.getHash());
	}

	public void enqueueIfAbsent(RequestTask task) {
		if (null == requests.putIfAbsent(task.getHash(), task))
			executor.execute(task);
	}

	public void receive(Hash hash) {
		if (!running)
			return;
		RequestTask task = requests.remove(hash);
		if (task != null)
			task.cancel();
	}

	private void sendContent(IncomingRequestTask task) {
		if (!running)
			return;
		try {
			Log.v(TAG, "Sending content for " + task.getHash());
			contentProtocol.send(task.getHash());
			requests.remove(task.getHash(), task);
		} catch (ManesFrameTooLargeException e) {
			Log.w(TAG, "Unable to respond to content request", e);
		} catch (IOException e) {
			Log.w(TAG, "Unable to respond to content request", e);
		} catch (NotFoundException e) {
			Log.w(TAG, "Unable to respond to content request", e);
		} catch (ManesNotRegisteredException e) {
			Log.w(TAG, "Unable to respond to content request", e);
		}
	}

	private void sendRequest(OutgoingRequestTask task) {
		// Send the request
		ByteBuffer buffer = PacketProtocol.createPacket();
		ContentRequestSerializer.serialize(buffer, task.getContentRequest());
		try {
			Log.v(TAG, "Sending content request for " + task.getContentRequest().getObjectHash());
			packetProtocol.send(buffer);
		} catch (ManesFrameTooLargeException e) {
			Log.w(TAG, "Unable to send content request", e);
		} catch (ManesNotRegisteredException e) {
			Log.w(TAG, "Unable to send content request", e);
		}

		// Requeue the task
		if (task.retry()) {
			executor.schedule(task, task.nextDelay(), TimeUnit.SECONDS);
		}
		else
			requests.remove(task.getHash(), task);
	}

	private abstract class RequestTask implements Runnable {

		private final Hash hash;
		private volatile boolean canceled;

		public RequestTask(Hash hash) {
			this.hash = hash;
			this.canceled = false;
		}

		public Hash getHash() {
			return hash;
		}

		public void cancel() {
			this.canceled = true;
		}

		public boolean isCanceled() {
			return this.canceled;
		}

	}

	private class IncomingRequestTask extends RequestTask {

		public IncomingRequestTask(Hash hash) {
			super(hash);
		}

		@Override
		public void run() {
			if (!isCanceled())
				sendContent(this);
		}
	}

	private class OutgoingRequestTask extends RequestTask {

		private final ContentRequest request;
		private int attempts;

		public OutgoingRequestTask(Hash hash) {
			super(hash);
			this.request = new ContentRequest(hash);
			this.attempts = 0;
		}

		public ContentRequest getContentRequest() {
			return request;
		}

		@Override
		public void run() {
			attempts++;
			if (!isCanceled())
				sendRequest(this);
		}

		public boolean retry() {
			return attempts < 7;
		}

		/**
		 * @return next scheduling delay in seconds
		 */
		public long nextDelay() {
			if (attempts >= 7)
				throw new IllegalStateException("Cannot attempt a task more than 7 times.");
			return (long) (Math.pow(3, attempts));
		}

	}

	/*
	 * Checking for the local existance of all incoming shouts will be moved
	 * into the ContentProvider, when access to avatars is moved there. Then,
	 * only shouts are not in the database already need to have their avatars
	 * checked.
	 */
	@Override
	public void sendShout(Shout shout) throws ShoutChainTooLongException,
			ManesNotRegisteredException {
		throw new IllegalStateException("Cannot send a shout");
	}

	@Override
	public void receive(Shout shout) {
		while (shout != null) {
			try {
				// Trigger retrieval of content, if we don't have it.
				// This is horribly inefficient
				contentManager.retrieve(shout.getSender().getAvatar().getHash());
			} catch (NotFoundException e) {
				// Ignore, we're just triggering retrieval
			} catch (IOException e) {
				// Ignore, we're just triggering retrieval
			} finally {
				shout = shout.getParent();
			}
		}
	}

}
