
package org.whispercomm.shout.ui.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.provider.ParcelableShout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.provider.ShoutProviderContract.SortOrder;
import org.whispercomm.shout.ui.widget.TimelineAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ShoutListFragment extends SherlockFragment {

	private static final String BUNDLE_KEY = "parceled_shouts";

	private static final String SORT_ORDER_KEY = "sort_order";

	private Set<LocalShout> expandedShouts;
	private TimelineAdapter adapter;
	private SortOrder sortOrder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_shout_list, container, false);

		this.expandedShouts = new HashSet<LocalShout>();
		this.adapter = new TimelineAdapter(getActivity(), null, expandedShouts);

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setEmptyView(view.findViewById(android.R.id.empty));
		listView.setAdapter(this.adapter);

		if (savedInstanceState != null && savedInstanceState.containsKey(SORT_ORDER_KEY))
			setShoutOrder((SortOrder) savedInstanceState.getSerializable(SORT_ORDER_KEY));
		else
			setShoutOrder(SortOrder.ReceivedTimeDescending);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
		super.onCreateOptionsMenu(menu, inflator);
		inflator.inflate(R.menu.fragment_shoutlist, menu);
	}

	@Override
	public void onDestroy() {
		adapter.changeCursor(null); // to close cursor
		super.onDestroy();
	}

	/**
	 * Reloads all the shouts sorted in the given order.
	 * 
	 * @param order the order in which to sort the shouts
	 */
	private void setShoutOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
		this.adapter.changeCursor(ShoutProviderContract
				.getCursorOverAllShouts(getActivity(), sortOrder));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.menu_sort_received_time:
				setShoutOrder(SortOrder.ReceivedTimeDescending);
				break;
			case R.id.menu_sort_sent_time:
				setShoutOrder(SortOrder.SentTimeDescending);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
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

		outState.putSerializable(SORT_ORDER_KEY, sortOrder);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			ArrayList<ParcelableShout> parceled = savedInstanceState
					.getParcelableArrayList(BUNDLE_KEY);
			for (ParcelableShout parceledShout : parceled) {
				expandedShouts.add(parceledShout.getShout(getActivity()));
			}
		}

		super.onActivityCreated(savedInstanceState);
	}
}
