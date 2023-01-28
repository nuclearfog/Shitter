package org.nuclearfog.twidda.backend.update;

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

	private String oauthToken1;
	private String oauthToken2;
	private String hostname;
	private int apiType;

	/**
	 * @param apiType default API type
	 */
	public ConnectionConfig(int apiType) {
		hostname = "";
		oauthToken1 = "";
		oauthToken2 = "";
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
		return oauthToken1;
	}

	/**
	 * get oauth token secret
	 *
	 * @return token secret
	 */
	public String getOauthTokenSecret() {
		return oauthToken2;
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
		return !oauthToken1.trim().isEmpty() && !oauthToken2.trim().isEmpty();
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
	 * @param oauthToken1 oauth token
	 * @param oauthToken2 oauth token secret
	 */
	public void setOauthTokens(String oauthToken1, String oauthToken2) {
		if (oauthToken1 != null)
			this.oauthToken1 = oauthToken1;
		if (oauthToken2 != null)
			this.oauthToken2 = oauthToken2;
	}
}