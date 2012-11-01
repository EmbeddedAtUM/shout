
package org.whispercomm.android.preference;

import java.util.ArrayList;
import java.util.List;

import org.whispercomm.shout.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Base class for preferences that delegate preference selection and retrieval
 * to {@link DelegateInterface} implementations. A delegate may, for example,
 * present an {@link Activity} or {@link Dialog} to the user to select the new
 * value for the preference.
 * <p>
 * See {@link DelegateInterface} for instructions on using delegates. Delegates
 * should be registered in the subclass constructor using
 * {@link #registerDelegate(DelegateInterface)}.
 * <p>
 * When the preference is clicked, the user is presented with a list of
 * delegates to choose from.
 * <p>
 * Delegates may choose to retrieve the full value of a preference in the
 * background after selection is made. {@code DelegatedPreference} will show a
 * view with id {@code R.id.progressBar}, if one exists, while this background
 * loading takes place. If the user clicks the preference during this time, the
 * loading and preference change is canceled.
 * 
 * @author David R. Bild
 * @param <T>
 */
public class DelegatedPreference<T> extends Preference implements OnActivityResultListener,
		PreferenceManager.OnActivityDestroyListener, DialogInterface.OnDismissListener {

	private State mState;
	private List<DelegateInterface<T>> mDelegates;
	private ExposedPreferenceManager mExposedPreferenceManager;
	private DelegateCallbacks<T> mCallbacks;

	private ProgressBar mProgressBar;
	private CharSequence mDialogTitle;
	private CharSequence mCancelText;

	private int mCurrentDelegate;
	private boolean mProgressBarVisible;

	private Dialog mDialog;

	public DelegatedPreference(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.delegatedPreferenceStyle);
	}

	public DelegatedPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mState = State.IDLE;
		mDelegates = new ArrayList<DelegateInterface<T>>();
		mCurrentDelegate = -1;
		mProgressBarVisible = false;

		mCallbacks = new DelegateCallbacksImpl();

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DelegatedPreference, defStyle, 0);
		mCancelText = a.getString(R.styleable.DelegatedPreference_cancelText);
		mDialogTitle = a.getString(R.styleable.DelegatedPreference_android_dialogTitle);
		if (mDialogTitle == null)
			mDialogTitle = getTitle();
		a.recycle();
	}

	/**
	 * Registers a new delegate. In order to properly preserve state across
	 * configuration changes, registration should be done in the same order each
	 * time a particular {@link DelegatedPreference} subclass instance is
	 * initialized.
	 * 
	 * @param delegate the delegate to register
	 */
	protected void registerDelegate(DelegateInterface<T> delegate) {
		delegate.setDelegateCallbacks(mCallbacks);
		synchronized (this) {
			mDelegates.add(delegate);
		}
	}

	/**
	 * Sets the title used for the delegate selection dialog.
	 * 
	 * @param title the title used for the delegate selection dialog
	 */
	public void setDialogTitle(CharSequence title) {
		mDialogTitle = title;
	}

	/**
	 * Gets the title used for the delegate selection dialog.
	 * 
	 * @return the title used for the delegate selection dialog
	 */
	public CharSequence getDialogTitle() {
		return mDialogTitle;
	}

	/**
	 * Sets the text for the {@link Toast} displayed when the user cancels the
	 * change of a preference.
	 * 
	 * @param text the text to display when a preference change is canceled
	 */
	public void setCancelText(CharSequence text) {
		mCancelText = text;
	}

	/**
	 * Gets the text for the {@link Toast} displayed when the user cancels the
	 * change of a preference.
	 * 
	 * @return the text displayed when a preference change is canceled
	 */
	public CharSequence getCancelText() {
		return mCancelText;
	}

	/**
	 * Called with the newly chosen preference value. Subclasses should override
	 * this method.
	 * 
	 * @param result the new preference value
	 */
	public void onResult(T result) {
		// Subclasses should override
	}

	/**
	 * Called if an error occurs while retrieving the new preference value. The
	 * default implementation displays the {@code message} as a {@link Toast},
	 * but subclasses may override this method.
	 * 
	 * @param message a human-readable description of the error
	 */
	public void onError(String message) {
		Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
		super.onAttachedToHierarchy(preferenceManager);

		mExposedPreferenceManager = ExposedPreferenceManager.expose(preferenceManager);
		mExposedPreferenceManager.registerOnActivityResultListener(this);

		synchronized (this) {
			for (DelegateInterface<T> delegate : mDelegates) {
				delegate.setPreferenceManager(getPreferenceManager());
				delegate.setRequestCode(mExposedPreferenceManager.getNextRequestCode());
			}
		}
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		View progressView = view.findViewById(R.id.progressBar);
		if (progressView != null && progressView instanceof ProgressBar) {
			mProgressBar = (ProgressBar) progressView;
			refreshProgressBar();
		}
	}

	private void setProgressBarVisible(boolean visible) {
		mProgressBarVisible = visible;
		refreshProgressBar();
	}

	private void refreshProgressBar() {
		if (mProgressBar != null)
			mProgressBar.setVisibility(mProgressBarVisible ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		for (DelegateInterface<T> delegate : mDelegates) {
			if (delegate.onActivityResult(requestCode, resultCode, data))
				return true;
		}
		return false;
	}

	/**
	 * Called when the user cancels retrieval of a preference value after the
	 * delegate has closed its UI but before the full preference value is
	 * retrieved.
	 * <p>
	 * The default implementation shows a toast displaying the text returned by
	 * {@link #getCancelText()}.
	 */
	protected void onCancel() {
		Toast.makeText(getContext(), getCancelText(), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick() {
		switch (mState) {
			case IDLE:
				transitionPreDelegation();
				break;
			case POST_DELEGATION:
				mDelegates.get(mCurrentDelegate).onCancel();
				transitionIdle();
				onCancel();
				break;
			case PRE_DELEGATION:
			case DELEGATION:
				throw new IllegalStateException("Preference should be unclickable in state "
						+ mState);
		}
	}

	/**
	 * Called when the state changes to idle. Must call through to super.
	 */
	protected void transitionIdle() {
		mState = State.IDLE;
		mCurrentDelegate = -1;
		mDialog = null;
		setProgressBarVisible(false);
	}

	/**
	 * Called when the state changes to pre-delegation. Must call through to
	 * super.
	 */
	protected void transitionPreDelegation() {
		mState = State.PRE_DELEGATION;
		showSelectionDialog();
	}

	/**
	 * Called when the state changes to delegation. Must call through to super.
	 * 
	 * @param which the delegate that is active
	 */
	protected void transitionDelegation(int which) {
		mState = State.DELEGATION;
		mDialog = null;
		mCurrentDelegate = which;
		mDelegates.get(mCurrentDelegate).onDelegate();
	}

	/**
	 * Called when the state changes to post-delegation. Must call through to
	 * super.
	 */
	protected void transitionPostDelegation() {
		mState = State.POST_DELEGATION;
		setProgressBarVisible(true);
	}

	/**
	 * Implementation for {@link DelegateCallbacks#onSelected()}.
	 */
	private void onSelectedImpl() {
		switch (mState) {
			case DELEGATION:
				transitionPostDelegation();
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"In state %s.  Expected state %s.", mState, State.DELEGATION));
		}
	}

	/**
	 * Implementation for {@link DelegateCallbacks#onResult(Object)}.
	 * 
	 * @param result the new preference value
	 */
	private void onResultImpl(T result) {
		switch (mState) {
			case POST_DELEGATION:
				onResult(result);
				transitionIdle();
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"In state %s.  Expected state %s.", mState, State.POST_DELEGATION));
		}
	}

	/**
	 * Implementation for {@link DelegateCallbacks#onCancel()}.
	 */
	private void onCancelImpl() {
		switch (mState) {
			case DELEGATION:
				transitionIdle();
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"In state %s.  Expected state %s.", mState, State.DELEGATION));
		}
	}

	/**
	 * Implementation for {@link DelegateCallbacks#onError(String)}.
	 * 
	 * @param message the error message
	 */
	private void onErrorImpl(String message) {
		switch (mState) {
			case DELEGATION:
			case POST_DELEGATION:
				onError(message);
				transitionIdle();
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"In state %s.  Expected state %s or %s.", mState, State.DELEGATION,
						State.POST_DELEGATION));
		}
	}

	private void showSelectionDialog() {
		final Context context = getContext();

		// Build list of prompts
		final CharSequence[] options = new CharSequence[mDelegates.size()];
		for (int i = 0; i < options.length; ++i) {
			options[i] = mDelegates.get(i).getSelectionPrompt();
		}

		// Build dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setTitle(mDialogTitle)
				.setItems(options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handleDialogOnClick(which);
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						handleDialogOnCancel();
					}
				});

		mExposedPreferenceManager.registerOnActivityDestroyListener(this);

		Dialog dialog = mDialog = builder.create();
		dialog.setOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Called by the {@code OnClickListener} of the selection dialog
	 * 
	 * @param which
	 */
	private void handleDialogOnClick(int which) {
		switch (mState) {
			case PRE_DELEGATION:
				transitionDelegation(which);
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"In state %s.  Expected state %s.", mState, State.PRE_DELEGATION));
		}
	}

	/**
	 * Called by the {@code OnCancelListener} of the selection dialog
	 */
	private void handleDialogOnCancel() {
		switch (mState) {
			case PRE_DELEGATION:
				transitionIdle();
				break;
			default:
				throw new IllegalArgumentException(String.format(
						"In state %s.  Expected state %s.", mState, State.PRE_DELEGATION));
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mExposedPreferenceManager.unregisterOnActivityDestroyListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onActivityDestroy() {
		if (mDialog == null || !mDialog.isShowing()) {
			return;
		}

		mDialog.dismiss();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();

		final SavedState myState = new SavedState(superState);
		myState.state = mState;
		myState.currentDelegate = mCurrentDelegate;
		myState.progressBarVisible = mProgressBarVisible;
		myState.isDialogShowing = mDialog != null;

		myState.delegates = new Parcelable[mDelegates.size()];
		int i = 0;
		for (DelegateInterface<T> delegate : mDelegates) {
			myState.delegates[i++] = delegate.onSaveInstanceState();
		}

		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		mState = myState.state;
		mCurrentDelegate = myState.currentDelegate;
		mProgressBarVisible = myState.progressBarVisible;

		int i = 0;
		for (DelegateInterface<T> delegate : mDelegates) {
			delegate.onRestoreInstanceState(myState.delegates[i++]);
		}

		if (myState.isDialogShowing)
			showSelectionDialog();
	}

	/**
	 * State that should be persisted across configuration changes.
	 */
	private static class SavedState extends BaseSavedState {
		State state;
		int currentDelegate;
		boolean progressBarVisible;
		Parcelable[] delegates;
		boolean isDialogShowing;

		public SavedState(Parcel source) {
			super(source);
			state = State.valueOf(source.readString());
			currentDelegate = source.readInt();
			progressBarVisible = source.readInt() == 1;
			delegates = source.readParcelableArray(null);
			isDialogShowing = source.readInt() == 1;
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(state.name());
			dest.writeInt(currentDelegate);
			dest.writeInt(progressBarVisible ? 1 : 0);
			dest.writeParcelableArray(delegates, flags);
			dest.writeInt(isDialogShowing ? 1 : 0);
		}

		@SuppressWarnings("unused")
		// Used by Android system
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};

	}

	private class DelegateCallbacksImpl implements DelegateCallbacks<T> {

		@Override
		public void startActivityForResult(Intent intent, int requestCode) {
			mExposedPreferenceManager.getActivity().startActivityForResult(intent, requestCode);
		}

		@Override
		public void onSelected() {
			onSelectedImpl();
		}

		@Override
		public void onResult(T result) {
			onResultImpl(result);
		}

		@Override
		public void onCancel() {
			onCancelImpl();
		}

		@Override
		public void onError(String message) {
			onErrorImpl(message);
		}

	}

	/**
	 * Enumeration of the internal states of the preference.
	 */
	protected enum State {
		/**
		 * When the preference is not active.
		 */
		IDLE,
		/**
		 * When the user is selecting a delegate.
		 */
		PRE_DELEGATION,
		/**
		 * When the delegate UI is active.
		 */
		DELEGATION,
		/**
		 * When the delegate UI is closed, but still loading the full preference
		 * value.
		 */
		POST_DELEGATION
	}

	/**
	 * Interface for a preference delegate. The delegate is responsible for
	 * getting the new preference value from the user and reporting it back to
	 * the hosting {@link DelegatedPreference} instance.
	 * <p>
	 * Usage:
	 * <p>
	 * The {@code DelegatedPreference} communicates with the delegate via this
	 * interface. The interface communicates with the preference via a
	 * {@link DelegateCallbacks} object set via the
	 * {@link #setDelegateCallbacks(DelegateCallbacks)} method.
	 * <p>
	 * {@link #onDelegate()} is called to start the delegation. At this point,
	 * the delegate should display its UI.
	 * <p>
	 * When the user has selected a new value for the preference, call
	 * {@link DelegateCallbacks#onSelected()}, close the UI, and if needed begin
	 * loading the full value of the preference (for example, if the user
	 * selected an image from a thumbnail, but the full resolution image must be
	 * downloaded).
	 * <p>
	 * When the preference value is available, return it via
	 * {@link DelegateCallbacks#onResult(Object)}.
	 * <p>
	 * If the user cancels the selection process without choosing a value, call
	 * {@link DelegateCallbacks#onCancel()} and close the UI. If an error
	 * occurs, call {@link DelegateCallbacks#onError(String)} and close the UI.
	 * <p>
	 * {@link #onCancel()} may be called at any time and should cancel the
	 * background loading of the preference value, if applicable.
	 * <p>
	 * Starting an Activity:
	 * <p>
	 * The delegate may need to start an {@link Activity} to return a result.
	 * This is supported via the standard
	 * {@link Activity#startActivityForResult(Intent, int)} method, exposed as
	 * {@link DelegateCallbacks#startActivityForResult(Intent, int)}. Use the
	 * request code set by {@link #setRequestCode(int)}. When the
	 * {@code Activity} result is available, it will be returned via
	 * {@link #onActivityResult(int, int, Intent)}.
	 * <p>
	 * Use the {@link #onSaveInstanceState()} and
	 * {@link #onRestoreInstanceState(Parcelable)} methods to propagate state
	 * across configuration changes.
	 * 
	 * @author David R. Bild
	 * @param <T>
	 */
	public interface DelegateInterface<T> {

		/**
		 * Called to specify the {@link PreferenceManager} object that this
		 * delegate should use.
		 * 
		 * @param preferenceManager
		 */
		public void setPreferenceManager(PreferenceManager preferenceManager);

		/**
		 * Called to specify the {@link DelegateCallbacks} object that this
		 * delegate should use.
		 * 
		 * @param callbacks the callback object to use
		 */
		public void setDelegateCallbacks(DelegateCallbacks<T> callbacks);

		/**
		 * Called to specify the request code this delegate should use when
		 * calling {@link DelegateCallbacks#startActivityForResult(Intent, int)}
		 * 
		 * @param requestCode the request code returned by
		 *            {@link #onActivityResult(int, int, Intent)}.
		 */
		public void setRequestCode(int requestCode);

		/**
		 * Gets the string used to identify this delegate to the user. If more
		 * than one delegate is registered with the {@code DelegatedPreference},
		 * the user will be displayed a list of delegates to choose from. The
		 * returned string will be used to identify this delegate.
		 * 
		 * @return the string identifying this delegate
		 */
		public String getSelectionPrompt();

		/**
		 * Called when the delegate should begin interaction with the user.
		 */
		public void onDelegate();

		/**
		 * Called if the delegate should cancel loading of the preference value.
		 * This method will only be called after a call to
		 * {@link DelegateCallbacks#onSelected()} but before a call to
		 * {@link DelegateCallbacks#onResult(Object)}. {@code onResult(Object)}
		 * should not be called if {@code onCancel()} was called.
		 */
		public void onCancel();

		/**
		 * Called when an {@link Activity} returns a result. The delegate is
		 * responsible for the checking the request code.
		 * 
		 * @see Activity#onActivityResult(int, int, Intent)
		 * @return {@code true} if this delegate handled the result and
		 *         {@code false} otherwise.
		 */
		public boolean onActivityResult(int requestCode, int resultCode, Intent data);

		/**
		 * Hook allowing a delegate to generate a representation of its state
		 * that can later be used to create an instance with the same state.
		 * 
		 * @see {@link #onRestoreInstanceState(Parcelable)}
		 * @see {@link Preference#onSaveInstanceState()}
		 * @return a {@code Parcelable} object containing the current state of
		 *         this delegate, or {@code null} if there is nothing to save.
		 */
		public Parcelable onSaveInstanceState();

		/**
		 * Hook allowing a delegate to re-apply a representation of its internal
		 * state that had previously been generated by
		 * {@link #onSaveInstanceState()}. This method will never be called with
		 * a {@code null} state.
		 * 
		 * @see {@link #onSaveInstanceState()}
		 * @see {@link Preference#onRestoreInstanceState(Parcelable)}
		 * @param state the saved state that had previously been returned by
		 *            {@link #onSaveInstanceState()}.
		 */
		public void onRestoreInstanceState(Parcelable parcelable);

	}

	/**
	 * Callback methods for a {@link DelegateInterface} to interact with a
	 * {@link DelegatedPreference}. See {@link DelegateInterface} for a
	 * description of a typical usage scenario and flow.
	 * 
	 * @see DelegateInterface
	 * @param <T> the type of the preference value
	 */
	public interface DelegateCallbacks<T> {

		/**
		 * Call this method to start an {@link Activity}. The result will be
		 * made available via the
		 * {@link DelegateInterface#onActivityResult(int, int, Intent)} method.
		 * See {@link Activity#startActivityForResult(Intent, int)} for full
		 * documentation of the parameters and semantics.
		 * 
		 * @param intent the intent specifying the {@code Activity} to start
		 * @param requestCode the code returned in
		 *            {@link DelegateInterface#onActivityResult(int, int, Intent)}
		 *            .
		 */
		public void startActivityForResult(Intent intent, int requestCode);

		/**
		 * Call this method when the user has finished interacting with the
		 * delegate and selected a new value for the preference. The new value
		 * does not need to be available yet. When available, it should be
		 * returned via the {@link #onResult(Object)} callback.
		 * <p>
		 * For example, consider a user selecting an image from a set of
		 * thumbnails. {@code #onSelected()} should be called when the image is
		 * selected and the delegate UI closes. The full size image can be
		 * retrieved via a background thread and when available, returned via
		 * {@code #onResult}.
		 * 
		 * @see #onResult(Object)
		 */
		public void onSelected();

		/**
		 * Call this method when the chosen preference value is available.
		 * 
		 * @param result the chosen preference value
		 */
		public void onResult(T result);

		/**
		 * Call this method if the user cancels the preference selection,
		 * instead of selecting a value. This method should only be called
		 * before {@link #onSelected()} is called. If an error occurs after
		 * selection while retrieving the full value, use
		 * {@link #onError(String)} instead.
		 */
		public void onCancel();

		/**
		 * Call this method if an error occurs. The given message will be
		 * displayed to the user. This method may be called before or after
		 * {@link #onSelected()} is called.
		 * 
		 * @param message the error message to display to the user
		 */
		public void onError(String message);

	}

}
