
package org.whispercomm.shout.ui;

import org.whispercomm.shout.R;
import org.whispercomm.shout.tutorial.TutorialManager;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

/**
 * The main activity for Shout. The activity displays a list of all received
 * shouts and provides interfaces for shouting, reshouting, and commenting.
 * 
 * @author David R. Bild
 */
public class ShoutActivity extends AbstractShoutViewActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void initialize() {
		super.initialize();
		TutorialManager.showHelp(this);
		setContentView(R.layout.shout_activity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
}
