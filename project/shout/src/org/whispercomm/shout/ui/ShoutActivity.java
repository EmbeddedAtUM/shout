
package org.whispercomm.shout.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.provider.ParcelableShout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.tutorial.TutorialActivity;
import org.whispercomm.shout.tutorial.TutorialManager;
import org.whispercomm.shout.ui.widget.TimelineAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * The main activity for Shout. The activity displays a list of all received
 * shouts and provides interfaces for shouting, reshouting, and commenting.
 * 
 * @author David R. Bild
 */
public class ShoutActivity extends AbstractShoutViewActivity {

	private static final String BUNDLE_KEY = "parceled_shouts";

	private Cursor cursor;

	private Set<LocalShout> expandedShouts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		uninitialize();
		super.onDestroy();
	}

	protected void initialize() {
		super.initialize();

		TutorialManager.showHelp(this);

		setContentView(R.layout.shout_activity);

		this.cursor = ShoutProviderContract
				.getCursorOverAllShouts(getApplicationContext());
		this.expandedShouts = new HashSet<LocalShout>();

		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setEmptyView(findViewById(android.R.id.empty));
		listView.setAdapter(new TimelineAdapter(this, cursor, expandedShouts));
	}

	private void uninitialize() {
		if (this.cursor != null) {
			this.cursor.close();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (expandedShouts != null) {
			ArrayList<ParcelableShout> parceled = new ArrayList<ParcelableShout>();
			for (LocalShout shout : expandedShouts) {
				parceled.add(new ParcelableShout(shout));
			}
			outState.putParcelableArrayList(BUNDLE_KEY, parceled);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle saveInstanceState) {
		ArrayList<ParcelableShout> parceled = saveInstanceState.getParcelableArrayList(BUNDLE_KEY);
		for (ParcelableShout parceledShout : parceled) {
			expandedShouts.add(parceledShout.getShout(this));
		}

		super.onRestoreInstanceState(saveInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.settings:
				intent = new Intent(this, SettingsActivity.class);
				break;
			case R.id.compose:
				intent = new Intent(this, MessageActivity.class);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		startActivity(intent);
		return true;
	}

	public void onClickShout(View v) {
		MessageActivity.shout(this);
	}

	public void onClickSettings(View v) {
		SettingsActivity.show(this);
	}

	public void onClickHelp(View v) {
		TutorialActivity.show(this);
	}
}
