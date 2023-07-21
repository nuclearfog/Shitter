package org.nuclearfog.twidda.backend.api.mastodon.impl;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Emoji;
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
	private String url;
	private String screenname;
	private String username;
	private String profileUrl = "";
	private String bannerUrl = "";
	private String description = "";

	private int following;
	private int follower;
	private int statusCount;
	private boolean locked;
	private boolean isCurrentUser;
	private Emoji[] emojis = {};
	private Field[] fields = {};

	/**
	 * constructor used to create an user instance of the current user
	 *
	 * @param json json object used by Mastodon API
	 */
	public MastodonUser(JSONObject json) throws JSONException {
		this(json, 0L);
		isCurrentUser = true;
	}

	/**
	 * default constructor for all user instances
	 *
	 * @param json          json object used by Mastodon API
	 * @param currentUserId current user ID
	 */
	public MastodonUser(JSONObject json, long currentUserId) throws JSONException {
		JSONArray emojiArray = json.optJSONArray("emojis");
		JSONArray fieldsArray = json.optJSONArray("fields");
		String idStr = json.getString("id");
		String description = json.optString("note", "");
		String profileUrl = json.optString("avatar_static", "");
		String bannerUrl = json.optString("header_static", "");
		String createdAtStr = json.optString("created_at", "");
		screenname = '@' + json.optString("acct", "");
		username = json.optString("display_name", "");
		createdAt = StringUtils.getTime(createdAtStr, StringUtils.TIME_MASTODON);
		url = json.optString("url", "");
		following = json.optInt("following_count");
		follower = json.optInt("followers_count");
		statusCount = json.optInt("statuses_count");
		locked = json.optBoolean("locked");
		if (!description.isEmpty()) {
			this.description = Jsoup.parse(description).text();
		}
		if (Patterns.WEB_URL.matcher(profileUrl).matches()) {
			this.profileUrl = profileUrl;
		}
		if (Patterns.WEB_URL.matcher(bannerUrl).matches()) {
			this.bannerUrl = bannerUrl;
		}
		if (emojiArray != null && emojiArray.length() > 0) {
			emojis = new Emoji[emojiArray.length()];
			for (int i = 0; i < emojis.length; i++) {
				JSONObject emojiJson = emojiArray.getJSONObject(i);
				emojis[i] = new MastodonEmoji(emojiJson);
			}
		}
		if (fieldsArray != null && fieldsArray.length() > 0) {
			fields = new Field[fieldsArray.length()];
			for (int i = 0; i < fields.length; i++) {
				JSONObject fieldJson = fieldsArray.getJSONObject(i);
				fields[i] = new MastodonField(fieldJson);
			}
		}
		try {
			id = Long.parseLong(idStr);
			isCurrentUser = currentUserId == id;
		} catch (NumberFormatException e) {
			throw new JSONException("bad user ID:" + idStr);
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
	public long getTimestamp() {
		return createdAt;
	}


	@Override
	public String getOriginalProfileImageUrl() {
		return profileUrl;
	}


	@Override
	public String getProfileImageThumbnailUrl() {
		return profileUrl; // todo switch to thumbnail url if supported by API
	}


	@Override
	public String getOriginalBannerImageUrl() {
		return bannerUrl;
	}


	@Override
	public String getBannerImageThumbnailUrl() {
		return bannerUrl; // todo switch to thumbnail url if supported by API
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
		// using getFields() instead
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
		return -1;
	}


	@Override
	public boolean hasDefaultProfileImage() {
		return false;
	}


	@Override
	public boolean isCurrentUser() {
		return isCurrentUser;
	}


	@Override
	public Emoji[] getEmojis() {
		return emojis;
	}


	@Override
	public Field[] getFields() {
		return fields;
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
	 *
	 */
	private static class MastodonField implements Field {

		private static final long serialVersionUID = 2278113885084330065L;

		private String key;
		private String value;
		private long timestamp = 0L;

		/**
		 * @param json fields json
		 */
		public MastodonField(JSONObject json) throws JSONException {
			key = json.getString("name");
			value = StringUtils.extractText(json.optString("value", ""));

			String timeStr = json.getString("verified_at");
			if (!timeStr.equals("null")) {
				timestamp = StringUtils.getTime(timeStr, StringUtils.TIME_MASTODON);
			}
		}


		@Override
		public String getKey() {
			return key;
		}


		@Override
		public String getValue() {
			return value;
		}


		@Override
		public long getTimestamp() {
			return timestamp;
		}
	}
}