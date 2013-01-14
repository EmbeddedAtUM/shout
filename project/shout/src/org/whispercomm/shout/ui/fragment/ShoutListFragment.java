
package org.whispercomm.shout.ui.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.whispercomm.shout.LocalShout;
import org.whispercomm.shout.R;
import org.whispercomm.shout.provider.ParcelableShout;
import org.whispercomm.shout.provider.ShoutProviderContract;
import org.whispercomm.shout.tutorial.TutorialActivity;
import org.whispercomm.shout.ui.MessageActivity;
import org.whispercomm.shout.ui.SettingsActivity;
import org.whispercomm.shout.ui.widget.TimelineAdapter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ShoutListFragment extends Fragment {
	private Cursor cursor;

	private Set<LocalShout> expandedShouts;

	private static final String BUNDLE_KEY = "parceled_shouts";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.shout_fragment, container, false);

		this.cursor = ShoutProviderContract
				.getCursorOverAllShouts(getActivity());
		this.expandedShouts = new HashSet<LocalShout>();

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setEmptyView(view.findViewById(android.R.id.empty));
		listView.setAdapter(new TimelineAdapter(getActivity(), cursor, expandedShouts));

		return view;
	}

	@Override
	public void onDestroy() {
		uninitialize();
		super.onDestroy();
	}

	private void uninitialize() {
		if (this.cursor != null) {
			this.cursor.close();
		}
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

	public void onClickShout(View v) {
		MessageActivity.shout(getActivity());
	}

	public void onClickSettings(View v) {
		SettingsActivity.show(getActivity());
	}

	public void onClickHelp(View v) {
		TutorialActivity.show(getActivity());
	}
}
