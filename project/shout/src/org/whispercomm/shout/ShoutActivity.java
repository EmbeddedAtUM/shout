
package org.whispercomm.shout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.whispercomm.manes.client.maclib.ManesActivityHelper;
import org.whispercomm.manes.client.maclib.ManesNotInstalledException;
import org.whispercomm.manes.client.maclib.ManesNotRegisteredException;
import org.whispercomm.shout.customwidgets.DialogFactory;
import org.whispercomm.shout.customwidgets.ShoutListViewRow;
import org.whispercomm.shout.id.IdManager;
import org.whispercomm.shout.id.UserNotInitiatedException;
import org.whispercomm.shout.network.BootReceiver;
import org.whispercomm.shout.network.NetworkInterface;
import org.whispercomm.shout.network.NetworkInterface.NotConnectedException;
import org.whispercomm.shout.network.NetworkInterface.ShoutServiceConnection;
import org.whispercomm.shout.network.NetworkService;
import org.whispercomm.shout.provider.ParcelableShout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.serialization.ShoutChainTooLongException;
import org.whispercomm.shout.tasks.AsyncTaskCallback.AsyncTaskCompleteListener;
import org.whispercomm.shout.tasks.ReshoutTask;
import org.whispercomm.shout.tasks.SendResult;
import org.whispercomm.shout.tasks.SendShoutTask;
import org.whispercomm.shout.terms.AgreementListener;
import org.whispercomm.shout.terms.AgreementManager;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Toast;

public class ShoutActivity extends ListActivity {
	private static final String TAG = "ShoutActivity";

	private static final String BUNDLE_KEY = "parceled_shouts";

	private NetworkInterface network;
	private IdManager idManager;
	private Cursor cursor;

	private Set<LocalShout> expandedShouts;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.v(TAG, "Finished onCreate");
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

	private void initialize() {
		startBackgroundService();

		this.network = new NetworkInterface(this, new ShoutServiceConnection() {
			@Override
			public void manesNotInstalled() {
				DialogFactory.buildInstallationPromptDialog(ShoutActivity.this,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ManesActivityHelper
										.launchManesInstallation(ShoutActivity.this);
								finish();
							}
						}).show();
			}

			@Override
			public void manesNotRegistered() {
				ManesActivityHelper
						.launchRegistrationActivity(ShoutActivity.this);
			}
		});

		this.idManager = new IdManager(this);
		this.cursor = ShoutProviderContract
				.getCursorOverAllShouts(getApplicationContext());
		this.expandedShouts = new HashSet<LocalShout>();

		setListAdapter(new TimelineAdapter(this, cursor));
	}

	private void uninitialize() {
		if (this.network != null) {
			this.network.unbind();
		}
		if (this.cursor != null) {
			this.cursor.close();
		}
	}

	/**
	 * Ensures that the background Shout service is started, if the user has
	 * that option enabled.
	 */
	private void startBackgroundService() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean runInBackground = prefs.getBoolean(
				BootReceiver.START_SERVICE_ON_BOOT, true);
		if (runInBackground) {
			Intent intent = new Intent(this, NetworkService.class);
			this.startService(intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "Finished onDestroy");
	}

	@Override
	protected void onStart() {
		super.onStart();
		AgreementListener listener = new AgreementListener() {

			@Override
			public void declined() {
				finish();
			}

			@Override
			public void accepted() {
				initialize();
			}
		};
		AgreementManager.getConsent(this, listener);
	}

	@Override
	protected void onStop() {
		uninitialize();
		super.onStop();
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
		Log.v(TAG, "Shout button clicked");
		startActivity(new Intent(this, MessageActivity.class));
	}

	public void onClickSettings(View v) {
		Log.v(TAG, "Settings button clicked");
		displaySettings();
	}

	public void onClickReshout(LocalShout shout) {
		Log.v(TAG, "Reshout button clicked");

		try {
			new ReshoutTask(getApplicationContext(),
					new ShoutCreationCompleteListener(), idManager.getMe(),
					shout).execute();
		} catch (UserNotInitiatedException e) {
			Toast.makeText(this, "Please set a username before shouting.",
					Toast.LENGTH_LONG).show();
			displaySettings();
		}
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
			DialogFactory.buildInstallationPromptDialog(this,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ManesActivityHelper.launchManesInstallation(ShoutActivity.this);
							finish();
						}
					}).show();
		} catch (ManesNotRegisteredException e) {
			DialogFactory.buildRegistrationPromptDialog(this,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ManesActivityHelper
									.launchRegistrationActivity(ShoutActivity.this);
						}
					}).show();
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

	private void displaySettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onClickComment(LocalShout shout) {
		Log.v(TAG, "Comment button clicked");

		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(MessageActivity.PARENT_ID, shout.getHash());
		startActivity(intent);
	}

	public void onClickDetails(LocalShout shout) {
		Log.v(TAG, "Details buttons clicked");

		Intent intent = new Intent(this, DetailsActivity.class);
		intent.putExtra(DetailsActivity.SHOUT_ID, shout.getHash());
		startActivity(intent);
	}

	private class TimelineAdapter extends CursorAdapter {

		public TimelineAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ShoutListViewRow row = (ShoutListViewRow) view;

			// Get the shout
			final LocalShout shout = ShoutProviderContract.retrieveShoutFromCursor(
					context, cursor);

			row.clearExpandedStateChangeListeners();
			row.registerExpandedStateChangeListener(new ShoutListViewRow.ExpandedStateChangeListener() {
				@Override
				public void stateChanged(boolean expanded) {
					if (expanded) {
						expandedShouts.add(shout);
					} else {
						expandedShouts.remove(shout);
					}
				}
			});

			Log.v(TAG, "Binding shout: " + shout);

			row.bindShout(shout, expandedShouts.contains(shout));
		}

		@Override
		public View newView(final Context context, Cursor cursor,
				ViewGroup parent) {
			return new ShoutListViewRow(context);
		}
	}
}
