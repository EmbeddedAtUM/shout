
package org.whispercomm.shout.network.content;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.content.descriptor.ContentDescriptor;
import org.whispercomm.shout.content.merkle.MerkleNode;
import org.whispercomm.shout.content.request.ContentRequest;

public interface ContentRequestHandler {

	public void request(Hash hash);

	public void receiveRequest(ContentRequest request);

	public void receiveContentDescriptor(ContentDescriptor descriptor);

	public void receiveMerkleNode(MerkleNode node);

}
