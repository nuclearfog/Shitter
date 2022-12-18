package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface for relation implementations
 *
 * @author nuclearfog
 */
public interface Relation extends Serializable {

	/**
	 * @return true if the relation points to the current user
	 */
	boolean isCurrentUser();

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

	/**
	 * @return true if this user accepts direct messages from the current user
	 */
	boolean canDm();
}