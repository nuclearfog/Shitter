package org.nuclearfog.twidda.model;

/**
 * Tweet metrics class containing information like views and link clicks
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
	 * get retweet count
	 *
	 * @return retweet count
	 */
	int getRetweets();

	/**
	 * get like/favorite count
	 *
	 * @return like count
	 */
	int getLikes();

	/**
	 * get reply count
	 *
	 * @return reply count
	 */
	int getReplies();

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
}