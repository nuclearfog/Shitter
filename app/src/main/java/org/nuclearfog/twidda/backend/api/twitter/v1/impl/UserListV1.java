package org.nuclearfog.twidda.backend.api.twitter.v1.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

/**
 * API v 1.1 user list implementation
 *
 * @author nuclearfog
 */
public class UserListV1 implements UserList {

	private static final long serialVersionUID = 4121925943880606236L;

	/**
	 * indicates that a list can only be accessed by the owner
	 */
	private static final String PRIVATE = "private";

	private long id;
	private long timestamp;
	private String title;
	private String description;
	private int memberCount;
	private int subscriberCount;
	private boolean isPrivate;
	private boolean following;
	private User owner;

	/**
	 * @param json      JSON object containing userlist information
	 * @param currentId ID of the current user
	 * @throws JSONException if values are missing
	 */
	public UserListV1(JSONObject json, long currentId) throws JSONException {
		String idStr = json.getString("id_str");

		owner = new UserV1(json.getJSONObject("user"), currentId);
		timestamp = StringUtils.getTime(json.optString("created_at", ""), StringUtils.TIME_TWITTER_V1);
		title = json.optString("name", "");
		description = json.optString("description", "");
		memberCount = json.optInt("member_count");
		subscriberCount = json.optInt("subscriber_count");
		isPrivate = PRIVATE.equals(json.optString("mode"));
		following = json.optBoolean("following");
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad userlist ID: " + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public boolean isEdiatable() {
		return owner.isCurrentUser();
	}


	@Override
	public User getListOwner() {
		return owner;
	}


	@Override
	public boolean isPrivate() {
		return isPrivate;
	}


	@Override
	public boolean isFollowing() {
		return following;
	}


	@Override
	public int getMemberCount() {
		return memberCount;
	}


	@Override
	public int getSubscriberCount() {
		return subscriberCount;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + getId() + " title=\"" + getTitle() + "\"";
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof UserList))
			return false;
		return ((UserList) obj).getId() == getId();
	}

	/**
	 * set manually follow status
	 *
	 * @param following following status
	 */
	public void setFollowing(boolean following) {
		this.following = following;
	}
}