package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * @author nuclearfog
 */
public interface Tag extends Serializable, Comparable<Tag> {

	/**
	 * @return tag name
	 */
	String getName();

	/**
	 * @return trend ID if any
	 */
	long getId();

	/**
	 * @return tag ID
	 */
	long getLocationId();

	/**
	 * @return rank of the tag if any
	 */
	int getRank();

	/**
	 * @return popularity of the tag
	 */
	int getPopularity();

	/**
	 * @return true if current user follows tag
	 */
	boolean isFollowed();


	@Override
	default int compareTo(Tag tag) {
		if (getRank() > 0 && tag.getRank() > 0)
			return Integer.compare(getRank(), tag.getRank());
		if (tag.getPopularity() > 0 && getPopularity() > 0)
			return Integer.compare(tag.getPopularity(), getPopularity());
		if (tag.getPopularity() > 0)
			return 1;
		if (getPopularity() > 0)
			return -1;
		return String.CASE_INSENSITIVE_ORDER.compare(getName(), tag.getName());
	}
}