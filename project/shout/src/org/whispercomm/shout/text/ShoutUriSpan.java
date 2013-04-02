
package org.whispercomm.shout.text;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.view.View;

public class ShoutUriSpan extends ClickableSpan {

	private Uri mUri;

	public ShoutUriSpan(String uri) {
		mUri = Uri.parse(uri);
	}

	public ShoutUriSpan(Uri uri) {
		mUri = uri;
	}

	@Override
	public void onClick(View widget) {
		Uri uri = mUri;
		Context context = widget.getContext();
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			new AlertDialog.Builder(widget.getContext())
					.setMessage(
							"Upgrade to the latest version of Shout via Google Play to view this image.")
					.setTitle("Please Update Shout").setPositiveButton("OK", null).create().show();
		}
	}

}
