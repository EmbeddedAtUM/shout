
package org.whispercomm.shout.provider;

import org.whispercomm.shout.LocalShout;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * An extension of {@link CursorAdapter} that converts results from the
 * underlying cursor to {@link LocalShout} objects. The abstract
 * {@link #bindView(View, Context, LocalShout)} and
 * {@link #newView(Context, LocalShout, ViewGroup)} methods take a
 * {@code LocalShout} instead of a {@link Cursor}.
 * 
 * @see CursorAdapter
 */
public abstract class ShoutCursorAdapter extends CursorAdapter {

	public ShoutCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	public ShoutCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	/**
	 * Bind an existing view to the specified shout.
	 * 
	 * @param view Existing view, returned earlier by newView
	 * @param context Interface to application's global information
	 * @param shout The shout to bind to the view
	 */
	public abstract void bindView(View view, Context context, LocalShout shout);

	/**
	 * Makes a new view to hold the specified shout.
	 * 
	 * @param context Interface to application's global information
	 * @param shout The shout to bind to the new view
	 * @param parent The parent to which the new view is attached to
	 * @return the newly created view.
	 */
	public abstract View newView(final Context context, LocalShout shout, ViewGroup parent);

	@Override
	public LocalShout getItem(int position) {
		Cursor c = (Cursor) super.getItem(position);
		return ShoutProviderContract.retrieveShoutFromCursor(mContext, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final LocalShout shout = ShoutProviderContract.retrieveShoutFromCursor(
				context, cursor);
		bindView(view, context, shout);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LocalShout shout = ShoutProviderContract.retrieveShoutFromCursor(
				context, cursor);
		return newView(context, shout, parent);
	}

}
