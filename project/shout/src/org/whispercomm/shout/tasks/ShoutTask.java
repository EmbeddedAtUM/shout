package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.ShoutCreator;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;

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
	 *            the callback to invoke when the creation is complete.
	 */
	public ShoutTask(Context context,
			AsyncTaskCompleteListener<LocalShout> completeListener) {
		super(completeListener);
		this.context = context;
		try {
			this.me = new IdManager(context).getMe();
		} catch (UserNotInitiatedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected LocalShout doInBackground(String... params) {
		String message = params[0];

		ShoutCreator creator = new ShoutCreator(context);
		return creator.createShout(DateTime.now(), message, me);
	}

}
