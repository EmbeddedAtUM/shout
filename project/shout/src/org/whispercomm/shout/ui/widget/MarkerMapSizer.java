
package org.whispercomm.shout.ui.widget;

import java.util.ArrayList;
import java.util.HashSet;

import android.database.DataSetObserver;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

/**
 * A class that sizes a {@link GoogleMap} to show a set of {@code Marker}s. The
 * Markers can be added manually or taken from a {@link MarkMapLayer}. It also
 * enforces a minimum size for the map.
 * <p>
 * NB: This code is largely a complete hack, but functional for our immediate
 * purposes.
 */
public class MarkerMapSizer {

	private final ArrayList<MarkerMapLayer> mMarkerLayers = new ArrayList<MarkerMapLayer>();

	private final HashSet<Marker> mMarkers = new HashSet<Marker>();

	private final LayerDataSetObserver mObserver = new LayerDataSetObserver();

	private GoogleMap mMap;

	private boolean mLayoutComplete;

	private int mPadding = 100;

	private double minLength = 0.02; // degrees. ~ 2km

	public MarkerMapSizer(GoogleMap map) {
		this(map, true);
	}

	public MarkerMapSizer(GoogleMap map, boolean disableMarkerClick) {
		mMap = map;

		/* Disable default marker click handler, which recenters the map */
		if (disableMarkerClick) {
			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {
					return true;
				}
			});
		}

		mLayoutComplete = false;

		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				mMap.setOnCameraChangeListener(null);
				mLayoutComplete = true;
				resize(false);
			}
		});
	}

	public MarkerMapSizer addMarker(Marker marker) {
		mMarkers.add(marker);
		return this;
	}

	public MarkerMapSizer removeMarker(Marker marker) {
		mMarkers.remove(marker);
		return this;
	}

	public MarkerMapSizer addMarkerLayer(MarkerMapLayer markerLayer) {
		markerLayer.registerDataSetObserver(mObserver);
		mMarkerLayers.add(markerLayer);
		return this;
	}

	public MarkerMapSizer removeMarkerLayer(MarkerMapLayer markerLayer) {
		markerLayer.unregisterDataSetObserver(mObserver);
		mMarkerLayers.remove(markerLayer);
		return this;
	}

	private LatLngBounds getBounds() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		for (Marker marker : mMarkers) {
			builder.include(marker.getPosition());
		}

		for (MarkerMapLayer layer : mMarkerLayers) {
			LatLngBounds bounds = layer.getLatLngBounds();
			if (bounds != null) {
				builder.include(bounds.northeast);
				builder.include(bounds.southwest);
			}
		}

		LatLngBounds bounds;
		try {
			bounds = builder.build();
		} catch (IllegalStateException e) {
			return null;
		}

		/* Increase bound to be at least minimum size */
		/* TODO: fix this math. It breaks in other hemispheres. */

		LatLng ne = bounds.northeast;
		LatLng sw = bounds.southwest;

		double swLat;
		double swLng;
		double neLat;
		double neLng;

		// Calculate new width
		double width = Math.abs(sw.longitude - ne.longitude);
		if (width < minLength) {
			double inc = (minLength - width) / 2;
			swLng = sw.longitude - inc;
			neLng = ne.longitude + inc;
		} else {
			swLng = sw.longitude;
			neLng = ne.longitude;
		}

		// Calculate new height
		double height = Math.abs(ne.latitude - sw.latitude);
		if (height < minLength) {
			double inc = (minLength - height) / 2;
			neLat = ne.latitude + inc;
			swLat = sw.latitude - inc;
		} else {
			neLat = ne.latitude;
			swLat = sw.latitude;
		}

		ne = new LatLng(neLat, neLng);
		sw = new LatLng(swLat, swLng);

		return new LatLngBounds.Builder().include(sw)
				.include(ne).build();
	}

	public void resize(boolean animate) {
		if (mLayoutComplete) {
			LatLngBounds bounds = getBounds();
			if (bounds != null) {
				CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, mPadding);
				if (animate)
					mMap.animateCamera(update);
				else
					mMap.moveCamera(update);
			} else {

			}
		}
	}

	class LayerDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			resize(true);
		}

		@Override
		public void onInvalidated() {
			// Ignore
		}

	}

}
