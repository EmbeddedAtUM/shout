
package org.whispercomm.shout.ui;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ui.fragment.ContentFragment;
import org.whispercomm.shout.ui.fragment.ContentFragment.ContentListener;
import org.whispercomm.shout.util.ShoutUriUtils;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

public class ContentActivity extends AbstractShoutActivity implements ContentListener {
	private static final String TAG = MessageActivity.class.getSimpleName();

	private Hash hash;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_content);

		Uri uri = getIntent().getData();

		try {
			// Retrieve hash from URI
			hash = ShoutUriUtils.parseUri(uri);

			// Display content fragment
			FragmentTransaction ft =
					getSupportFragmentManager().beginTransaction();
			ft.add(R.id.main, ContentFragment.newInstance(hash));
			ft.commit();
		} catch (IllegalArgumentException e) {
			Log.i(TAG, String.format("Invalid shout uri: %s", getIntent().getData()), e);
			Toast.makeText(this, "Not a valid Shout uri.", Toast.LENGTH_LONG).show();
			finish();
		}

	}

	@Override
	public void displayContent(SherlockFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.main, fragment);
		ft.commit();
	}

}
