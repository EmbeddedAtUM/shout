
package org.whispercomm.android.preference.delegate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

/**
 * Abstract implementation of {@link BaseDelegate} that starts an
 * {@link Activity} when the delegate is chosen. Subclasses must implemenation
 * {@link #onPrepareIntent(Intent)} to setup the intent used to start the
 * activity.
 * 
 * @author David R. Bild
 * @param <T> the type of the preference value
 */
public abstract class ActivityDelegate<T> extends BaseDelegate<T> {

	protected Intent mIntent;

	/**
	 * Sets up the {@link Intent} used to start the activity.
	 * 
	 * @param intent the intent to be configured to start the activity
	 */
	protected abstract void onPrepareIntent(Intent intent);

	public ActivityDelegate(Context context) {
		super(context);
		mIntent = new Intent();
		onPrepareIntent(mIntent);
	}

	@Override
	public void onDelegate() {
		getCallbacks().startActivityForResult(mIntent, getRequestCode());
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		return superState;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}

}
