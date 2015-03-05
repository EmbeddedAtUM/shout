/* 
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2012 The Regents of the University of Michigan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.whispercomm.android.preference.delegate;

import org.whispercomm.android.preference.DelegatedPreference.DelegateCallbacks;
import org.whispercomm.android.preference.DelegatedPreference.DelegateInterface;
import org.whispercomm.shout.R;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * A {@link DelegateInterface} that takes string input.
 * <p>
 * It is a subclass of {@link DialogDelegate} and show the {@link EditText} in a
 * dialog. Subclasses should implement the {@link #onPositive(CharSequence)}
 * method to receive the text when the dialog is closed with a positive button
 * click.
 * 
 * @author David R. Bild
 * @param <T> the type of the preference value
 */
public abstract class EditTextDelegate<T> extends DialogDelegate<T> {
	/*
	 * The code is mostly copied from {@link EditTextPreference}, with some
	 * slight modifications.
	 */

	/**
	 * The edit text shown in the dialog.
	 */
	private EditText mEditText;

	private String mText;

	/**
	 * Creates a new {@code EditTextDelegate} using the default layout.
	 * 
	 * @param context the context to associate with the dialog
	 */
	public EditTextDelegate(Context context) {
		this(context, R.layout.delegate_dialog_edittext);
	}

	/**
	 * Creates a new EditTextDelegate using the specified layout.
	 * 
	 * @param context the context to associate with the dialog
	 * @param dialogLayoutResourceId the layout resource to use for the dialog
	 */
	public EditTextDelegate(Context context, int dialogLayoutResourceId) {
		super(context, dialogLayoutResourceId);
		mEditText = new EditText(context); // TODO: add back attrs

		// Give it an ID so it can be saved/restored
		mEditText.setId(android.R.id.edit);
		mEditText.setEnabled(true);
		mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
	}

	/**
	 * Called when the user has closed the dialog with a positive result.
	 * <p>
	 * This method should call {@link DelegateCallbacks#onSelected()} and
	 * schedule {@link DelegateCallback#onResult(Object)}.
	 * 
	 * @param text the text entered by the user
	 */
	protected abstract void onPositive(CharSequence text);

	/**
	 * Called when the user has closed the dialog with a negative result.
	 * <p>
	 * The default implementation calls {@link DelegateCallbacks#onCancel()}.
	 */
	protected void onNegative() {
		getCallbacks().onCancel();
	}

	/**
	 * Sets the displayed text.
	 * 
	 * @param text The text to save
	 */
	public void setText(String text) {
		mText = text;
	}

	/**
	 * Gets the displayed text.
	 * 
	 * @return The currently displayed text
	 */
	public String getText() {
		return mText;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		EditText editText = mEditText;
		editText.setText(getText());

		ViewParent oldParent = editText.getParent();
		if (oldParent != view) {
			if (oldParent != null) {
				((ViewGroup) oldParent).removeView(editText);
			}
			onAddEditTextToDialogView(view, editText);
		}
	}

	/**
	 * Adds the EditText widget of this preference to the dialog's view.
	 * 
	 * @param dialogView The dialog view.
	 */
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		ViewGroup container = (ViewGroup) dialogView
				.findViewById(R.id.edittext_container);
		if (container != null) {
			container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		setText(mEditText.getText().toString());

		if (positiveResult) {
			this.onPositive(mEditText.getText());
		} else {
			this.onNegative();
		}
	}

	@Override
	protected boolean needInputMethod() {
		// We want the input method to show, if possible, when dialog is
		// displayed
		return true;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();

		final SavedState myState = new SavedState(superState);
		myState.text = getText();
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
		setText(myState.text);
	}

	private static class SavedState extends BaseSavedState {
		String text;

		public SavedState(Parcel source) {
			super(source);
			text = source.readString();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(text);
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

}
