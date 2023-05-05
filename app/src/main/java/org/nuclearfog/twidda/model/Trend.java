package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Trend interface containing hashtag name or search string and additional information
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

	/**
	 * @return true if current user follows trend (hashtag)
	 */
	boolean following();


	@Override
	default int compareTo(Trend trend) {
		if (getRank() > 0 && trend.getRank() > 0)
			return Integer.compare(getRank(), trend.getRank());
		if (trend.getPopularity() > 0 && getPopularity() > 0)
			return Integer.compare(trend.getPopularity(), getPopularity());
		if (trend.getPopularity() > 0)
			return 1;
		if (getPopularity() > 0)
			return -1;
		return String.CASE_INSENSITIVE_ORDER.compare(getName(), trend.getName());
	}
}