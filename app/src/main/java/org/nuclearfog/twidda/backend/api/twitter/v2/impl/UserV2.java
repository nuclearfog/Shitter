package org.nuclearfog.twidda.backend.api.twitter.v2.impl;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.User;

/**
 * User implementation of API V2
 *
 * @author nuclearfog
 */
public class UserV2 implements User {

	private static final long serialVersionUID = 1136243062864162774L;

	/**
	 * extra parameters required to fetch additional data
	 */
	public static final String FIELDS_USER = "user.fields=created_at%2Cdescription%2Centities%2Cid%2Clocation%2Cname%2Cprofile_image_url" +
			"%2Cprotected%2Cpublic_metrics%2Curl%2Cusername%2Cverified";

	private long id;
	private long createdAt;
	private String username;
	private String screenName;
	private String description;
	private String location;
	private String url;
	private String profileImageUrl = "";
	private String profileBannerUrl = "";
	private int following;
	private int follower;
	private int tweetCount;
	private boolean isCurrentUser;
	private boolean isVerified;
	private boolean isProtected;
	private boolean defaultImage;

	/**
	 * @param json      JSON object containing user data
	 * @param twitterId ID of the current user
	 */
	public UserV2(JSONObject json, long twitterId) throws JSONException {
		JSONObject metrics = json.getJSONObject("public_metrics");
		String idStr = json.getString("id");
		String profileImageUrl = json.optString("profile_image_url", "");
		String profileBannerUrl = json.optString("profile_banner_url", "");
		String createdAtStr = json.optString("created_at", "");
		username = json.optString("name", "");
		screenName = '@' + json.optString("username", "");
		isProtected = json.optBoolean("protected");
		location = json.optString("location", "");
		isVerified = json.optBoolean("verified");
		createdAt = StringUtils.getTime(createdAtStr, StringUtils.TIME_TWITTER_V2);
		defaultImage = profileImageUrl.contains("default_profile_images");
		url = getUrl(json);
		description = getDescription(json);
		following = metrics.optInt("following_count");
		follower = metrics.optInt("followers_count");
		tweetCount = metrics.optInt("tweet_count");

		if (Patterns.WEB_URL.matcher(profileImageUrl).matches()) {
			if (defaultImage) {
				this.profileImageUrl = profileImageUrl;
			} else {
				this.profileImageUrl = StringUtils.createProfileImageLink(profileImageUrl);
			}
		}
		if (Patterns.WEB_URL.matcher(profileBannerUrl).matches()) {
			this.profileBannerUrl = profileBannerUrl;
		}
		try {
			id = Long.parseLong(idStr);
			isCurrentUser = id == twitterId;
		} catch (NumberFormatException e) {
			throw new JSONException("bad user ID: " + idStr);
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
		return screenName;
	}


	@Override
	public long getTimestamp() {
		return createdAt;
	}


	@Override
	public String getOriginalProfileImageUrl() {
		return profileImageUrl;
	}


	@Override
	public String getProfileImageThumbnailUrl() {
		if (defaultImage || profileImageUrl.isEmpty())
			return profileImageUrl;
		return profileImageUrl + "_bigger";
	}


	@Override
	public String getOriginalBannerImageUrl() {
		if (profileBannerUrl.isEmpty())
			return "";
		return profileBannerUrl + "/1500x500";
	}


	@Override
	public String getBannerImageThumbnailUrl() {
		if (profileBannerUrl.isEmpty())
			return "";
		return profileBannerUrl + "/600x200";
	}


	@Override
	public boolean hasDefaultProfileImage() {
		return defaultImage;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public String getLocation() {
		return location;
	}


	@Override
	public String getProfileUrl() {
		return url;
	}


	@Override
	public boolean isVerified() {
		return isVerified;
	}


	@Override
	public boolean isProtected() {
		return isProtected;
	}


	@Override
	public boolean followRequested() {
		// todo not yet implemented in API V2
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
		return tweetCount;
	}


	@Override
	public int getFavoriteCount() {
		// todo not yet implemented in API V2
		return -1;
	}


	@Override
	public boolean isCurrentUser() {
		return isCurrentUser;
	}


	@Override
	public Emoji[] getEmojis() {
		return new Emoji[0];
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof User))
			return false;
		return ((User) obj).getId() == getId();
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + getScreenname() + "\"";
	}


	/**
	 * expand URLs of the user description
	 *
	 * @param json root json object of user v1
	 * @return user description
	 */
	@NonNull
	private String getDescription(JSONObject json) {
		String description = json.optString("description", "");
		JSONObject entities = json.optJSONObject("entities");
		if (entities != null) {
			JSONObject descrEntities = entities.optJSONObject("description");
			if (descrEntities != null) {
				try {
					// expand shortened urls
					JSONArray urls = descrEntities.getJSONArray("urls");
					StringBuilder builder = new StringBuilder(description);
					for (int i = urls.length() - 1; i >= 0; i--) {
						JSONObject entry = urls.getJSONObject(i);
						String link = entry.getString("expanded_url");
						int start = entry.getInt("start");
						int end = entry.getInt("end");
						int offset = StringUtils.calculateIndexOffset(description, start);
						builder.replace(start + offset, end + offset, link);
					}
					return builder.toString();
				} catch (JSONException e) {
					// ignore, use default description
				}
			}
		}
		return description;
	}

	/**
	 * get expanded profile url
	 *
	 * @param json root json object of user v1
	 * @return expanded url
	 */
	@NonNull
	private String getUrl(JSONObject json) {
		JSONObject entities = json.optJSONObject("entities");
		if (entities != null) {
			JSONObject url = entities.optJSONObject("url");
			if (url != null) {
				try {
					JSONArray urls = url.getJSONArray("urls");
					if (urls.length() > 0) {
						return urls.getJSONObject(0).getString("display_url");
					}
				} catch (JSONException e) {
					// ignore
				}
			}
		}
		return "";
	}
}