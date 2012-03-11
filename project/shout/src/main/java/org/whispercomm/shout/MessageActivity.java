package org.whispercomm.shout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MessageActivity extends Activity {

	Toast successToast;
	Toast failedToast;
	public static final String TAG = "MessageActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.message);

		successToast = Toast.makeText(this, "Message Successfully Sent.",
				Toast.LENGTH_SHORT);

		failedToast = Toast.makeText(this, "An error occured, please try again later.",
				Toast.LENGTH_SHORT);
	}

	public void onClickSend(View v) {
		Log.v(TAG, "Send button clicked");

		startActivity(new Intent(this, ShoutActivity.class));

		successToast.show();
	}
}
