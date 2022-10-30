package org.nuclearfog.twidda.model;

/**
 * interface for location implementation
 *
 * @author nuclearfog
 */
public interface Location {

	/**
	 * @return ID of the place (World ID)
	 */
	int getId();

	/**
	 * @return name of the location (country, city)
	 */
	String getName();
}