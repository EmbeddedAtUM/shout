
package org.whispercomm.shout.ui.widget;

import java.util.HashSet;
import java.util.Set;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A class for adding {@link Marker}s to a {@link GoogleMap} from an
 * {@link ItemAdapter}.
 * <p>
 * After construction, call {@link #setAdapter(ItemAdapter)} to specify the
 * source of the {@link MarkerOptions}.
 */
public class MarkerMapLayer {

	private final DataSetObservable mDataSetObservable = new DataSetObservable();

	private final Set<Marker> mMarkers = new HashSet<Marker>();

	private final GoogleMap mMap;

	private ItemAdapter<MarkerOptions> mAdapter;
	private DataSetObserver mDataSetObserver;

	private int mItemCount;

	private LatLngBounds mLatLngBounds;

	public MarkerMapLayer(GoogleMap map) {
		mMap = map;
	}

	/**
	 * Set the adapter providing the {@link MarkerOptions} to render on the map.
	 * 
	 * @param adapter the adapter providing the {@link MarkerOptions} to render
	 *            on the map.
	 */
	public void setAdapter(ItemAdapter<MarkerOptions> adapter) {
		if (mAdapter != null && mDataSetObserver != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		mAdapter = adapter;

		if (mAdapter != null) {
			mItemCount = mAdapter.getCount();

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);
		}

		fillMarkers();
	}

	/**
	 * @return the adapter providing the {@link MarkerOptions} to render on the
	 *         map
	 */
	public ItemAdapter<MarkerOptions> getAdapter() {
		return mAdapter;
	}

	/**
	 * @return the number of markers in this layer
	 */
	public int getMarkerCount() {
		return mMarkers.size();
	}

	/**
	 * @return the bounds of the markers in this map or {@code null} if no
	 *         markers
	 */
	public LatLngBounds getLatLngBounds() {
		return mLatLngBounds;
	}

	/**
	 * Register an observer that is called when the set of displayed markers
	 * changes.
	 * 
	 * @param observer the object that gets notified when the markers change
	 */
	public void registerDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.registerObserver(observer);
	}

	/**
	 * Unregister an observer that has previously been registered with this
	 * adapter via {@link #registerDataSetObserver(DataSetObserver)}.
	 * 
	 * @param observer the object to unregister
	 */
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.unregisterObserver(observer);
	}

	private void clearMarkers() {
		for (Marker marker : mMarkers) {
			marker.remove();
		}
		mMarkers.clear();
		mLatLngBounds = null;
	}

	private void fillMarkers() {
		/*
		 * This method clears all markers and recreates everything from the
		 * underlying adapter. One could optimize by not removing existing
		 * items.
		 */
		clearMarkers();

		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		for (int i = 0; i < mItemCount; i++) {
			MarkerOptions markerOptions = mAdapter.get(i);
			if (markerOptions != null) {
				Marker marker = mMap.addMarker(markerOptions);
				mMarkers.add(marker);
				builder.include(marker.getPosition());
			}
		}

		try {
			mLatLngBounds = builder.build();
		} catch (IllegalStateException e) {
			// Ignore. Thrown if no points in builder.
		}

		mDataSetObservable.notifyChanged();
	}

	class AdapterDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			mItemCount = getAdapter().getCount();
			fillMarkers();
		}

		@Override
		public void onInvalidated() {
			// Data is invalid so we should reset our state
			mItemCount = 0;
			fillMarkers();
		}

	}

}
