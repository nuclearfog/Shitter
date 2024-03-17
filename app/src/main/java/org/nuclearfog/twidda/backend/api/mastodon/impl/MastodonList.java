package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
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
	private int policy;
	private boolean isExclusive;

	/**
	 * @param json userlist json object
	 */
	public MastodonList(JSONObject json) throws JSONException {
		String idStr = json.getString("id");
		String policyStr = json.optString("replies_policy", "");
		title = json.getString("title");
		isExclusive = json.optBoolean("exclusive", false);

		switch (policyStr) {
			case "followed":
				policy = REPLIES_FOLLOWING;
				break;

			case "list":
				policy = REPLIES_MEMBER;
				break;

			default:
				policy = REPLIES_NONE;
				break;
		}
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
	public String getTitle() {
		return title;
	}


	@Override
	public int getReplyPolicy() {
		return policy;
	}


	@Override
	public boolean isExclusive() {
		return isExclusive;
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
}