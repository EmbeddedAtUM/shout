
package org.whispercomm.shout.ui.preference;

import java.io.IOException;

import org.whispercomm.android.preference.delegate.EditTextDelegate;
import org.whispercomm.shout.R;
import org.whispercomm.shout.twitter.TwitterUserData;
import org.whispercomm.shout.twitter.UnknownScreennameException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class TwitterProfileImageDelegate extends EditTextDelegate<Bitmap> {

	private String mSelectionPrompt;
	private DownloadTwitterAvatar mDownloadTask;
	private String mUsername;

	public TwitterProfileImageDelegate(Context context) {
		super(context);
		mSelectionPrompt = getString(R.string.delegateprompt_Twitter);
		this.setDialogTitle(R.string.delegatetwittertitle);
		this.setPositiveButtonText(android.R.string.ok);
		this.setNegativeButtonText(android.R.string.cancel);
	}

	@Override
	public String getSelectionPrompt() {
		return mSelectionPrompt;
	}

	@Override
	protected void onPositive(CharSequence text) {
		getCallbacks().onSelected();
		startDownload(text.toString());
	}

	private void startDownload(String username) {
		mUsername = username;
		mDownloadTask = new DownloadTwitterAvatar(mUsername);
		mDownloadTask.execute();
	}

	@Override
	public void onCancel() {
		if (mDownloadTask != null)
			mDownloadTask.cancel(true);
		mDownloadTask = null;
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		return false; // We do not use activities.
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (mDownloadTask == null)
			return superState;

		mDownloadTask.cancel(true);
		final SavedState myState = new SavedState(superState);
		myState.username = mUsername;
		return myState;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		startDownload(myState.username);
	}

	private static class SavedState extends BaseSavedState {
		String username;

		public SavedState(Parcel source) {
			super(source);
			username = source.readString();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(username);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		// Used by Android framework
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

	private class DownloadTwitterAvatar extends AsyncTask<Void, Integer, Bitmap> {
		private static final String TAG = "DownlaodTwitterAvatar";

		private String mUsername;
		private Throwable mError;

		public DownloadTwitterAvatar(String username) {
			mUsername = username;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			TwitterUserData userData;
			try {
				userData = new TwitterUserData(mUsername);
				byte[] mImage = userData.getProfileImage();
				return BitmapFactory.decodeByteArray(mImage, 0, mImage.length);
			} catch (UnknownScreennameException e) {
				mError = e;
				return null;
			} catch (IOException e) {
				mError = e;
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (!isCancelled()) {
				if (result != null)
					getCallbacks().onResult(result);
				else
					getCallbacks().onError(getErrorMessage());
				mDownloadTask = null;
			}
		}

		private String getErrorMessage() {
			String message;

			if (mError instanceof UnknownScreennameException) {
				message = String.format(
						getString(R.string.delegatetwittererror_invalidscreenname),
						mUsername);
			} else if (mError instanceof IOException) {
				message = getString(R.string.delegatetwittererror_networkconnection);
			} else {
				Log.e(TAG, "Unknown exception while downloading Twitter profile image.", mError);
				message = getString(R.string.delegatetwittererror_unknown);
			}

			return message;
		}

	}

}
