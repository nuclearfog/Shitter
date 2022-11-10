package org.nuclearfog.twidda.backend.api.twitter.impl;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Trend;

/**
 * API v 1.1 implementation for a twitter trend
 *
 * @author nuclearfog
 */
public class TrendV1 implements Trend {

	private int rank;
	private int popularity;
	private String name;

	/**
	 * @param json JSON object containing trend information
	 * @param rank position of the trend starting with '1'
	 */
	public TrendV1(JSONObject json, int rank) {
		name = json.optString("name", "");
		popularity = json.optInt("tweet_volume", -1);
		this.rank = rank;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public int getPopularity() {
		return popularity;
	}

	@NonNull
	@Override
	public String toString() {
		return "rank=" + rank + " name=\"" + name + "\"";
	}
}