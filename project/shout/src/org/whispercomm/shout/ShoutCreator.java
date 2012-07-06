package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;

// TODO Switch to static after sig utility is static
public class ShoutCreator {

	static final String TAG = ShoutCreator.class.getSimpleName();

	private Context context;

	public ShoutCreator(Context context) {
		this.context = context;
	}

	/**
	 * Creates a new shout, storing it in the content provider.
	 * 
	 * @param timestamp
	 *            the timestamp field of the shout
	 * @param message
	 *            the message field of the shout
	 * @param sender
	 *            the identity for the sender field of the shout
	 * @return the saved shout
	 */
	public LocalShout createShout(DateTime timestamp, String message, Me sender) {
		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, sender,
				message, null);
		byte[] signature = SignatureUtility.generateSignature(unsigned, sender);
		Shout shout = new SimpleShout(timestamp, sender, message, null,
				signature);

		return ShoutProviderContract.saveShout(context, shout);
	}

	/**
	 * Creates a new comment shout, storing it in the content provider.
	 * 
	 * @param timestamp
	 *            the timestamp field of the shout
	 * @param message
	 *            the message field of the shout
	 * @param parent
	 *            the parent field of the shout
	 * @param sender
	 *            the identity for the sender field of the shout
	 * @return the saved shout
	 */
	public LocalShout createComment(DateTime timestamp, String message,
			Shout parent, Me sender) {
		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, sender,
				message, null);
		byte[] signature = SignatureUtility.generateSignature(unsigned, sender);
		Shout shout = new SimpleShout(timestamp, sender, message, parent,
				signature);

		return ShoutProviderContract.saveShout(context, shout);
	}

	/**
	 * Creates a new reshout, storing it in the content provider.
	 * 
	 * @param timestamp
	 *            the timestamp field of the shout
	 * @param parent
	 *            the parent field of the shout
	 * @param sender
	 *            the identity for the sender field of the shout
	 * @return the saved shout
	 */
	public LocalShout createReshout(DateTime timestamp, Shout parent, Me sender) {
		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, sender,
				null, null);
		byte[] signature = SignatureUtility.generateSignature(unsigned, sender);
		Shout shout = new SimpleShout(timestamp, sender, null, parent,
				signature);

		return ShoutProviderContract.saveShout(context, shout);
	}

}
