
package org.whispercomm.shout;

import org.whispercomm.shout.crypto.ECPrivateKey;

public interface Me extends LocalUser {

	public ECPrivateKey getPrivateKey();

}
