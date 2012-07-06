package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;

// TODO Switch to static after sig utility is static
public class ShoutCreator {

	static final String TAG = ShoutCreator.class.getSimpleName();

	private Context context;
	private IdManager idManager;

	public ShoutCreator(Context context) {
		this.context = context;
		idManager = new IdManager(context);
	}

	/**
	 * Creates a new shout, storing it in the content provider.
	 * 
	 * @param timestamp
	 *            the timestamp field of the shout
	 * @param message
	 *            the message field of the shout
	 * @return the saved shout
	 */
	public LocalShout createShout(DateTime timestamp, String message) {
		Me me;
		try {
			me = idManager.getMe();
		} catch (UserNotInitiatedException e1) {
			// TODO
			e1.printStackTrace();
			return null;
		}

		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, me,
				message, null);
		byte[] signature = SignatureUtility.generateSignature(unsigned, me);
		Shout shout = new SimpleShout(timestamp, me, message, null, signature);

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
	 * @return the saved shout
	 */
	public LocalShout createComment(DateTime timestamp, String message,
			Shout parent) {
		Me me;
		try {
			me = idManager.getMe();
		} catch (UserNotInitiatedException e1) {
			// TODO
			e1.printStackTrace();
			return null;
		}

		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, me,
				message, null);
		byte[] signature = SignatureUtility.generateSignature(unsigned, me);
		Shout shout = new SimpleShout(timestamp, me, message, parent, signature);

		return ShoutProviderContract.saveShout(context, shout);
	}

	/**
	 * Creates a new reshout, storing it in the content provider.
	 * 
	 * @param timestamp
	 *            the timestamp field of the shout
	 * @param parent
	 *            the parent field of the shout
	 * @return the saved shout
	 */
	public LocalShout createReshout(DateTime timestamp, Shout parent) {
		Me me;
		try {
			me = idManager.getMe();
		} catch (UserNotInitiatedException e1) {
			// TODO
			e1.printStackTrace();
			return null;
		}

		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, me, null,
				null);
		byte[] signature = SignatureUtility.generateSignature(unsigned, me);
		Shout shout = new SimpleShout(timestamp, me, null, parent, signature);

		return ShoutProviderContract.saveShout(context, shout);
	}

}
