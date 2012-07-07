package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.ShoutCreator;

import android.content.Context;

/**
 * Creates a new root shout and saves it to the content provider.
 * 
 * @author David R. Bild
 * 
 */
public class ShoutTask extends AsyncTaskCallback<String, Void, LocalShout> {

	private Context context;
	private Me me;

	/**
	 * Create a new {@code ShoutTask}.
	 * <p>
	 * If the shout creation succeeds, the created {@link LocalShout} is passed
	 * to the specified callback. If creation fails, {@code null} is passed
	 * instead.
	 * 
	 * @param context
	 *            the context used to connect to the content provider
	 * @param completeListener
	 *            the callback to invoke when the creation is complete
	 * @param me
	 *            the user for the sender field of the shout
	 */
	public ShoutTask(Context context,
			AsyncTaskCompleteListener<LocalShout> completeListener, Me me) {
		super(completeListener);
		this.context = context;
		this.me = me;
	}

	@Override
	protected LocalShout doInBackground(String... params) {
		String message = params[0];

		ShoutCreator creator = new ShoutCreator(context);
		return creator.createShout(DateTime.now(), message, me);
	}

}
