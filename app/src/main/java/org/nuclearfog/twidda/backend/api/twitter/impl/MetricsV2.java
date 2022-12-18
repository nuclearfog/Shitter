package org.nuclearfog.twidda.backend.api.twitter.impl;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Metrics;

/**
 * Implementation of {@link Metrics} using API V2
 *
 * @author nuclearfog
 */
public class MetricsV2 implements Metrics {

	private static final long serialVersionUID = -305086994844228862L;

	private long statusId;
	private int impressions;
	private int retweets;
	private int likes;
	private int replies;
	private int quotes;
	private int linkClicks;
	private int profileClicks;
	private int videoViews;

	/**
	 * @param metricsPublic    json of public metrics
	 * @param nonPublicMetrics json of non public metrics
	 * @param statusId         Id of the status
	 */
	public MetricsV2(JSONObject metricsPublic, JSONObject nonPublicMetrics, long statusId) {
		impressions = nonPublicMetrics.optInt("impression_count", 0);
		retweets = nonPublicMetrics.optInt("retweet_count", 0);
		likes = nonPublicMetrics.optInt("like_count", 0);
		replies = nonPublicMetrics.optInt("reply_count", 0);
		quotes = metricsPublic.optInt("quote_count", 0);
		linkClicks = nonPublicMetrics.optInt("url_link_clicks", 0);
		profileClicks = nonPublicMetrics.optInt("user_profile_clicks", 0);
		videoViews = nonPublicMetrics.optInt("view_count", 0);
		this.statusId = statusId;
	}


	@Override
	public long getStatusId() {
		return statusId;
	}


	@Override
	public int getViews() {
		return impressions;
	}


	@Override
	public int getReposts() {
		return retweets;
	}


	@Override
	public int getFavorits() {
		return likes;
	}


	@Override
	public int getReplies() {
		return replies;
	}


	@Override
	public int getQuoteCount() {
		return quotes;
	}


	@Override
	public int getLinkClicks() {
		return linkClicks;
	}


	@Override
	public int getProfileClicks() {
		return profileClicks;
	}


	@Override
	public int getVideoViews() {
		return videoViews;
	}
}