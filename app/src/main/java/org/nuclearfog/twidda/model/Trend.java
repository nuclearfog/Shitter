package org.nuclearfog.twidda.model;

/**
 * interface for trend implementations
 *
 * @author nuclearfog
 */
public interface Trend {

	/**
	 * @return trend name
	 */
	String getName();

	/**
	 * @return ID of the trend location
	 */
	int getLocationId();

	/**
	 * @return rank of the trend
	 */
	int getRank();

	/**
	 * @return popularity of the trend
	 */
	int getPopularity();
}