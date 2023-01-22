package org.nuclearfog.twidda.backend.api.twitter;

import android.content.Context;

import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;

import io.michaelrocks.paranoid.Obfuscate;

/**
 * this class manages Twitter oauth 1.0 keys (consumer token & token secret) for API V1.1 & V2
 *
 * @author nuclearfog
 */
@Obfuscate
public class Tokens {

	/**
	 * false means there are no API keys available
	 * set to true when {@link #CONSUMER_TOKEN} and {@link #TOKEN_SECRET} are set
	 */
	public static final boolean USE_DEFAULT_KEYS = true;

	/**
	 * for compability mode disable Twitter API version 2.0 (use only version 1.1) if the API access don't support this.
	 */
	public static final boolean DISABLE_API_V2 = true;

	/**
	 * add here your consumer token
	 */
	private static final String CONSUMER_TOKEN = "";

	/**
	 * add here your token secret
	 */
	private static final String TOKEN_SECRET = "";


	private static Tokens instance;
	private GlobalSettings settings;

	/**
	 *
	 */
	private Tokens(Context context) {
		settings = GlobalSettings.getInstance(context);
	}

	/**
	 * get singleton instance
	 *
	 * @return instance of this class
	 */
	public static Tokens getInstance(Context context) {
		if (instance == null)
			instance = new Tokens(context);
		return instance;
	}

	/**
	 * get consumer key of the app
	 *
	 * @param forceDefault use default API key
	 * @return consumer API key
	 */
	public String getConsumerKey(boolean forceDefault) {
		Account login = settings.getLogin();
		if (login.usingDefaultTokens() || forceDefault)
			return CONSUMER_TOKEN;
		return login.getConsumerToken();
	}

	/**
	 * get consumer secret of the app
	 *
	 * @param forceDefault use default API key
	 * @return consumer secret API key
	 */
	public String getConsumerSecret(boolean forceDefault) {
		Account login = settings.getLogin();
		if (login.usingDefaultTokens() || forceDefault)
			return TOKEN_SECRET;
		return login.getConsumerSecret();
	}

	/**
	 * @return true to use Twitte rAPI v2
	 */
	public boolean useAPIv2() {
		if (USE_DEFAULT_KEYS)
			return !DISABLE_API_V2;
		return settings.isTwitterV2Enabled();
	}
}