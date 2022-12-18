package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface for location implementation
 *
 * @author nuclearfog
 */
public interface Location extends Serializable {

	/**
	 * @return ID of the place (World ID)
	 */
	int getId();

	/**
	 * @return name of the location (country, city)
	 */
	String getName();
}