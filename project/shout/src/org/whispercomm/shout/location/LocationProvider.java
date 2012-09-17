
package org.whispercomm.shout.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * A location provider designed for retrieving the current location to attach to
 * user-generated content. Locations are aggregated from all available
 * providers.
 * <p>
 * The following scheme is used. Upon constructor, listeners are registered for
 * the GPS, network, and passive providers and the last known location for each
 * is retrieved. If, after {@link #GPS_TIMEOUT_MS} milliseconds no GPS update is
 * received, the GPS listener is disabled on the assumption that the device is
 * out of GPS range. A provider is also disabled when it returns a result with
 * an accuracy of at least {@link #DESIRED_ACCURACY_M} meters (With the
 * exception of the passive provider, that has no cost to run).
 * <p>
 * The best location is saved, where "best" is determined by the
 * {@link #processUpdate(Location)} method.
 * <p>
 * One may call {@link #getLocation()} at any time to retrieve the current best
 * location. This may return {@code null}, if no estimate is available yet.
 * <p>
 * {@code #stop()} must be called in order to properly release all the
 * underlying location provider listeners.
 * 
 * @author David R. Bild
 */
public class LocationProvider {
	private static final String TAG = LocationProvider.class.getSimpleName();

	private static final int MIN_TIME_MS = 5 * 1000;
	private static final int MIN_DISTANCE_M = 10;
	private static final int GPS_TIMEOUT_MS = 15 * 1000;
	private static final int DESIRED_ACCURACY_M = 5;
	private static final int EXPIRY_AGE_MS = 1000 * 60 * 2;

	private final LocationManager mLocationManager;

	private final LocationListenerImpl mGps;

	private final LocationListenerImpl mNetwork;

	private final LocationListenerImpl mPassive;

	private Location bestLocation;

	public LocationProvider(Context context) {
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		mGps = new LocationListenerImpl(mLocationManager, LocationManager.GPS_PROVIDER,
				GPS_TIMEOUT_MS, DESIRED_ACCURACY_M);
		mNetwork = new LocationListenerImpl(mLocationManager, LocationManager.NETWORK_PROVIDER, -1,
				DESIRED_ACCURACY_M);
		mPassive = new LocationListenerImpl(mLocationManager, LocationManager.PASSIVE_PROVIDER, -1,
				-1);

		mGps.getLastKnownLocation();
		mNetwork.getLastKnownLocation();
		mPassive.getLastKnownLocation();
	}

	/**
	 * Get the best estimate for the current location.
	 * 
	 * @return the best estimate for the current location or {@code null} if
	 *         none is available.
	 */
	public Location getLocation() {
		return this.bestLocation;
	}

	/**
	 * Stops all the underlying location provider listeners. This method must be
	 * called before the object is garbage collected.
	 */
	public void stop() {
		mGps.stop();
		mNetwork.stop();
		mPassive.stop();
	}

	private void processUpdate(Location location) {
		synchronized (this) {
			// If no previous location, keep the new one
			if (bestLocation == null) {
				bestLocation = location;
			}

			// If previous location is too old and new one is not, keep the new
			// one.
			else if (expired(bestLocation) && !expired(location)) {
				bestLocation = location;
			}

			// If both have accuracies, keep the more accurate one
			else if (bestLocation.hasAccuracy() && location.hasAccuracy()) {
				if (location.getAccuracy() < bestLocation.getAccuracy()) {
					bestLocation = location;
				}
			}

			// If accuracies cannot be compared, keep the newer one
			else if (location.getTime() > bestLocation.getTime()) {
				bestLocation = location;
			}
		}
	}

	/**
	 * Wrapper to manage a LocationListener instance. This class registers the
	 * listener, gets the last known location from it, unregisters it if the
	 * first update is not received within the specified time, and unregisters
	 * it if the desired accuracy is achieved.
	 * 
	 * @author David R. Bild
	 */
	private class LocationListenerImpl implements LocationListener {

		private final LocationManager mLocationManager;

		private final String provider;

		private final int timeout;

		private final int accuracy;

		private final Handler mHandler;

		private final Runnable mTimeout;

		private boolean enabled;

		private boolean receivedUpdate = false;

		public LocationListenerImpl(LocationManager locationManager, String provider, int timeout,
				int accuracy) {
			this.mLocationManager = locationManager;
			this.provider = provider;
			this.timeout = timeout;
			this.accuracy = accuracy;

			this.mHandler = new Handler();
			this.mTimeout = new Runnable() {
				@Override
				public void run() {
					if (!receivedUpdate) {
						Log.i(TAG,
								String.format(
										"'%s' location provider timed out with receiving location.  Stopped listener.",
										LocationListenerImpl.this.provider));
						stop();
					}
				}
			};

			start();
		}

		private synchronized void start() {
			if (!enabled) {
				try {
					enabled = mLocationManager.isProviderEnabled(provider);
				} catch (SecurityException e) {
					enabled = false;
					Log.i(TAG,
							String.format("'%s' location provider permission not granted.",
									provider));
				}

				if (enabled) {
					mLocationManager.requestLocationUpdates(provider,
							MIN_TIME_MS,
							MIN_DISTANCE_M,
							this);
					if (timeout > 0) {
						mHandler.postDelayed(mTimeout, timeout);
					}
				} else {
					Log.i(TAG, String.format(
							"'%s' location provider not enabled and will not be used.", provider));
				}
			}
		}

		public synchronized void stop() {
			if (enabled) {
				mLocationManager.removeUpdates(this);
				mHandler.removeCallbacks(mTimeout);
				enabled = false;
			}
		}

		/**
		 * Retrieves the last known location for the provider and sends it to
		 * {@link LocationProvider#processUpdate(Location)}.
		 */
		public void getLastKnownLocation() {
			if (enabled) {
				Location location = mLocationManager.getLastKnownLocation(provider);
				processUpdate(location);
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			receivedUpdate = true;
			if (location.hasAccuracy() && (location.getAccuracy() <= accuracy)) {
				Log.i(TAG, String.format(
						"'%s' location provider reached desired accuracy.  Stopped listener.",
						provider));
				stop();
			}
			processUpdate(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	/**
	 * Determines if a location has expired. A location has expired if its age
	 * is greater than {@link #EXPIRY_AGE_MS} milliseconds.
	 * 
	 * @param location the location to check for expiration
	 * @return {@code true} if the location has expired, {@code false}
	 *         otherwise.
	 */
	private static boolean expired(Location location) {
		return age(location) > EXPIRY_AGE_MS;
	}

	/**
	 * Calculates the age of location.
	 * 
	 * @param location the location whose age to calculate
	 * @return age of location in milliseconds
	 */
	private static long age(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}

}
