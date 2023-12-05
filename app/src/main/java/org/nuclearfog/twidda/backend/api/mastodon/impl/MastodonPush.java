package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.WebPush;

/**
 * Mastodon push implementation
 *
 * @author nuclearfog
 */
public class MastodonPush implements WebPush {

	private static final long serialVersionUID = 565081495547561476L;

	private long id;
	private String host;
	private String instance;
	private String serverKey, publicKey, privateKey, authSec;
	private boolean mentionAlert, favoriteAlert, repostAlert, newPostAlert, newFollowerAlert, followRequestAlert, pollEndAlert, statusChangeAlert;
	private int policy;

	/**
	 * @param json    web push json object
	 * @param account current user information
	 */
	public MastodonPush(JSONObject json, Account account) throws JSONException {
		JSONObject alerts = json.getJSONObject("alerts");
		String id = json.getString("id");
		host = json.getString("endpoint");
		serverKey = json.getString("server_key");
		mentionAlert = alerts.optBoolean("mention", false);
		favoriteAlert = alerts.optBoolean("favourite", false);
		repostAlert = alerts.optBoolean("reblog", false);
		newPostAlert = alerts.optBoolean("status", false);
		newFollowerAlert = alerts.optBoolean("follow", false);
		followRequestAlert = alerts.optBoolean("follow_request", false);
		pollEndAlert = alerts.optBoolean("poll", false);
		statusChangeAlert = alerts.optBoolean("update", false);
		instance = StringUtils.getPushInstanceHash(account);
		try {
			this.id = Long.parseLong(id);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID: " + id);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getHost() {
		return host;
	}


	@Override
	public String getInstance() {
		return instance;
	}


	@Override
	public String getServerKey() {
		return serverKey;
	}


	@Override
	public String getPublicKey() {
		return publicKey;
	}


	@Override
	public String getPrivateKey() {
		return privateKey;
	}


	@Override
	public String getAuthSecret() {
		return authSec;
	}


	@Override
	public boolean alertMentionEnabled() {
		return mentionAlert;
	}


	@Override
	public boolean alertNewStatusEnabled() {
		return newPostAlert;
	}


	@Override
	public boolean alertRepostEnabled() {
		return repostAlert;
	}


	@Override
	public boolean alertFollowingEnabled() {
		return newFollowerAlert;
	}


	@Override
	public boolean alertFollowRequestEnabled() {
		return followRequestAlert;
	}


	@Override
	public boolean alertFavoriteEnabled() {
		return favoriteAlert;
	}


	@Override
	public boolean alertPollEnabled() {
		return pollEndAlert;
	}


	@Override
	public boolean alertStatusChangeEnabled() {
		return statusChangeAlert;
	}


	@Override
	public int getPolicy() {
		return policy;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + getId() + " url=\"" + getHost() + "\"";
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof WebPush))
			return false;
		WebPush push = (WebPush) obj;
		return getId() == push.getId() && getHost().equals(push.getHost());
	}

	/**
	 * set encryption keys
	 */
	public void setKeys(String publicKey, String privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/**
	 * set auth key
	 */
	public void setAuthSecret(String authSec) {
		this.authSec = authSec;
	}

	/**
	 * set push policy
	 */
	public void setPolicy(int policy) {
		this.policy = policy;
	}
}