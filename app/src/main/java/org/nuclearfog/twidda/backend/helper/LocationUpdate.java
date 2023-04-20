package org.nuclearfog.twidda.backend.helper;


import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Location;

/**
 * This class contains location information used for {@link StatusUpdate}
 *
 * @author nuclearfog
 */
public class LocationUpdate {

	private double longitude, latitude;

	/**
	 *
	 */
	public LocationUpdate(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/**
	 * create locationupdate from status location
	 *
	 * @param location location information
	 */
	public LocationUpdate(Location location) {
		String[] locationStr = location.getCoordinates().split(",");
		try {
			this.longitude = Double.parseDouble(locationStr[0]);
			this.latitude = Double.parseDouble(locationStr[1]);
		} catch (NumberFormatException e) {
			longitude = 0.0;
			latitude = 0.0;
		}
	}

	/**
	 * get GPS longitute
	 *
	 * @return longitute value
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * get GPS latitude
	 *
	 * @return latitude value
	 */
	public double getLatitude() {
		return latitude;
	}


	@NonNull
	@Override
	public String toString() {
		return "longitude=" + longitude + " latitude=" + latitude;
	}
}