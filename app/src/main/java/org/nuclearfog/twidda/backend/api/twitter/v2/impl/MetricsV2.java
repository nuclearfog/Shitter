package org.nuclearfog.twidda.backend.api.twitter.v2.impl;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Metrics;

/**
 * Implementation of {@link Metrics} using API V2
 *
 * @author nuclearfog
 */
public class MetricsV2 implements Metrics {

	private static final long serialVersionUID = -305086994844228862L;

	private int impressions;
	private int quotes;
	private int linkClicks;
	private int profileClicks;
	private int videoViews;

	/**
	 * @param metricsPublic    json of public metrics
	 * @param nonPublicMetrics json of non public metrics
	 */
	public MetricsV2(JSONObject metricsPublic, JSONObject nonPublicMetrics) {
		impressions = nonPublicMetrics.optInt("impression_count", 0);
		quotes = metricsPublic.optInt("quote_count", 0);
		linkClicks = nonPublicMetrics.optInt("url_link_clicks", 0);
		profileClicks = nonPublicMetrics.optInt("user_profile_clicks", 0);
		videoViews = nonPublicMetrics.optInt("view_count", 0);
	}


	@Override
	public int getViews() {
		return impressions;
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


	@NonNull
	@Override
	public String toString() {
		return "impressions=" + getViews() + " profile_clicks=" + getProfileClicks() +
				" link_clicks=" + getLinkClicks() + " quotes=" + getQuoteCount() + "video_views=" + getVideoViews();
	}
}