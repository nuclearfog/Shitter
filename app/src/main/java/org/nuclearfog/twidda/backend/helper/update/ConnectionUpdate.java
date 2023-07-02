package org.nuclearfog.twidda.backend.helper.update;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.helper.ConnectionResult;
import org.nuclearfog.twidda.config.Configuration;

import java.io.Serializable;

/**
 * Configuration class for {@link org.nuclearfog.twidda.backend.api.Connection}
 *
 * @author nuclearfog
 */
public class ConnectionUpdate implements Serializable {

	private static final long serialVersionUID = 2238181470544567706L;

	private Configuration apiConfig;
	private String hostname = "";
	private String consumerKey = "";
	private String consumerSecret = "";
	private String tempOauth = "";

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
	 * set custom oauth tokens
	 *
	 * @param consumerKey    custom oauth consumer key
	 * @param consumerSecret custom oauth consumer secret
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
	 * set temporary access tokens
	 *
	 * @param connectionResult connection containing temporary access tokens
	 */
	public void setConnection(@Nullable ConnectionResult connectionResult) {
		if (connectionResult != null) {
			tempOauth = connectionResult.getOauthToken();
			consumerKey = connectionResult.getConsumerKey();
			consumerSecret = connectionResult.getConsumerSecret();
		} else {
			tempOauth = "";
			consumerKey = "";
			consumerSecret = "";
		}
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