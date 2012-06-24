package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ShoutListViewRow extends LinearLayout {

	private ActionShoutView actionShoutView;
	private LinearLayout border;
	private boolean expanded;

	public ShoutListViewRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutlistview_row, this);
		initialize();
	}

	public ShoutListViewRow(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.shoutlistview_row, this);
		initialize();
	}

	private void initialize() {
		this.setLayoutParams(new ListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.setOrientation(VERTICAL);
		this.setBackgroundColor(Color.TRANSPARENT);

		actionShoutView = (ActionShoutView) findViewById(R.id.actionshoutview);
		border = (LinearLayout) findViewById(R.id.border);

		expanded = false;
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleExpanded();
			}
		});
	}

	/**
	 * Expands the display of the shout.
	 * 
	 * The shout is offset by margins in the listview and the children views are
	 * expanded (e.g., the action bar and comments are shown).
	 * 
	 * @param expanded
	 *            {@code true} if the view should be expanded; {@code false}
	 *            otherwise.
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		setMargins(expanded);
		actionShoutView.setBarVisibility(expanded);
	}

	public void toggleExpanded() {
		setExpanded(!expanded);
	}

	private void setMargins(boolean expanded) {
		int spacing = expanded ? (int) (5 * ShoutListViewRow.this.getContext()
				.getResources().getDisplayMetrics().density + 0.5f) : 0;
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) border
				.getLayoutParams();
		params.bottomMargin = spacing;
		params.topMargin = spacing;
		border.setLayoutParams(params);
	}

}
