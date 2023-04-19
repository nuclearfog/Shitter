package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

/**
 * userlist implementation
 * <a href="https://docs.joinmastodon.org/entities/List/">Mastodon documentation</a>
 *
 * @author nuclearfog
 */
public class MastodonList implements UserList {

	private static final long serialVersionUID = 2135928743724359656L;

	private long id;
	private String title;

	/**
	 * @param json userlist json object
	 */
	public MastodonList(JSONObject json) throws JSONException {
		String idStr = json.getString("id");
		title = json.getString("title");

		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID:" + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public long getTimestamp() {
		return 0;
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public String getDescription() {
		return "";
	}


	@Override
	public User getListOwner() {
		return null;
	}


	@Override
	public boolean isEdiatable() {
		// Mastodon only shows lists of the current user, so all lists are editable
		return true;
	}


	@Override
	public boolean isPrivate() {
		return false;
	}


	@Override
	public boolean isFollowing() {
		return false;
	}


	@Override
	public int getMemberCount() {
		return 0;
	}


	@Override
	public int getSubscriberCount() {
		return 0;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + getId() + " title=\"" + getTitle() + "\"";
	}


	@Override
	public int compareTo(UserList userlist) {
		if (userlist.getTimestamp() != getTimestamp())
			return Long.compare(userlist.getTimestamp(), getTimestamp());
		return Long.compare(userlist.getId(), getId());
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof UserList))
			return false;
		return ((UserList) obj).getId() == getId();
	}
}