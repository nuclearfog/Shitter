package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 *  This interface represents a link card, containing information about a website
 *
 * @author nuclearfog
 */
public interface Card extends Serializable {

	/**
	 * @return website title
	 */
	String getTitle();

	/**
	 * @return description used by the website
	 */
	String getDescription();

	/**
	 * @return original url of the website
	 */
	String getUrl();

	/**
	 * @return preview image link
	 */
	String getImageUrl();
}