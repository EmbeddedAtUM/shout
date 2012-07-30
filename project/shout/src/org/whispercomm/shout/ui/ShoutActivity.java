
package org.whispercomm.shout.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.NetworkInterface.NotConnectedException;
import org.whispercomm.shout.provider.ParcelableShout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.ReshoutTask;
import org.whispercomm.shout.tasks.SendResult;
import org.whispercomm.shout.tasks.SendShoutTask;
import org.whispercomm.shout.ui.widget.TimelineAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The main activity for Shout. The activity displays a list of all received
 * shouts and provides interfaces for shouting, reshouting, and commenting.
 * 
 * @author David R. Bild
 */
public class ShoutActivity extends AbstractShoutActivity {
	private static final String TAG = ShoutActivity.class.getSimpleName();

	private static final String BUNDLE_KEY = "parceled_shouts";

	private IdManager idManager;
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

		setContentView(R.layout.shout_activity);

		this.idManager = new IdManager(this);
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
		MenuInflater inflater = getMenuInflater();
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

	public void onClickReshout(LocalShout shout) {
		try {
			new ReshoutTask(getApplicationContext(),
					new ShoutCreationCompleteListener(), idManager.getMe(),
					shout).execute();
		} catch (UserNotInitiatedException e) {
			Toast.makeText(this, "Please set a username before shouting.",
					Toast.LENGTH_LONG).show();
			SettingsActivity.show(this);
		}
	}

	public void onClickComment(LocalShout shout) {
		MessageActivity.comment(this, shout);
	}

	public void onClickDetails(LocalShout shout) {
		DetailsActivity.show(this, shout);
	}

	private void shoutCreated(LocalShout result) {
		if (result != null) {
			new SendShoutTask(network, new ShoutSendCompleteListener())
					.execute(result);
		} else {
			Toast.makeText(this, R.string.create_shout_failure,
					Toast.LENGTH_LONG).show();
		}
	}

	private void shoutSent(SendResult result) {
		try {
			result.getResultOrThrow();
			Toast.makeText(this, R.string.send_shout_success, Toast.LENGTH_SHORT)
					.show();
		} catch (NotConnectedException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
		} catch (ShoutChainTooLongException e) {
			Log.e(TAG, "SHOUT_CHAIN_TOO_LONG error.  Unable to send shout.");
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
		} catch (ManesNotInstalledException e) {
			this.promptForInstallation();
		} catch (ManesNotRegisteredException e) {
			this.promptForRegistration();
		} catch (IOException e) {
			Toast.makeText(this, R.string.send_shout_failure, Toast.LENGTH_LONG)
					.show();
		}
	}

	private class ShoutCreationCompleteListener implements
			AsyncTaskCompleteListener<LocalShout> {
		@Override
		public void onComplete(LocalShout result) {
			shoutCreated(result);
		}
	}

	private class ShoutSendCompleteListener implements
			AsyncTaskCompleteListener<SendResult> {
		@Override
		public void onComplete(SendResult result) {
			shoutSent(result);
		}
	}
}
