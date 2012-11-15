package org.whispercomm.shout.test.util;

import org.whispercomm.shout.Location;

public class TestLocation implements Location {
	
	public double latitude;
	public double longitude;
	
	public TestLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
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
