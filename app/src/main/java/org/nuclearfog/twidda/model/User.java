package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface for user implementations
 *
 * @author nuclearfog
 */
public interface User extends Serializable {

	/**
	 * @return ID of the user
	 */
	long getId();

	/**
	 * @return user name of the user
	 */
	String getUsername();

	/**
	 * @return screen name of the user
	 */
	String getScreenname();

	/**
	 * @return date of account creation
	 */
	long getCreatedAt();

	/**
	 * @return profile image url
	 */
	String getImageUrl();

	/**
	 * @return profile banner url
	 */
	String getBannerUrl();

	/**
	 * @return profile description (bio)
	 */
	String getDescription();

	/**
	 * @return location name
	 */
	String getLocation();

	/**
	 * @return url added to the profile
	 */
	String getProfileUrl();

	/**
	 * @return true if user is verified
	 */
	boolean isVerified();

	/**
	 * @return true if user is protected
	 */
	boolean isProtected();

	/**
	 * @return true if current user has requested a follow
	 */
	boolean followRequested();

	/**
	 * @return number of following
	 */
	int getFollowing();

	/**
	 * @return number of follower
	 */
	int getFollower();

	/**
	 * @return number of tweets
	 */
	int getStatusCount();

	/**
	 * @return number of favorites/likes
	 */
	int getFavoriteCount();

	/**
	 * @return true if user has a default profile image
	 */
	boolean hasDefaultProfileImage();

	/**
	 * @return true if the user is the same as the current user
	 */
	boolean isCurrentUser();
}