
package org.whispercomm.shout.id;

import org.whispercomm.shout.Me;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.SimpleShout;
import org.whispercomm.shout.UnsignedShout;
import org.whispercomm.shout.crypto.DsaSignature;
import org.whispercomm.shout.crypto.EcdsaWithSha256;
import org.whispercomm.shout.serialization.SerializeUtility;

public class SignatureUtility {
	@SuppressWarnings("unused")
	private static final String TAG = SignatureUtility.class.getSimpleName();

	public static Shout signShout(UnsignedShout shout, Me me) {
		DsaSignature sig = EcdsaWithSha256.sign(SerializeUtility.serializeShoutData(shout),
				me.getPrivateKey());
		return new SimpleShout(SerializeUtility.VERSION, shout.getTimestamp(), shout.getSender(),
				shout.getMessage(),
				shout.getLocation(),
				shout.getParent(), sig);
	}

	public static boolean verifyShout(Shout shout) {
		return EcdsaWithSha256.verify(shout.getSignature(),
				SerializeUtility.serializeShoutData(shout), shout.getSender().getPublicKey());
	}

	private SignatureUtility() {
		throw new IllegalStateException("Cannot instantiate SignatureUtility.");
	}
}
