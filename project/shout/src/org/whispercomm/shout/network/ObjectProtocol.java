
package org.whispercomm.shout.network;

import java.nio.ByteBuffer;

/**
 * Interface for object protocol handlers.
 * 
 * @author David R. Bild
 */
public interface ObjectProtocol {

	/**
	 * Callback for receiving serialized objects. This method must return with
	 * the buffer position set just past the end of the serialized object and
	 * the limit unchanged.
	 * 
	 * @param type the type of the object serialized in {@code data}
	 * @param data the buffer containing the serialized object.
	 */
	public void receive(ObjectType type, ByteBuffer data);

}
