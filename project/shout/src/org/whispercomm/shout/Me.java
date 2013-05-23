
package org.whispercomm.shout;

import org.whispercomm.shout.crypto.ECPrivateKey;

public interface Me extends User {

	public ECPrivateKey getPrivateKey();

}
