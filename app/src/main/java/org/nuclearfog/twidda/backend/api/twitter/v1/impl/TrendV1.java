package org.nuclearfog.twidda.backend.api.twitter.v1.impl;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Trend;

/**
 * API v 1.1 implementation for a twitter trend
 *
 * @author nuclearfog
 */
public class TrendV1 implements Trend {

	private static final long serialVersionUID = -2405773547644847221L;

	private int rank;
	private int popularity;
	private long locationId;
	private String name;

	/**
	 * @param json       JSON object containing trend information
	 * @param index      array index of this item
	 * @param locationId Id of the trend location
	 */
	public TrendV1(JSONObject json, int index, long locationId) {
		name = json.optString("name", "");
		popularity = json.optInt("tweet_volume", -1);
		this.locationId = locationId;
		this.rank = index + 1;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public long getLocationId() {
		return locationId;
	}


	@Override
	public int getRank() {
		return rank;
	}


	@Override
	public int getPopularity() {
		return popularity;
	}


	@Override
	public int compareTo(Trend trend) {
		return Integer.compare(rank, trend.getRank());
	}


	@NonNull
	@Override
	public String toString() {
		return "rank=" + rank + " name=\"" + name + "\"";
	}
}