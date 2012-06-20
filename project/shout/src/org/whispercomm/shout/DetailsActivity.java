
package org.whispercomm.shout;

import org.whispercomm.shout.ShoutActivity.ViewHolder;
import org.whispercomm.shout.provider.ShoutProviderContract;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsActivity extends ListActivity {

	private static final String TAG = "DetailsActivity";
	public static final String SHOUT_ID = "shout_id";
	private Shout shout;
	
	private Cursor cursor;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		//get shout id from intent
		//shoutprovidercontract with id
		int shoutId = 1; // FIXME
		cursor = ShoutProviderContract.getCursorOverShoutComments(getApplicationContext(), shoutId);
		setListAdapter(new CommentsAdapter(this, cursor));
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
	}

	private class CommentsAdapter extends CursorAdapter {

		public CommentsAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			/*
			 * Set the text in the relevant view fields.
			 */
			// Get the shout
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
			holder.origSender = (TextView) rowView.findViewById(R.id.origsender);
			holder.age = (TextView) rowView.findViewById(R.id.age);
			holder.message = (TextView) rowView.findViewById(R.id.message);
			rowView.setTag(holder);
			Log.v(TAG, "View inflated");
			return rowView;
		}
	}
}
