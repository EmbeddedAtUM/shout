
package org.whispercomm.shout.content;

import java.io.File;
import java.io.IOException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.MimeType;
import org.whispercomm.shout.content.descriptor.ContentDescriptor;
import org.whispercomm.shout.content.descriptor.ContentDescriptorReference;
import org.whispercomm.shout.content.descriptor.ContentDescriptorStore;
import org.whispercomm.shout.content.merkle.MerkleStore;
import org.whispercomm.shout.content.storage.FileObjectStorage;
import org.whispercomm.shout.content.storage.ObjectStorage;
import org.whispercomm.shout.errors.NotFoundException;

import android.content.ContentProvider;
import android.content.Context;

/**
 * This class functions as a local content provider. Eventually, it should be
 * replaced with a true {@link ContentProvider} implementation.
 * 
 * @author David R. Bild
 */
public class ContentManager {

	private static final String OBJECT_ROOT_DIR = "objects";

	private File mObjectDir;
	private ObjectStorage mObjectStorage;
	private MerkleStore mMerkleStore;
	private ContentDescriptorStore mContentDescriptorStore;

	public ContentManager(Context context) {
		mObjectDir = new File(context.getExternalFilesDir(null), OBJECT_ROOT_DIR);
		if (!mObjectDir.exists())
			mObjectDir.mkdir();

		mObjectStorage = new FileObjectStorage(mObjectDir, context);

		mMerkleStore = new MerkleStore(mObjectStorage);
		mContentDescriptorStore = new ContentDescriptorStore(mObjectStorage);
	}

	public Hash store(byte[] data, MimeType mimetype) throws IOException {
		return mContentDescriptorStore.store(new ContentDescriptor(mMerkleStore.putObject(data)
				.getHash(),
				mimetype)).getHash();
	}

	public Hash store(Content content) throws IOException {
		return store(content.getData(), content.getMimeType());
	}

	public Content retrieve(Hash hash) throws NotFoundException, IOException {
		ContentDescriptorReference descriptorRef = mContentDescriptorStore.retrieve(hash);
		if (!descriptorRef.isAvailable())
			throw new NotFoundException();

		ContentDescriptor descriptor = descriptorRef.get();
		byte[] data = mMerkleStore.getObject(descriptor.getObjectRoot());

		return new Content(data, descriptor.getMimeType());
	}

	public ObjectStorage getObjectStorage() {
		return mObjectStorage;
	}

	public MerkleStore getMerkleStore() {
		return mMerkleStore;
	}

	public ContentDescriptorStore getDescriptorStore() {
		return mContentDescriptorStore;
	}

}
