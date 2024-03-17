package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface of an user list
 *
 * @author nuclearfog
 */
public interface UserList extends Serializable, Comparable<UserList> {

	/**
	 * Show replies to no one
	 */
	int REPLIES_NONE = 0;

	/**
	 * Show replies to any followed user
	 */
	int REPLIES_FOLLOWING = 1;

	/**
	 * Show replies to members of the list
	 */
	int REPLIES_MEMBER = 2;

	/**
	 * @return ID of the user list
	 */
	long getId();

	/**
	 * @return title of the list
	 */
	String getTitle();

	/**
	 * @return Which replies should be shown in the list {@link #REPLIES_NONE ,#FOLLOWED,#LIST}
	 */
	int getReplyPolicy();

	/**
	 * @return true if members of this list are excluded from the home timeline
	 */
	boolean isExclusive();


	@Override
	default int compareTo(UserList userlist) {
		return Long.compare(userlist.getId(), getId());
	}
}