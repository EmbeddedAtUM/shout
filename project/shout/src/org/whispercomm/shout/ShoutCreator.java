
package org.whispercomm.shout;

import org.joda.time.DateTime;
import org.whispercomm.shout.colorstorage.ShoutBorder;
import org.whispercomm.shout.id.SignatureUtility;
import org.whispercomm.shout.provider.ShoutColorContract;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.content.Context;

/**
 * Helper class with methods for creating new shouts, reshouts, and comments.
 * The newly-created shouts are stored in the Shout content provider.
 * 
 * @author David R. Bild
 */
public class ShoutCreator {
	@SuppressWarnings("unused")
	private static final String TAG = ShoutCreator.class.getSimpleName();

	private Context context;

	public ShoutCreator(Context context) {
		this.context = context;
	}

	/**
	 * Creates a new shout, storing it in the content provider.
	 * 
	 * @param timestamp the timestamp field of the shout
	 * @param message the message field of the shout
	 * @param sender the identity for the sender field of the shout
	 * @return the saved shout
	 */
	public LocalShout createShout(DateTime timestamp, String message, Location location, Me sender) {
		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, sender,
				message, location, null);
		Shout shout = SignatureUtility.signShout(unsigned, sender);
		ShoutBorder shoutBorder = new ShoutBorder(shout.getSender().getUsername(), shout
				.getSender().getPublicKey());
		ShoutColorContract.saveShoutBorder(context, shoutBorder);
		return ShoutProviderContract.saveShout(context, shout);
	}

	/**
	 * Creates a new comment shout, storing it in the content provider.
	 * 
	 * @param timestamp the timestamp field of the shout
	 * @param message the message field of the shout
	 * @param parent the parent field of the shout
	 * @param sender the identity for the sender field of the shout
	 * @return the saved shout
	 */
	public LocalShout createComment(DateTime timestamp, String message, Location location,
			Shout parent, Me sender) {
		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, sender,
				message, location, parent);
		Shout shout = SignatureUtility.signShout(unsigned, sender);
		return ShoutProviderContract.saveShout(context, shout);
	}

	/**
	 * Creates a new reshout, storing it in the content provider.
	 * 
	 * @param timestamp the timestamp field of the shout
	 * @param parent the parent field of the shout
	 * @param sender the identity for the sender field of the shout
	 * @return the saved shout
	 */
	public LocalShout createReshout(DateTime timestamp, Location location, Shout parent, Me sender) {
		UnsignedShout unsigned = new SimpleUnsignedShout(timestamp, sender,
				null, location, parent);
		Shout shout = SignatureUtility.signShout(unsigned, sender);
		return ShoutProviderContract.saveShout(context, shout);
	}

}
