
package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TableRow;
import android.widget.TextView;

public class ShoutDetailRow extends TableRow {

	TextView title;
	TextView entry;

	public ShoutDetailRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeViews(context);
	}

	public ShoutDetailRow(Context context) {
		super(context);
		initializeViews(context);
	}

	private void initializeViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.detail_row, this);
		title = (TextView) findViewById(R.id.rowTitle);
		entry = (TextView) findViewById(R.id.rowEntry);
	}

	public void setTitleText(String titleText) {
		title.setText(titleText);
	}

	public void setEntryText(String entryText) {
		entry.setText(entryText);
	}
}
