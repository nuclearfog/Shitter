package org.nuclearfog.twidda.config.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.WebPush;

/**
 * @author nuclearfog
 */
public class ConfigPush implements WebPush {

	private static final long serialVersionUID = -6942479639448210795L;

	private long id;
	private String host;
	private String serverKey, publicKey, privateKey, authKey;
	private boolean mentions, reposts, favorits, following, follow_request, status_post, status_change, poll_finished;
	private int policy;

	/**
	 * @param webPush web push instance to copy information
	 */
	public ConfigPush(WebPush webPush) {
		id = webPush.getId();
		host = webPush.getHost();
		serverKey = webPush.getServerKey();
		publicKey = webPush.getPublicKey();
		privateKey = webPush.getPrivateKey();
		authKey = webPush.getAuthSecret();
		mentions = webPush.alertMentionEnabled();
		reposts = webPush.alertRepostEnabled();
		favorits = webPush.alertFavoriteEnabled();
		following = webPush.alertFollowingEnabled();
		follow_request = webPush.alertFollowRequestEnabled();
		status_post = webPush.alertNewStatusEnabled();
		status_change = webPush.alertStatusChangeEnabled();
		poll_finished = webPush.alertPollEnabled();
		policy = webPush.getPolicy();
	}

	/**
	 *
	 */
	public ConfigPush(long id, String host, String serverKey, String publicKey, String privateKey, String authKey, int policy, boolean mentions, boolean reposts,
	                  boolean favorits, boolean following, boolean follow_request, boolean status_post, boolean status_change, boolean poll_finished) {
		this.id = id;
		this.host = host;
		this.policy = policy;
		this.serverKey = serverKey;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.authKey = authKey;
		this.mentions = mentions;
		this.reposts = reposts;
		this.favorits = favorits;
		this.following = following;
		this.follow_request = follow_request;
		this.status_post = status_post;
		this.status_change = status_change;
		this.poll_finished = poll_finished;
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
		return authKey;
	}


	@Override
	public boolean alertMentionEnabled() {
		return mentions;
	}


	@Override
	public boolean alertNewStatusEnabled() {
		return status_post;
	}


	@Override
	public boolean alertRepostEnabled() {
		return reposts;
	}


	@Override
	public boolean alertFollowingEnabled() {
		return following;
	}


	@Override
	public boolean alertFollowRequestEnabled() {
		return follow_request;
	}


	@Override
	public boolean alertFavoriteEnabled() {
		return favorits;
	}


	@Override
	public boolean alertPollEnabled() {
		return poll_finished;
	}


	@Override
	public boolean alertStatusChangeEnabled() {
		return status_change;
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
	 * clear user related information
	 */
	public void clear() {
		id = 0L;
		host = "";
		serverKey = "";
		publicKey = "";
		privateKey = "";
		authKey = "";
	}
}