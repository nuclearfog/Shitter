package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface for location implementation
 *
 * @author nuclearfog
 */
public interface Location extends Serializable {

	String LOCATION_TYPE = "Point";

	/**
	 * @return unique place id
	 */
	long getId();

	/**
	 * @return ID of the place (World ID)
	 */
	int getWorldId();

	/**
	 * @return country name
	 */
	String getCountry();

	/**
	 * @return place name (e.g. city name)
	 */
	String getPlace();

	/**
	 * @return comma separated GPS coordinates
	 */
	String getCoordinates();

	/**
	 * @return name of the location (country, city)
	 */
	String getFullName();
}