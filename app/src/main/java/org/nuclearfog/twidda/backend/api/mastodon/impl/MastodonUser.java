package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.User;

/**
 * User implementation for Mastodon API
 *
 * @author nuclearfog
 */
public class MastodonUser implements User {

	private static final long serialVersionUID = 7322491410965084755L;

	private long id;
	private long createdAt;
	private String screenname, username;
	private String profileUrl, bannerUrl;
	private String description, url;

	private int following, follower;
	private int statusCount;
	private boolean locked;
	private boolean isCurrentUser = true;

	/**
	 * @param json          json object used by Mastodon API
	 * @param currentUserId ID of the current user
	 */
	public MastodonUser(JSONObject json, long currentUserId) throws JSONException {
		this(json);
		isCurrentUser = currentUserId == id;
	}

	/**
	 * @param json json object used by Mastodon API
	 */
	public MastodonUser(JSONObject json) throws JSONException {
		screenname = json.optString("acct", "");
		username = json.optString("display_name");
		createdAt = StringTools.getTime2(json.optString("created_at", ""));
		profileUrl = json.optString("avatar");
		bannerUrl = json.optString("banner");
		description = json.optString("note");
		url = json.optString("url");
		following = json.optInt("following_count");
		follower = json.optInt("followers_count");
		statusCount = json.optInt("statuses_count");
		locked = json.optBoolean("locked");

		try {
			id = Long.parseLong(json.getString("id"));
		} catch (NumberFormatException e) {
			throw new JSONException("bad user ID:" + id);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getUsername() {
		return username;
	}


	@Override
	public String getScreenname() {
		return screenname;
	}


	@Override
	public long getCreatedAt() {
		return createdAt;
	}


	@Override
	public String getImageUrl() {
		return profileUrl;
	}


	@Override
	public String getBannerUrl() {
		return bannerUrl;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public String getLocation() {
		return "";
	}


	@Override
	public String getProfileUrl() {
		return url;
	}


	@Override
	public boolean isVerified() {
		return false;
	}


	@Override
	public boolean isProtected() {
		return locked;
	}


	@Override
	public boolean followRequested() {
		return false;
	}


	@Override
	public int getFollowing() {
		return following;
	}


	@Override
	public int getFollower() {
		return follower;
	}


	@Override
	public int getStatusCount() {
		return statusCount;
	}


	@Override
	public int getFavoriteCount() {
		return 0;
	}


	@Override
	public boolean hasDefaultProfileImage() {
		return false;
	}


	@Override
	public boolean isCurrentUser() {
		return isCurrentUser;
	}
}