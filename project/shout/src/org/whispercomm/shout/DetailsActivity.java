package org.whispercomm.shout;

import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.util.ShoutMessageUtility;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

// TODO Make this page not look terrible
public class DetailsActivity extends ListActivity {

	private static final String TAG = "DetailsActivity";
	public static final String SHOUT_ID = "shout_id";
	private Shout shout;
	private int shoutId;

	private Cursor cursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		Bundle extras = getIntent().getExtras();
		shoutId = extras.getInt(SHOUT_ID);
		shout = ShoutProviderContract.retrieveShoutById(
				getApplicationContext(), shoutId);
		ShoutType type = ShoutMessageUtility.getShoutType(shout);
		switch (type) {
		case RESHOUT:
			int parentId = ShoutProviderContract.storeShout(
					getApplicationContext(), shout.getParent());
			cursor = ShoutProviderContract.getCursorOverShoutComments(
					getApplicationContext(), parentId);
			break;
		default:
			cursor = ShoutProviderContract.getCursorOverShoutComments(
					getApplicationContext(), shoutId);
			break;
		}
		setListAdapter(new CommentsAdapter(this, cursor));
	}

	@Override
	public void onResume() {
		super.onResume();
		shout = ShoutProviderContract.retrieveShoutById(
				getApplicationContext(), shoutId);
		// Set message details
		TextView username = (TextView) findViewById(R.id.origsender);
		username.setText(shout.getSender().getUsername());
		TextView age = (TextView) findViewById(R.id.age);
		age.setText(ShoutMessageUtility.getDateTimeAge(shout.getTimestamp()));
		TextView message = (TextView) findViewById(R.id.message);
		message.setText(shout.getMessage());
		TextView signature = (TextView) findViewById(R.id.signature);
		signature.setText(Base64.encodeToString(shout.getSignature(),
				Base64.DEFAULT));
		TextView hash = (TextView) findViewById(R.id.hash);
		hash.setText(Base64.encodeToString(shout.getHash(), Base64.DEFAULT));
		/*
		 * TODO 1) Figure out how to get access to the views that show the
		 * information about the Shout we're viewing the details of. 2) Populate
		 * those fields with content from this.shout
		 */
		Log.v(TAG, "Finished onResume");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cursor.close();
		Log.v(TAG, "Finished onDestroy");
	}

	static class DetailsViewHolder {
		TextView origSender;
		TextView message;
		TextView age;
		int id = -1;
	}

	private class CommentsAdapter extends CursorAdapter {

		public CommentsAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Get the shout
			LocalShout comment = ShoutProviderContract.retrieveShoutFromCursor(
					getApplicationContext(), cursor);

			// Set the view content
			DetailsViewHolder holder = (DetailsViewHolder) view.getTag();
			holder.id = comment.getDatabaseId();
			holder.age.setText(ShoutMessageUtility.getDateTimeAge(comment
					.getTimestamp()));
			holder.message.setText(comment.getMessage());
			holder.origSender.setText(comment.getSender().getUsername());
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Inflate the relevant views, and return the root view for the row.
			// These view come from comment.xml
			// You may want to make a class similar to ShoutActivity.ViewHolder
			// to do this.
			LayoutInflater inflater = LayoutInflater.from(context);
			View rowView = inflater.inflate(R.layout.comment, parent, false);
			DetailsViewHolder holder = new DetailsViewHolder();
			holder.origSender = (TextView) rowView
					.findViewById(R.id.commentingUser);
			holder.age = (TextView) rowView.findViewById(R.id.age);
			holder.message = (TextView) rowView.findViewById(R.id.commentText);
			rowView.setTag(holder);
			Log.v(TAG, "View inflated");
			return rowView;
		}
	}
}
