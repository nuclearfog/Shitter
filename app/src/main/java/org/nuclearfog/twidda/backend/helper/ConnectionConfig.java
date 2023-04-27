package org.nuclearfog.twidda.backend.helper;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.config.Configuration;

/**
 * Configuration class for {@link org.nuclearfog.twidda.backend.api.Connection}
 *
 * @author nuclearfog
 */
public class ConnectionConfig {

	private Configuration apiConfig;
	private String hostname = "";
	// these attributes below may be changed by another (background) thread
	private volatile String consumerKey = "";
	private volatile String consumerSecret = "";
	private volatile String tempOauth = "";

	/**
	 * get host url used by the API
	 *
	 * @return hostname url
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * get temporary oauth token
	 *
	 * @return oauth token
	 */
	public String getTempOauthToken() {
		return tempOauth;
	}

	/**
	 * get oauth consumer token
	 *
	 * @return oauth token
	 */
	public String getOauthConsumerToken() {
		return consumerKey;
	}

	/**
	 * get oauth token secret
	 *
	 * @return token secret
	 */
	public String getOauthTokenSecret() {
		return consumerSecret;
	}

	/**
	 * get configured API type
	 *
	 * @return API type
	 */
	public Configuration getApiType() {
		return apiConfig;
	}

	/**
	 * override default API type
	 */
	public void setApiType(Configuration apiConfig) {
		this.apiConfig = apiConfig;
	}

	/**
	 * override host url
	 *
	 * @param hostname new host url
	 */
	public void setHost(String hostname) {
		if (hostname != null && !hostname.trim().isEmpty() && !hostname.startsWith("http")) {
			this.hostname = "https://" + hostname;
		} else {
			this.hostname = "";
		}
	}

	/**
	 * @return true if token key pair is set
	 */
	public boolean useTokens() {
		return !consumerKey.trim().isEmpty() && !consumerSecret.trim().isEmpty();
	}

	/**
	 * @return true if host url is set
	 */
	public boolean useHost() {
		return !hostname.trim().isEmpty();
	}

	/**
	 * set oauth token key pair
	 *
	 * @param consumerKey    oauth token
	 * @param consumerSecret oauth token secret
	 */
	public void setOauthTokens(String consumerKey, String consumerSecret) {
		if (consumerKey != null) {
			this.consumerKey = consumerKey;
		}
		if (consumerSecret != null) {
			this.consumerSecret = consumerSecret;
		}
	}

	/**
	 * set temporary oauth token
	 *
	 * @param tempOauth oauth token
	 */
	public void setTempOauthToken(String tempOauth) {
		this.tempOauth = tempOauth;
	}


	@NonNull
	@Override
	public String toString() {
		String result = "";
		if (apiConfig != null) {
			switch (apiConfig) {
				case MASTODON:
					result = "network=\"Mastodon\"";
					break;

				case TWITTER1:
					result = "network=\"Twitter V1.1\"";
					break;

				case TWITTER2:
					result = "network=\"Twitter V2.0\"";
					break;
			}
		} else {
			result = "network=\"none\"";
		}
		if (useHost()) {
			result += " hostname=\"" + hostname + "\"";
		}
		return result;
	}
}