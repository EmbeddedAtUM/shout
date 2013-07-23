
package org.whispercomm.shout.ui.widget;

import org.whispercomm.shout.DeletedShout;
import org.whispercomm.shout.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

/**
 * This is
 * 
 * @author Bowen Xu
 */

public class UndoBarController {

	private static final String TAG = UndoBarController.class.getSimpleName();

	private static View mUndoBarView;
	private static View mUndoButton;
	private static UndoListener mUndoLisener;
	private static SaveDeletedShoutListener sdsl;
	private static Parcelable mUndoToken;
	private static Handler mHideHandler = new Handler();

	private static Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hideUndoBarView(IMMEDIATE_HIDE);
		}
	};

	private static DeletedShout ds = null;
	private static int undoBarHide = 0;

	private static final int DELAY_TIME = 4000; // 4 second.
	public static final int IMMEDIATE_HIDE = 0;
	public static final int DELAY_HIDE = 1;
	public static final int NOT_HIDE = 2;
	public static final int SCROLL_HIDE = 3;

	public interface UndoListener {
		void onUndo(int id);
	}

	public interface SaveDeletedShoutListener {
		void saveDeleted(DeletedShout ds);
	}

	public static void with(View undoBarView) {

		mUndoBarView = undoBarView;
		if (mUndoBarView != null)
			mUndoButton = mUndoBarView.findViewById(R.id.undoBar_button);
	}

	public static void listenedBy(UndoListener undoListener,
			SaveDeletedShoutListener SDSL) {
		mUndoLisener = undoListener;
		sdsl = SDSL;
		if (mUndoButton != null) {
			mUndoButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mUndoLisener.onUndo(ds.getId());

				}
			});
		}
	}

	public static void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("undo_token", mUndoToken);
	}

	public static void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mUndoToken = savedInstanceState.getParcelable("undo_token");
		}
	}

	public static View getUndoBarView() {
		return mUndoBarView;
	}

	public static void hideUndoBarView(int hideStatus) {
		undoBarHide = hideStatus;

		if (mUndoBarView == null) {
			Log.e(TAG, "Failed to change undobar's visibility. Undo bar is null");
			return;
		}
		switch (hideStatus) {
			case IMMEDIATE_HIDE:
				mUndoBarView.setVisibility(View.GONE);
				break;
			case NOT_HIDE:
				mUndoBarView.setVisibility(View.VISIBLE);
				break;
			case DELAY_HIDE:
				mUndoBarView.setVisibility(View.VISIBLE);
				mHideHandler.removeCallbacks(mHideRunnable);
				mHideHandler.postDelayed(mHideRunnable, DELAY_TIME);
				break;
		}
	}

	public static boolean saveDeleteResult(DeletedShout result) {
		ds = result;
		if (ds == null || ds.getId() == -1)
			return false;
		sdsl.saveDeleted(ds);
		return true;
	}

	public static DeletedShout getDeletedShout() {
		return ds;
	}

	public static int getUndoBarStatus() {
		return undoBarHide;
	}

}
