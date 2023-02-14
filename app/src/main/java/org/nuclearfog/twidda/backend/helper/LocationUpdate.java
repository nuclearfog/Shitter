package org.nuclearfog.twidda.backend.helper;

import android.location.Location;

import androidx.annotation.NonNull;

/**
 * This class contains location information used for {@link StatusUpdate}
 *
 * @author nuclearfog
 */
public class LocationUpdate {

	private double[] coordinates = new double[2];

	/**
	 * @param location Android location information
	 */
	public LocationUpdate(Location location) {
		coordinates[0] = location.getLongitude();
		coordinates[1] = location.getLatitude();
	}

	/**
	 * get GPS longitute
	 *
	 * @return longitute value
	 */
	public double getLongitude() {
		return coordinates[0];
	}

	/**
	 * get GPS latitude
	 *
	 * @return latitude value
	 */
	public double getLatitude() {
		return coordinates[1];
	}


	@NonNull
	@Override
	public String toString() {
		return "longitude=" + coordinates[0] + " latitude=" + coordinates[1];
	}
}