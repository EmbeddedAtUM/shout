package org.whispercomm.shout.tasks;

import org.joda.time.DateTime;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.Me;
import org.whispercomm.shout.Shout;
import org.whispercomm.shout.ShoutCreator;

import android.content.Context;

/**
 * Creates a new comment shout and saves it to the content provider.
 * 
 * @author David R. Bild
 * 
 */
public class CommentTask extends AsyncTaskCallback<String, Void, LocalShout> {

	private Context context;
	private Shout parent;
	private Me me;

	/**
	 * Create a new {@code CommentTask}.
	 * <p>
	 * If the comment creation succeeds, the created {@link LocalShout} is
	 * passed to the specified callback. If creation fails, {@code null} is
	 * passed instead.
	 * 
	 * @param context
	 *            the context used to connect to the content provider
	 * @param completeListener
	 *            the callback to invoke when the creation is complete.
	 * @param me
	 *            the user for the sender field of the shout.
	 * @param parent
	 *            the shout being commented on
	 */
	public CommentTask(Context context,
			AsyncTaskCompleteListener<LocalShout> completeListener, Me me,
			Shout parent) {
		super(completeListener);
		this.context = context;
		this.parent = parent;
		this.me = me;
	}

	@Override
	protected LocalShout doInBackground(String... params) {
		String message = params[0];

		ShoutCreator creator = new ShoutCreator(context);
		return creator.createComment(DateTime.now(), message, parent, me);
	}

}
