package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface for relation implementations
 *
 * @author nuclearfog
 */
public interface Relation extends Serializable {

	/**
	 * @return User ID
	 */
	long getId();

	/**
	 * @return true if current user is following this user
	 */
	boolean isFollowing();

	/**
	 * @return true if this user is a follower
	 */
	boolean isFollower();

	/**
	 * @return true if this user is blocked
	 */
	boolean isBlocked();

	/**
	 * @return true if this user is muted
	 */
	boolean isMuted();
}