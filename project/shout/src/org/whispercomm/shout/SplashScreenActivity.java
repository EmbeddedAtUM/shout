
package org.whispercomm.shout;

import org.whispercomm.shout.network.NetworkInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
	}

	@Override
	public void onStart() {
		super.onStart();

		Thread loader = new Thread() {
			@SuppressWarnings("unused")
			// Force constructor calls
			@Override
			public void run() {
				Context context = getApplicationContext();
				NetworkInterface networkIf = NetworkInterface.getInstance(context);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Do nothing
				}
				SplashScreenActivity.this.runOnUiThread(endSplash);
			}
		};
		loader.start();
	}

	private Runnable endSplash = new Runnable() {
		@Override
		public void run() {
			finish();
			Intent mainIntent = new Intent(SplashScreenActivity.this, ShoutActivity.class);
			startActivity(mainIntent);
		}
	};
}
