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
	private String endpoint;
	private String serverKey, publicKey, privateKey, authKey;

	/**
	 * @param webPush web push instance to copy information
	 */
	public ConfigPush(WebPush webPush) {
		id = webPush.getId();
		endpoint = webPush.getEndpoint();
		serverKey = webPush.getServerKey();
		publicKey = webPush.getPublicKey();
		privateKey = webPush.getPrivateKey();
		authKey = webPush.getAuthSecret();
	}

	/**
	 *
	 */
	public ConfigPush(long id, String endpoint, String serverKey, String publicKey, String privateKey, String authKey) {
		this.id = id;
		this.endpoint = endpoint;
		this.serverKey = serverKey;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.authKey = authKey;
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getEndpoint() {
		return endpoint;
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
		return false;
	}


	@Override
	public boolean alertStatusEnabled() {
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


	@NonNull
	@Override
	public String toString() {
		return "id=" + getId() + " url=\"" + getEndpoint() + "\"";
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof WebPush))
			return false;
		WebPush push = (WebPush) obj;
		return getId() == push.getId() && getEndpoint().equals(push.getEndpoint());
	}
}