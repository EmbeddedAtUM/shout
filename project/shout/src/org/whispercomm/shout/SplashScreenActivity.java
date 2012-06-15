package org.whispercomm.shout;

import org.whispercomm.shout.network.NetworkInterface;

import android.app.Activity;
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
				NetworkInterface networkIf = NetworkInterface.getInstance(getApplicationContext());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Do nothing
				}
				SplashScreenActivity.this.endSplash();
			}
		};
		loader.start();
	}

	private void endSplash() {
		finish();
		Intent mainIntent = new Intent(this, ShoutActivity.class);
		startActivity(mainIntent);
	}
}
