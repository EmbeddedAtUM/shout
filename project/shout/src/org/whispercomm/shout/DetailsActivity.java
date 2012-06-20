
package org.whispercomm.shout;

import org.whispercomm.shout.provider.ShoutProviderContract;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class DetailsActivity extends ListActivity {

	public static final String SHOUT_ID = "shout_id";
	
	private Cursor cursor;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		int shoutId = 1; // FIXME
		cursor = ShoutProviderContract.getCursorOverShoutComments(getApplicationContext(), shoutId);
		setListAdapter(new CommentsAdapter(this, cursor));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cursor.close();
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
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Inflate the relevant views, and return the root view for the row.
			// These view come from comment.xml
			// You may want to make a class similar to ShoutActivity.ViewHolder
			// to do this.
			return null;
		}
	}
}
