package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Represents an emoji reaction
 *
 * @author nuclearfog
 */
public interface Reaction extends Serializable, Comparable<Reaction> {

	/**
	 * @return title, emoji unicode or custom emoji shortcode of the reaction
	 */
	String getName();

	/**
	 * @return icon image url or empty if unused
	 */
	String getImageUrl();

	/**
	 * @return number of users adding this reaction
	 */
	int getCount();

	/**
	 * @return true if selected by current user
	 */
	boolean isSelected();


	@Override
	default int compareTo(Reaction reaction) {
		return Integer.compare(reaction.getCount(), getCount());
	}
}