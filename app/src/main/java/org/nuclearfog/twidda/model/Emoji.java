package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Interface used for "custom emoji" implementation
 *
 * @author nuclearfog
 */
public interface Emoji extends Serializable, Comparable<Emoji> {

	/**
	 * short code of an emoji used by the server
	 *
	 * @return short code
	 */
	String getCode();

	/**
	 * url of the emoji image
	 *
	 * @return url
	 */
	String getUrl();

	/**
	 * category of the emoji
	 *
	 * @return category name
	 */
	String getCategory();


	@Override
	default int compareTo(Emoji emoji) {
		int categoryCompare = String.CASE_INSENSITIVE_ORDER.compare(emoji.getCategory(), getCategory());
		if (categoryCompare != 0)
			return categoryCompare;
		return String.CASE_INSENSITIVE_ORDER.compare(emoji.getCode(), getCode());
	}
}