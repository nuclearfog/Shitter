package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface of an user list
 *
 * @author nuclearfog
 */
public interface UserList extends Serializable, Comparable<UserList> {

	int NONE = 0;

	int FOLLOWED = 1;

	int LIST = 2;

	/**
	 * @return ID of the user list
	 */
	long getId();

	/**
	 * @return title of the list
	 */
	String getTitle();

	/**
	 * @return Which replies should be shown in the list {@link #NONE,#FOLLOWED,#LIST}
	 */
	int getReplyPolicy();


	@Override
	default int compareTo(UserList userlist) {
		return Long.compare(userlist.getId(), getId());
	}
}