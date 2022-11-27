package org.nuclearfog.twidda.backend.api.mastodon.impl;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

/**
 * Status implementation for Mastodon
 *
 * @author nuclearfog
 */
public class MastodonStatus implements Status {

	private static final long serialVersionUID = 1184375228249441241L;

	private long id;
	private long replyId;
	private long replyUserId;
	private long createdAt;

	private int replyCount, favoriteCount, reblogCount;
	private boolean favorited, reblogged, sensitive;

	private String text, source, mentions;

	private User author;

	/**
	 * @param json          Mastodon status json object
	 * @param currentUserId Id of the current user
	 */
	public MastodonStatus(JSONObject json, long currentUserId) throws JSONException {
		JSONObject application = json.optJSONObject("application");
		JSONArray mentionsJson = json.optJSONArray("mentions");
		String idStr = json.getString("id");
		String replyIdStr = json.optString("in_reply_to_id", "0");
		String replyUserIdStr = json.optString("in_reply_to_account_id", "0");

		author = new MastodonUser(json.getJSONObject("account"), currentUserId);
		createdAt = StringTools.getTime2(json.optString("created_at"));
		replyCount = json.optInt("replies_count");
		reblogCount = json.optInt("reblogs_count");
		favoriteCount = json.optInt("favourites_count");
		favorited = json.optBoolean("favourited");
		reblogged = json.optBoolean("reblogged");
		text = json.optString("content", "");
		text = Jsoup.parse(text).text();
		sensitive = json.optBoolean("sensitive", false);
		if (mentionsJson != null) {
			StringBuilder mentionsBuilder = new StringBuilder();
			for (int i = 0; i < mentionsJson.length(); i++) {
				String item = mentionsJson.getJSONObject(i).optString("acct", "");
				mentionsBuilder.append('@').append(item).append(' ');
			}
			mentions = mentionsBuilder.toString();
		} else {
			mentions = "";
		}
		if (application != null) {
			source = application.optString("name", "");
		} else {
			source = "";
		}
		try {
			id = Long.parseLong(idStr);
			if (!replyIdStr.equals("null"))
				replyId = Long.parseLong(replyIdStr);
			if (!replyUserIdStr.equals("null"))
				replyUserId = Long.parseLong(replyUserIdStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID:" + idStr + ' ' + replyIdStr + ' ' + replyUserIdStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getText() {
		return text;
	}


	@Override
	public User getAuthor() {
		return author;
	}


	@Override
	public long getTimestamp() {
		return createdAt;
	}


	@Override
	public String getSource() {
		return source;
	}


	@Nullable
	@Override
	public Status getEmbeddedStatus() {
		return null;
	}


	@Override
	public String getReplyName() {
		return "";
	}


	@Override
	public long getRepliedUserId() {
		return replyUserId;
	}


	@Override
	public long getRepliedStatusId() {
		return replyId;
	}


	@Override
	public long getRepostId() {
		return 0;
	}


	@Override
	public int getRepostCount() {
		return reblogCount;
	}


	@Override
	public int getFavoriteCount() {
		return favoriteCount;
	}


	@Override
	public int getReplyCount() {
		return replyCount;
	}


	@NonNull
	@Override
	public Uri[] getMediaUris() {
		return new Uri[0];
	}


	@Override
	public String getUserMentions() {
		return mentions;
	}


	@Override
	public int getMediaType() {
		return MEDIA_NONE;
	}


	@Override
	public boolean isSensitive() {
		return sensitive;
	}


	@Override
	public boolean isReposted() {
		return reblogged;
	}


	@Override
	public boolean isFavorited() {
		return favorited;
	}


	@Override
	public boolean isHidden() {
		return false;
	}


	@Override
	public String getLocationName() {
		return "";
	}


	@Override
	public String getLocationCoordinates() {
		return "";
	}


	@NonNull
	@Override
	public String toString() {
		return author.toString() + " text=\"" + text + "\"";
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Status))
			return false;
		return ((Status) obj).getId() == id;
	}
}