
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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ShoutListFragment extends Fragment {

	private static final String BUNDLE_KEY = "parceled_shouts";

	private Set<LocalShout> expandedShouts;
	private TimelineAdapter adapter;

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

		loadShouts(SortOrder.ReceivedTimeDescending);

		return view;
	}

	@Override
	public void onDestroy() {
		adapter.changeCursor(null); // to close cursor
		super.onDestroy();
	}

	private void loadShouts(SortOrder order) {
		this.adapter.changeCursor(ShoutProviderContract
				.getCursorOverAllShouts(getActivity(), order));
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
