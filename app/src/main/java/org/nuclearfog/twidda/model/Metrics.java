package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Status metrics class containing information like views and link clicks
 *
 * @author nuclearfog
 */
public interface Metrics extends Serializable {

	/**
	 * get view count of the status
	 *
	 * @return view count
	 */
	int getViews();

	/**
	 * get number of quotes
	 *
	 * @return quote count
	 */
	int getQuoteCount();

	/**
	 * get link click count
	 *
	 * @return click count
	 */
	int getLinkClicks();

	/**
	 * get profile click count
	 *
	 * @return click count
	 */
	int getProfileClicks();

	/**
	 * get video view count
	 *
	 * @return video view count
	 */
	int getVideoViews();
}