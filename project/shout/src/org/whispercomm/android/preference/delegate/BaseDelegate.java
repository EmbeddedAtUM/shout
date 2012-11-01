
package org.whispercomm.android.preference.delegate;

import org.whispercomm.android.preference.ExposedPreferenceManager;
import org.whispercomm.android.preference.DelegatedPreference.DelegateCallbacks;
import org.whispercomm.android.preference.DelegatedPreference.DelegateInterface;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.AbsSavedState;

/**
 * Base class for implementations of {@link DelegateInterface}.
 * <p>
 * This class handles saving of the {@link DelegateCallbacks},
 * {@link PreferenceManager}, and the {@code requestCode}.
 * 
 * @author David R. Bild
 */
public abstract class BaseDelegate<T> implements DelegateInterface<T> {

	private Context mContext;
	private PreferenceManager mPreferenceManager;
	private ExposedPreferenceManager mExposedPreferenceManager;
	private DelegateCallbacks<T> mCallbacks;
	private int mRequestCode;

	public BaseDelegate(Context context) {
		mContext = context;
	}

	/**
	 * Gets the {@link Context} associated with this delegate.
	 * 
	 * @return the context associated with this delegate
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Return a localized string from the application's package's default string
	 * table.
	 * 
	 * @param resId resource id for string
	 * @return the localized string
	 */
	protected String getString(int resId) {
		return getContext().getString(resId);
	}

	/**
	 * Gets the {@link PreferenceManager} associated with this delegate.
	 * 
	 * @return the associated preference manager
	 */
	public PreferenceManager getPreferenceManager() {
		return mPreferenceManager;
	}

	/**
	 * Gets the {@link ExposedPreferenceManager} associated with this delegate.
	 * 
	 * @return the associated exposed preference manager
	 */
	public ExposedPreferenceManager getExposedPreferenceManager() {
		return mExposedPreferenceManager;
	}

	@Override
	public void setPreferenceManager(PreferenceManager preferenceManager) {
		mPreferenceManager = preferenceManager;
		mExposedPreferenceManager = ExposedPreferenceManager.expose(mPreferenceManager);
	}

	@Override
	public void setDelegateCallbacks(DelegateCallbacks<T> callbacks) {
		mCallbacks = callbacks;
	}

	/**
	 * Gets the {@link DelegateCallbacks} object associated with this object.
	 * 
	 * @return the callbacks or {@code null} if none are attached
	 */
	protected DelegateCallbacks<T> getCallbacks() {
		return mCallbacks;
	}

	@Override
	public void setRequestCode(int requestCode) {
		mRequestCode = requestCode;
	}

	/**
	 * Gets the request code associated with the object.
	 * 
	 * @return the request code
	 */
	protected int getRequestCode() {
		return mRequestCode;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		return BaseSavedState.EMPTY_STATE;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state != null && state != BaseSavedState.EMPTY_STATE) {
			throw new IllegalArgumentException(
					"Wrong state class -- expecting BaseDelegate State");
		}
	}

	/**
	 * A base class for managing the instance state of a
	 * {@link DelegateInterface}
	 */
	public static class BaseSavedState extends AbsSavedState {

		public BaseSavedState(Parcel source) {
			super(source);
		}

		public BaseSavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
		}

		public static final Parcelable.Creator<BaseSavedState> CREATOR =
				new Parcelable.Creator<BaseSavedState>() {
					public BaseSavedState createFromParcel(Parcel in) {
						return new BaseSavedState(in);
					}

					public BaseSavedState[] newArray(int size) {
						return new BaseSavedState[size];
					}
				};
	}
}
