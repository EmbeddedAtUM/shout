
package org.whispercomm.shout.content.descriptor;

import java.io.IOException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.content.storage.ObjectStorage;
import org.whispercomm.shout.content.storage.ObjectStorage.ObjectListener;
import org.whispercomm.shout.errors.InvalidFormatException;
import org.whispercomm.shout.errors.NotFoundException;
import org.whispercomm.shout.network.UnsupportedVersionException;

import android.util.Log;

public class ContentDescriptorStore {
	private static final String TAG = ContentDescriptorStore.class.getSimpleName();

	private final ObjectStorage storage;

	public ContentDescriptorStore(ObjectStorage storage) {
		this.storage = storage;
	}

	public ContentDescriptorReference retrieve(Hash hash) {
		try {
			ContentDescriptor descriptor = ContentDescriptorSerializer.deserialize(storage
					.retrieve(hash));
			return new ContentDescriptorReference(descriptor, this);
		} catch (NotFoundException e) {
			// TODO: For all exceptions, figure out if anyone should be
			// notified or if the bad file should be removed.
			// Ignore, we may just not have it.
		} catch (IOException e) {
			Log.w(TAG, "Failed to retrieve content descriptor.", e);
		} catch (UnsupportedVersionException e) {
			Log.w(TAG, "Unable to retrieve content descriptor due to bad version.", e);
		} catch (InvalidFormatException e) {
			Log.w(TAG, "Unable to retrieve content descriptor due to bad encoding.", e);
		}

		return new ContentDescriptorReference(hash, this);
	}

	public ContentDescriptorReference store(ContentDescriptor descriptor) throws IOException {
		if (!storage.exists(descriptor.getHash()))
			storage.store(ContentDescriptorSerializer.serialize(descriptor));
		return new ContentDescriptorReference(descriptor, this);
	}

	public void register(ObjectListener listener, Hash hash) {
		storage.registerListener(listener, hash);
	}

	public void unregister(ObjectListener listener, Hash hash) {
		storage.unregisterListener(listener, hash);
	}

}
