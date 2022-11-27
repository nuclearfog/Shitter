package org.nuclearfog.twidda.backend.api.mastodon.impl;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
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
	private long createdAt;

	private int replyCount, favoriteCount, reblogCount;
	private boolean favorited, reblogged, sensitive;

	private String text, source;

	private User author;


	public MastodonStatus(JSONObject json, long currentUserId) throws JSONException {
		JSONObject application = json.optJSONObject("application");
		id = Long.parseLong(json.getString("id"));
		createdAt = StringTools.getTime2(json.optString("created_at"));
		replyCount = json.optInt("replies_count");
		reblogCount = json.optInt("reblogs_count");
		favoriteCount = json.optInt("favourites_count");
		favorited = json.optBoolean("favourited");
		reblogged = json.optBoolean("reblogged");
		text = json.optString("content", "");
		sensitive = json.optBoolean("sensitive", false);
		if (application != null)
			source = application.optString("name", "");
		else
			source = "";
		author = new MastodonUser(json.getJSONObject("account"), currentUserId);
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
		return 0;
	}


	@Override
	public long getRepliedStatusId() {
		return 0;
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
		return "";
	}


	@Override
	public int getMediaType() {
		return 0;
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
}