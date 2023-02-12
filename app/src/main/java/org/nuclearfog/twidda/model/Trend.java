package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface for trend implementations
 *
 * @author nuclearfog
 */
public interface Trend extends Serializable, Comparable<Trend> {

	/**
	 * @return trend name
	 */
	String getName();

	/**
	 * @return ID of the trend location
	 */
	long getLocationId();

	/**
	 * @return rank of the trend
	 */
	int getRank();

	/**
	 * @return popularity of the trend
	 */
	int getPopularity();
}