package org.nuclearfog.twidda.backend.utils;

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
	 * @return consumer API key
	 */
	public String getConsumerKey() {
		if (settings.isCustomApiSet())
			return settings.getConsumerKey();
		return API_KEY;
	}

	/**
	 * get consumer secret of the app
	 *
	 * @return consumer secret API key
	 */
	public String getConsumerSec() {
		if (settings.isCustomApiSet())
			return settings.getConsumerSecret();
		return API_SECRET;
	}
}