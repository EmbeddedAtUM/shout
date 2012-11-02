
package org.whispercomm.shout.content.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.whispercomm.shout.Hash;
import org.whispercomm.shout.errors.NotFoundException;

public class FileObjectStorage implements ObjectStorage {

	private final File root;

	private final Map<Hash, List<ObjectListener>> listeners;

	private final Lock listenersLock;

	public FileObjectStorage(File root) {
		this.root = root;
		this.listeners = new HashMap<Hash, List<ObjectListener>>();
		this.listenersLock = new ReentrantLock();
	}

	@Override
	public boolean exists(Hash hash) {
		return getFile(hash).exists();
	}

	@Override
	public byte[] retrieve(Hash hash) throws NotFoundException, IOException {
		try {
			byte[] data = FileUtils.readFileToByteArray(getFile(hash));
			if (hash.equals(Hash.hashData(data))) {
				return data;
			} else {
				throw new NotFoundException("The file contents do not match the hash.");
			}
		} catch (FileNotFoundException e) {
			throw new NotFoundException(String.format("File '%s' not found.", hash));
		}
	}

	@Override
	public Hash store(byte[] data) throws IOException {
		return store(data, 0, data.length);
	}

	@Override
	public Hash store(byte[] data, int offset, int len) throws IOException {
		Hash hash = Hash.hashData(data, offset, len);
		FileUtils.writeByteArrayToFile(getFile(hash), data, offset, len);
		notifyListeners(hash);
		return hash;
	}

	private File getFile(String filename) {
		File dir = new File(root, filename.substring(0, 2));
		return new File(dir, filename.substring(2));
	}

	private File getFile(Hash hash) {
		return getFile(hash.toString());
	}

	@Override
	public void registerListener(ObjectListener listener, Hash hash) {
		listenersLock.lock();
		try {
			List<ObjectListener> l = listeners.get(hash);
			if (l == null) {
				l = new ArrayList<ObjectListener>();
				listeners.put(hash, l);
			}
			l.add(listener);
		} finally {
			listenersLock.unlock();
		}
	}

	@Override
	public void unregisterListener(ObjectListener listener, Hash hash) {
		listenersLock.lock();
		try {
			List<ObjectListener> l = listeners.get(hash);
			if (l == null)
				return;

			l.remove(listener);
			if (l.isEmpty())
				listeners.remove(hash);
		} finally {
			listenersLock.unlock();
		}
	}

	private void notifyListeners(Hash hash) {
		listenersLock.lock();
		try {
			List<ObjectListener> l = listeners.get(hash);
			if (l == null)
				return;

			for (ObjectListener listener : l) {
				listener.stored(hash);
			}
		} finally {
			listenersLock.unlock();
		}
	}
}
