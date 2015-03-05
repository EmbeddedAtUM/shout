
package org.whispercomm.shout.ui.preference;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.whispercomm.android.preference.DelegatedPreference.DelegateCallbacks;
import org.whispercomm.android.preference.delegate.ActivityDelegate;
import org.whispercomm.shout.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Delegate to get an image from the local Gallery.
 * 
 * @author David R. Bild
 */
public class GalleryBitmapDelegate extends ActivityDelegate<Bitmap> {

	private String mSelectionPrompt;
	private String mRetrievalError;

	public GalleryBitmapDelegate(Context context) {
		super(context);
		mSelectionPrompt = getString(R.string.delegateprompt_Gallery);
		mRetrievalError = getString(R.string.delegategalleryerror);
	}

	@Override
	protected void onPrepareIntent(Intent intent) {
		intent.setAction(Intent.ACTION_PICK);
		intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == getRequestCode()) {
			DelegateCallbacks<Bitmap> callbacks = getCallbacks();
			if (resultCode == Activity.RESULT_OK && data != null) {
				callbacks.onSelected();
				try {
					Uri uri = data.getData();
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext()
							.getContentResolver(), uri);
					callbacks.onResult(bitmap);
				} catch (FileNotFoundException e) {
					callbacks.onError(mRetrievalError);
				} catch (IOException e) {
					callbacks.onError(mRetrievalError);
				}
				return true;
			}
			callbacks.onCancel();
			return true;
		}
		return false;
	}

	@Override
	public String getSelectionPrompt() {
		return mSelectionPrompt;
	}

	@Override
	public void onCancel() {
		// We call onResult() synchronously with onSelected(), so onCancel() can
		// never be called.
	}

}
