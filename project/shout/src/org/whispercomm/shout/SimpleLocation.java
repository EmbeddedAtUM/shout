
package org.whispercomm.shout;

/**
 * An immutable implementation of {@link Location}.
 * 
 * @author David R. Bild
 */
public class SimpleLocation implements Location {
	private final double longitude;
	private final double latitude;

	/**
	 * Creates a new {@code SimpleLocation}.
	 * 
	 * @param longitude the longitude of this location in decimal degrees
	 * @param latitude the latitude of this location in decimal degrees
	 */
	public SimpleLocation(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	@Override
	public double getLongitude() {
		return longitude;
	}

	@Override
	public double getLatitude() {
		return latitude;
	}
}
