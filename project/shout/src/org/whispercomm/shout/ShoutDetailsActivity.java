
package org.whispercomm.shout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public class ShoutDetailsActivity extends Activity {

	public static final String TAG = ShoutDetailsActivity.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v(TAG, "Options button clicked");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.shout_menu, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy called");
	}

}
