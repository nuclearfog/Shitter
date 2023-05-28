package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
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
	private String serverKey, publicKey, privateKey, authSec;
	private int policy;

	/**
	 * @param json web push json object
	 */
	public MastodonPush(JSONObject json) throws JSONException {
		String id = json.getString("id");
		host = json.getString("endpoint");
		serverKey = json.getString("server_key");
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
		return false;
	}


	@Override
	public boolean alertStatusPostEnabled() {
		return false;
	}


	@Override
	public boolean alertRepostEnabled() {
		return false;
	}


	@Override
	public boolean alertFollowingEnabled() {
		return false;
	}


	@Override
	public boolean alertFollowRequestEnabled() {
		return false;
	}


	@Override
	public boolean alertFavoriteEnabled() {
		return false;
	}


	@Override
	public boolean alertPollEnabled() {
		return false;
	}


	@Override
	public boolean alertStatusChangeEnabled() {
		return false;
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