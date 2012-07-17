
package org.whispercomm.shout.customwidgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
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

	// Listeners called when the expabnded state is toggled
	private List<ExpandedStateChangeListener> expandedStateChangeListeners;

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

		expandedStateChangeListeners = Collections
				.synchronizedList(new ArrayList<ExpandedStateChangeListener>());

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
	 * Expands the display of the shout. The shout is offset by margins in the
	 * listview.
	 * 
	 * @param expanded {@code true} if the view should be expanded;
	 *            {@code false} otherwise.
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		setMargins(expanded);
		commentsView.setVisibility(expanded ? VISIBLE : GONE);

		for (ExpandedStateChangeListener l : expandedStateChangeListeners) {
			l.stateChanged(this.expanded);
		}
	}

	public void toggleExpanded() {
		setExpanded(!expanded);
	}

	/**
	 * Sets the Shout to be displayed by the view.
	 * 
	 * @param shout the Shout to be displayed
	 * @param expanded {@code true} if the view should be expanded.
	 */
	public void bindShout(LocalShout shout, boolean expanded) {
		commentsView.bindShouts(shout);
		actionShoutView.bindShout(shout, expanded);
		setExpanded(expanded);
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

	/**
	 * Registers the listener to be called when the expanded state changes.
	 * <p>
	 * Each listener is called as many times as it was registered. But you
	 * probably don't actually want to register a listener more than once.
	 * 
	 * @param l the listener to register
	 */
	public void registerExpandedStateChangeListener(
			ExpandedStateChangeListener l) {
		this.expandedStateChangeListeners.add(l);
	}

	/**
	 * Unregisters the listener, if it was registered. If the listener was
	 * registered more than once, only a single registration is removed.
	 * 
	 * @param l the listener to unregister
	 * @return {@code true} if the listener was unregistered, {@code false} is
	 *         the listener was not already registered.
	 */
	public boolean unregisterExpandedStateChangeListener(
			ExpandedStateChangeListener l) {
		return this.expandedStateChangeListeners.remove(l);
	}

	/**
	 * Unregisters all the registered listeners.
	 */
	public void clearExpandedStateChangeListeners() {
		this.expandedStateChangeListeners.clear();
	}

	/**
	 * Callback class called when the expanded state changes.
	 * 
	 * @author David R. Bild
	 */
	public static interface ExpandedStateChangeListener {
		/**
		 * @param expanded {@code true} if the row is expanded, {@code false}
		 *            otherwise.
		 */
		public void stateChanged(boolean expanded);
	}

}
