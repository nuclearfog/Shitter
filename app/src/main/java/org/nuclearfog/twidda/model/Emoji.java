package org.nuclearfog.twidda.model;

/**
 * Interface used for "custom emoji" implementation
 *
 * @author nuclearfog
 */
public interface Emoji {

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
}