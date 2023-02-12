package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

/**
 * notification implementation for Mastodon
 *
 * @author nuclearfog
 */
public class MastodonNotification implements Notification {

	private static final long serialVersionUID = 4113306729125959429L;

	private long id;
	private long timestamp;
	private int type;
	private User user;
	private Status status;

	/**
	 * @param json      notification json object
	 * @param currentId Id of the current user
	 */
	public MastodonNotification(JSONObject json, long currentId) throws JSONException {
		String idStr = json.getString("id");
		String typeStr = json.getString("type");
		JSONObject statusJson = json.optJSONObject("status");
		JSONObject userJson = json.getJSONObject("account");
		timestamp = StringTools.getTime(json.getString("created_at"), StringTools.TIME_MASTODON);
		user = new MastodonUser(userJson);

		switch (typeStr) {
			case "mention":
				type = TYPE_MENTION;
				break;

			case "status":
				type = TYPE_STATUS;
				break;

			case "reblog":
				type = TYPE_REPOST;
				break;

			case "follow":
				type = TYPE_FOLLOW;
				break;

			case "follow_request":
				type = TYPE_REQUEST;
				break;

			case "favourite":
				type = TYPE_FAVORITE;
				break;

			case "poll":
				type = TYPE_POLL;
				break;

			case "update":
				type = TYPE_UPDATE;
				break;
		}
		if (statusJson != null) {
			status = new MastodonStatus(statusJson, currentId);
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
	public int getType() {
		return type;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public User getUser() {
		return user;
	}


	@Nullable
	@Override
	public Status getStatus() {
		return status;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + id + " " + user;
	}


	@Override
	public int compareTo(Notification notification) {
		return Long.compare(notification.getTimestamp(), timestamp);
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Notification))
			return false;
		return ((Notification) obj).getId() == id;
	}
}