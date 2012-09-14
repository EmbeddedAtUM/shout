
package org.whispercomm.shout.id;

import org.whispercomm.shout.Me;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.crypto.EcdsaWithSha256;
import org.whispercomm.shout.serialization.SerializeUtility;

public class SignatureUtility {
	@SuppressWarnings("unused")
	private static final String TAG = SignatureUtility.class.getSimpleName();

	public static DsaSignature signShout(UnsignedShout shout, Me me) {
		return EcdsaWithSha256.sign(SerializeUtility.serializeShoutData(shout), me.getPrivateKey());
	}

	public static boolean verifyShout(Shout shout) {
		return EcdsaWithSha256.verify(shout.getSignature(),
				SerializeUtility.serializeShoutData(shout), shout.getSender().getPublicKey());
	}

	private SignatureUtility() {
		throw new IllegalStateException("Cannot instantiate SignatureUtility.");
	}
}
