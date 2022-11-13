package org.nuclearfog.twidda.backend.api.twitter;

import android.content.Context;

import org.nuclearfog.twidda.database.GlobalSettings;

import io.michaelrocks.paranoid.Obfuscate;

/**
 * this class manages API and access tokens
 *
 * @author nuclearfog
 */
@Obfuscate
public class Tokens {

	/**
	 * false means there are no API keys available
	 * set to true when {@link #API_KEY} and {@link #API_SECRET} are set
	 */
	public static final boolean USE_DEFAULT_KEYS = false;

	/**
	 * consumer API key
	 */
	private static final String API_KEY = "";

	/**
	 * consumer API secret
	 */
	private static final String API_SECRET = "";


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
		if (settings.isCustomApiSet() && !forceDefault)
			return settings.getConsumerKey();
		return API_KEY;
	}

	/**
	 * get consumer secret of the app
	 *
	 * @param forceDefault use default API key
	 * @return consumer secret API key
	 */
	public String getConsumerSecret(boolean forceDefault) {
		if (settings.isCustomApiSet() && !forceDefault)
			return settings.getConsumerSecret();
		return API_SECRET;
	}
}