
package org.whispercomm.shout.content.storage;

import java.io.IOException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.errors.NotFoundException;

public interface ObjectStorage {

	public boolean exists(Hash hash);

	public byte[] retrieve(Hash hash) throws NotFoundException, IOException;

	public Hash store(byte[] data) throws IOException;

	public Hash store(byte[] data, int offset, int len) throws IOException;

	public void registerListener(ObjectListener listener, Hash hash);

	public void unregisterListener(ObjectListener listener, Hash hash);

	public interface ObjectListener {
		public void stored(Hash hash);
	}

}
