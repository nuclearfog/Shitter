package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * interface of an user list
 *
 * @author nuclearfog
 */
public interface UserList extends Serializable, Comparable<UserList> {

	/**
	 * @return ID of the user list
	 */
	long getId();

	/**
	 * @return date of creation
	 */
	long getTimestamp();

	/**
	 * @return title of the list
	 */
	String getTitle();

	/**
	 * @return description of the list
	 */
	String getDescription();

	/**
	 * @return owner of the list
	 */
	@Nullable
	User getListOwner();

	/**
	 * @return true if list is owned by the current user or the user can edit/delete the list
	 */
	boolean isEdiatable();

	/**
	 * @return true if list is private
	 */
	boolean isPrivate();

	/**
	 * @return true if current user is following the list
	 */
	boolean isFollowing();

	/**
	 * @return list member count
	 */
	int getMemberCount();

	/**
	 * @return list subscriber count
	 */
	int getSubscriberCount();


	@Override
	default int compareTo(UserList userlist) {
		if (userlist.getTimestamp() != getTimestamp())
			return Long.compare(userlist.getTimestamp(), getTimestamp());
		return Long.compare(userlist.getId(), getId());
	}
}