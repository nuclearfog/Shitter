package org.nuclearfog.twidda.backend.api.twitter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.util.regex.Pattern;

/**
 * API v 1.1 user list implementation
 *
 * @author nuclearfog
 */
public class UserListV1 implements UserList {

	public static final long serialVersionUID = 4121925943880606236L;

	private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

	private long id;
	private long createdAt;
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
		if (ID_PATTERN.matcher(idStr).matches()) {
			id = Long.parseLong(idStr);
		} else {
			throw new JSONException("bad userlist ID: " + idStr);
		}
		owner = new UserV1(json.getJSONObject("user"), currentId);
		createdAt = StringTools.getTime1(json.optString("created_at"));
		title = json.optString("name");
		description = json.optString("description");
		memberCount = json.optInt("member_count");
		subscriberCount = json.optInt("subscriber_count");
		isPrivate = json.optString("mode").equals("private");
		following = json.optBoolean("following");
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getTimestamp() {
		return createdAt;
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

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof UserList))
			return false;
		return ((UserList) obj).getId() == id;
	}

	@NonNull
	@Override
	public String toString() {
		return "title=\"" + title + "\" description=\"" + description + "\"";
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