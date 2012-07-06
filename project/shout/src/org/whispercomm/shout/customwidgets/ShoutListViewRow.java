package org.whispercomm.shout.customwidgets;

import org.whispercomm.shout.R;
import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.util.Conversions;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ShoutListViewRow extends LinearLayout {

	private static final int EXPANDED_MARGIN_DP = 5;

	private LinearLayout border;
	private ActionShoutView actionShoutView;
	private ShoutChainView commentsView;
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
		commentsView = (ShoutChainView) findViewById(R.id.commentsview);

		expanded = false;
		actionShoutView
				.registerActionBarStateChangeListener(new ActionShoutView.ActionBarStateChangeListener() {
					@Override
					public void stateChanged(boolean visibility) {
						setExpanded(visibility);
					}
				});
	}

	/**
	 * Expands the display of the shout.
	 * 
	 * The shout is offset by margins in the listview.
	 * 
	 * @param expanded
	 *            {@code true} if the view should be expanded; {@code false}
	 *            otherwise.
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		setMargins(expanded);
		commentsView.setVisibility(expanded ? VISIBLE : GONE);
	}

	public void toggleExpanded() {
		setExpanded(!expanded);
	}

	/**
	 * Sets the Shout to be displayed by the view.
	 * 
	 * @param shout
	 *            the Shout to be displayed
	 */
	public void bindShout(LocalShout shout) {
		setExpanded(false);

		actionShoutView.bindShout(shout);
		commentsView.bindShouts(shout);
	}

	private void setMargins(boolean expanded) {
		int spacing = expanded ? Conversions.dpToPx(EXPANDED_MARGIN_DP,
				getContext().getResources()) : 0;
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) border
				.getLayoutParams();
		params.bottomMargin = spacing;
		params.topMargin = spacing;
		border.setLayoutParams(params);
	}

}
