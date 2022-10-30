package org.nuclearfog.twidda.model;

/**
 * Status metrics class containing information like views and link clicks
 *
 * @author nuclearfog
 */
public interface Metrics {

	/**
	 * get view count of the tweet
	 *
	 * @return view count
	 */
	int getViews();

	/**
	 * get repost count
	 *
	 * @return repost count
	 */
	int getReposts();

	/**
	 * get like/favorite count
	 *
	 * @return like count
	 */
	int getFavorits();

	/**
	 * get reply count
	 *
	 * @return reply count
	 */
	int getReplies();

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