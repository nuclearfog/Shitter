package org.nuclearfog.twidda.backend.helper.update;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.UserList;

import java.io.Serializable;

/**
 * This class is used to upload list information
 *
 * @author nuclearfog
 */
public class UserListUpdate implements Serializable {

	private static final long serialVersionUID = -366691257985800712L;

	private long listId;
	private String title;
	private int policy;
	private boolean isExclusive;

	/**
	 *
	 */
	public UserListUpdate() {
		listId = 0L;
		title = "";
		policy = UserList.NONE;
		isExclusive = false;
	}

	/**
	 *
	 */
	public UserListUpdate(UserList list) {
		listId = list.getId();
		title = list.getTitle();
		policy = list.getReplyPolicy();
		isExclusive = false; // todo implement this
	}

	/**
	 * set ID of an existing list to update
	 *
	 * @param listId ID of an existing list
	 */
	public void setId(long listId) {
		this.listId = listId;
	}

	/**
	 * get ID of the list
	 *
	 * @return list ID
	 */
	public long getId() {
		return listId;
	}

	/**
	 * set list title
	 *
	 * @param title title text
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * get Title of the list
	 *
	 * @return Title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * set userlist policy
	 *
	 * @param policy values from {@link UserList#NONE,UserList#FOLLOWED,UserList#LIST}
	 */
	public void setPolicy(int policy) {
		this.policy = policy;
	}

	/**
	 * get userlist policy
	 *
	 * @return  policy values from {@link UserList#NONE,UserList#FOLLOWED,UserList#LIST}
	 */
	public int getPolicy() {
		return policy;
	}

	/**
	 *
	 */
	public void setExclusive(boolean exclusive) {
		this.isExclusive = exclusive;
	}

	/**
	 *
	 */
	public boolean isExclusive() {
		return isExclusive;
	}


	@NonNull
	@Override
	public String toString() {
		if (getId() != 0L)
			return "id=" + getId() + " title=\"" + getTitle() + "\"";
		return "title=\"" + getTitle() + "\"";
	}
}