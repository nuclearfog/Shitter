package org.nuclearfog.twidda.backend.api.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Metrics;

/**
 * Implementation of {@link Metrics} using API V2
 *
 * @author nuclearfog
 */
public class MetricsImpl implements Metrics {

	public static final String PARAMS = "tweet.fields=organic_metrics";

	private int impressions;
	private int retweets;
	private int likes;
	private int replies;
	private int linkClicks;
	private int profileClicks;

	/**
	 * @param json tweet json object containing metrics information
	 */
	public MetricsImpl(JSONObject json) throws JSONException {
		JSONObject organic = json.getJSONObject("data").getJSONObject("organic_metrics");
		impressions = organic.optInt("impression_count", 0);
		retweets = organic.optInt("retweet_count", 0);
		likes = organic.optInt("like_count", 0);
		replies = organic.optInt("reply_count", 0);
		linkClicks = organic.optInt("url_link_clicks", 0);
		profileClicks = organic.optInt("user_profile_", 0);
	}

	@Override
	public int getViews() {
		return impressions;
	}

	@Override
	public int getRetweets() {
		return retweets;
	}

	@Override
	public int getLikes() {
		return likes;
	}

	@Override
	public int getReplies() {
		return replies;
	}

	@Override
	public int getLinkClicks() {
		return linkClicks;
	}

	@Override
	public int getProfileClicks() {
		return profileClicks;
	}
}