package org.nuclearfog.twidda.backend.helper;

import androidx.annotation.NonNull;

/**
 * Configuration class for {@link org.nuclearfog.twidda.backend.api.Connection}
 *
 * @author nuclearfog
 */
public class ConnectionConfig {

	/**
	 * Twitter API version 1.1
	 */
	public static final int API_TWITTER_1 = 1;

	/**
	 * Twitter API version 2.0
	 */
	public static final int API_TWITTER_2 = 2;

	/**
	 * Mastodon API
	 */
	public static final int API_MASTODON = 3;

	private String consumerKey;
	private String consumerSecret;
	private String hostname;
	private int apiType;

	/**
	 * @param apiType default API type
	 */
	public ConnectionConfig(int apiType) {
		hostname = "";
		consumerKey = "";
		consumerSecret = "";
		this.apiType = apiType;
	}

	/**
	 * get host url used by the API
	 *
	 * @return hostname url
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * get oauth consumer token
	 *
	 * @return consumer token
	 */
	public String getOauthToken() {
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
	 * @return API type {@link #API_MASTODON,#API_TWITTER_1,#API_TWITTER_2}
	 */
	public int getApiType() {
		return apiType;
	}

	/**
	 * override default API type
	 *
	 * @param apiType API type {@link #API_MASTODON,#API_TWITTER_1,#API_TWITTER_2}
	 */
	public void setApiType(int apiType) {
		this.apiType = apiType;
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
	 * @param consumerKey oauth token
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


	@NonNull
	@Override
	public String toString() {
		String result= "network=\"";
		switch (apiType) {
			case API_MASTODON:
				result = "Mastodon\"";
				break;

			case API_TWITTER_1:
				result = "Twitter V1.1\"";
				break;

			case API_TWITTER_2:
				result = "Twitter V2.0\"";
				break;
		}
		if (useHost()) {
			result = " hostname=\"" + hostname + "\"";
		}
		return result;
	}
}