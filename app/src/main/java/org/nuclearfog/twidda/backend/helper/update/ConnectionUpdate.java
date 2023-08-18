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

	private Configuration apiConfig = Configuration.MASTODON;
	private String hostname = "";
	private String consumerKey = "";
	private String consumerSecret = "";
	private String appName = "";

	/**
	 * get host url used by the API
	 *
	 * @return hostname url
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * override host url
	 *
	 * @param hostname new host url
	 */
	public void setHostname(@NonNull String hostname) {
		if (!hostname.trim().isEmpty()) {
			if (!hostname.startsWith("http"))
				this.hostname = "https://" + hostname;
			else
				this.hostname = hostname;
		} else {
			this.hostname = "";
		}
	}

	/**
	 * get app name used by the API access
	 *
	 * @return short app name or empty if not set
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * set app name used by the API
	 *
	 * @param appname short app name
	 */
	public void setAppName(@NonNull String appname) {
		this.appName = appname;
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
	 * @return true if host url is set
	 */
	public boolean useHost() {
		return !hostname.trim().isEmpty();
	}

	/**
	 * set temporary access tokens
	 *
	 * @param connectionResult connection containing temporary access tokens
	 */
	public void setConnection(@Nullable ConnectionResult connectionResult) {
		if (connectionResult != null) {
			consumerKey = connectionResult.getConsumerKey();
			consumerSecret = connectionResult.getConsumerSecret();
		} else {
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