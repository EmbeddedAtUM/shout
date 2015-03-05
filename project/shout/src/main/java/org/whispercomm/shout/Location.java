
package org.whispercomm.shout;

/**
 * A location on the surface of the earth.
 * 
 * @author David R. Bild
 */
public interface Location {
	/**
	 * Get the longitude of this location
	 * 
	 * @return the longitude in decimal degrees
	 */
	public double getLongitude();

	/**
	 * Get the latitude of this location
	 * 
	 * @return the latitude in decimal degrees
	 */
	public double getLatitude();
}
