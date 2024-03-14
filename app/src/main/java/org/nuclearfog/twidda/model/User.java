package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface for user implementations
 *
 * @author nuclearfog
 */
public interface User extends Serializable, Comparable<User> {

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
	long getTimestamp();

	/**
	 * @return profile image url
	 */
	String getOriginalProfileImageUrl();

	/**
	 * @return small profile image url
	 */
	String getProfileImageThumbnailUrl();

	/**
	 * @return profile banner url in the highest available resolution
	 */
	String getOriginalBannerImageUrl();

	/**
	 * @return small banner image url
	 */
	String getBannerImageThumbnailUrl();

	/**
	 * @return true if user has a default profile image
	 */
	boolean hasDefaultProfileImage();

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
	 * @return true if user is indexable (user information and post are accessable in the public)
	 */
	boolean isIndexable();

	/**
	 * @return true if following/follower information is public
	 */
	boolean isDiscoverable();

	/**
	 * @return true if user is a bot
	 */
	boolean isBot();

	/**
	 * @return true if user represents a group
	 */
	boolean isGroup();

	/**
	 * @return true if the user is the same as the current user
	 */
	boolean isCurrentUser();

	/**
	 * @return number of following
	 */
	int getFollowing();

	/**
	 * @return number of follower
	 */
	int getFollower();

	/**
	 * @return number of statuses
	 */
	int getStatusCount();

	/**
	 * @return number of favorites/likes
	 */
	int getFavoriteCount();

	/**
	 * @return custom emojis used in the profile
	 */
	Emoji[] getEmojis();

	/**
	 * @return fields set by user
	 */
	Field[] getFields();


	@Override
	default int compareTo(User o) {
		return Long.compare(o.getId(), getId());
	}
}