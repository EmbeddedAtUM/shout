
package org.whispercomm.shout.tutorial;

import org.whispercomm.shout.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class TutorialActivity extends Activity implements OnTouchListener {

	// Initializing variables
	Float downXValue;
	int counter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial);

		LinearLayout layMain = (LinearLayout) findViewById(R.id.layout_main);
		layMain.setOnTouchListener((OnTouchListener) this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return false;
	}

	public static void show(Context context)
	{
		Intent intent = new Intent(context, TutorialActivity.class);
		context.startActivity(intent);
	}

	public void continueToShout(View view)
	{
		finish();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// Get the action that was done on this touch event
		switch (arg1.getAction())
		{
			case MotionEvent.ACTION_DOWN: {
				// store the X coordinate value at which the user's finger was
				// pressed down
				downXValue = arg1.getX();
				break;
			}

			case MotionEvent.ACTION_UP: {

				// Get the X value at which the user released his finger
				float currentX = arg1.getX();

				// left to right navigation
				if (downXValue < currentX)
				{

					ViewFlipper vf = (ViewFlipper) findViewById(R.id.details);

					// animation while navigation
					vf.setOutAnimation(getApplicationContext(), R.anim.push_left_out);
					vf.setInAnimation(getApplicationContext(), R.anim.push_right_out);

					// Prevent looping of the images
					counter = vf.getDisplayedChild();
					if (counter > 0) {
						vf.showPrevious();
					}
				}

				// right to left navigation
				if (downXValue > currentX)
				{

					ViewFlipper vf = (ViewFlipper) findViewById(R.id.details);

					// animation while navigation
					vf.setOutAnimation(getApplicationContext(), R.anim.push_left_in);
					vf.setInAnimation(getApplicationContext(), R.anim.push_right_in);

					// Prevent looping of images
					counter = vf.getDisplayedChild();
					if (counter < 8) {
						vf.showNext();
					}
				}
			}
		}

		return true;
	}
}
