
package org.whispercomm.shout.ui.preference;

import org.whispercomm.android.preference.delegate.ActivityDelegate;
import org.whispercomm.shout.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

/**
 * Delegate to get an image from the camera. This returns only the
 * low-resolution preview from the camera, not the full resolution image.
 * 
 * @author David R. Bild
 */
public class CameraBitmapDelegate extends ActivityDelegate<Bitmap> {

	private String mSelectionPrompt = "Camera";

	public CameraBitmapDelegate(Context context) {
		super(context);
		mSelectionPrompt = getString(R.string.delegateprompt_Camera);
	}

	@Override
	protected void onPrepareIntent(Intent intent) {
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == getRequestCode()) {
			/*
			 * resultCode is broken on Galaxy Nexus and Nexus 7 JB 4.1.2. The
			 * value "-1" is returned even if the picture is taken. Instead, we
			 * just check for the returned data.
			 */
			if (data != null && data.hasExtra("data")) {
				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				getCallbacks().onSelected();
				getCallbacks().onResult(bitmap);
				return true;
			}
			getCallbacks().onCancel();
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
