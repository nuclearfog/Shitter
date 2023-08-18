package org.nuclearfog.twidda.backend.helper;

/**
 * This class contains temporary access tokens and authorization url used to login app
 *
 * @author nuclearfog
 */
public class ConnectionResult {

	private String consumerKey;
	private String consumerSecret;
	private String authorizationUrl;

	/**
	 * @param authorizationUrl authorization url used to redirect to login page
	 * @param consumerKey      temporary generated app consumer token
	 * @param consumerSecret   temporary generated app consumer secret
	 */
	public ConnectionResult(String authorizationUrl, String consumerKey, String consumerSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.authorizationUrl = authorizationUrl;
	}

	/**
	 * get API consumer token
	 */
	public String getConsumerKey() {
		return consumerKey;
	}

	/**
	 * get API consumer secret
	 */
	public String getConsumerSecret() {
		return consumerSecret;
	}

	/**
	 * get authorization url (link to login page)
	 */
	public String getAuthorizationUrl() {
		return authorizationUrl;
	}
}