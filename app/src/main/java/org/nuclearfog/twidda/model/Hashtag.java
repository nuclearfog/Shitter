package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Trend interface containing hashtag name or search string and additional information
 *
 * @author nuclearfog
 */
public interface Hashtag extends Serializable, Comparable<Hashtag> {

	/**
	 * @return hashtag name
	 */
	String getName();

	/**
	 * @return trend ID if any
	 */
	long getId();

	/**
	 * @return hashtag ID
	 */
	long getLocationId();

	/**
	 * @return rank of the hashtag if any
	 */
	int getRank();

	/**
	 * @return popularity of the hashtag
	 */
	int getPopularity();

	/**
	 * @return true if current user follows hashtag
	 */
	boolean following();


	@Override
	default int compareTo(Hashtag hashtag) {
		if (getRank() > 0 && hashtag.getRank() > 0)
			return Integer.compare(getRank(), hashtag.getRank());
		if (hashtag.getPopularity() > 0 && getPopularity() > 0)
			return Integer.compare(hashtag.getPopularity(), getPopularity());
		if (hashtag.getPopularity() > 0)
			return 1;
		if (getPopularity() > 0)
			return -1;
		return String.CASE_INSENSITIVE_ORDER.compare(getName(), hashtag.getName());
	}
}