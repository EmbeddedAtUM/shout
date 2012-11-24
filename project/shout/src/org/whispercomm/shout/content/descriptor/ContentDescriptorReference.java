
package org.whispercomm.shout.content.descriptor;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.SimpleHashReference;

public class ContentDescriptorReference extends SimpleHashReference<ContentDescriptor>
{

	public ContentDescriptorReference(Hash hash, ContentDescriptorStore store) {
		super(hash);
	}

	public ContentDescriptorReference(ContentDescriptor descriptor,
			ContentDescriptorStore store) {
		super(descriptor.getHash(), descriptor);
	}

}
