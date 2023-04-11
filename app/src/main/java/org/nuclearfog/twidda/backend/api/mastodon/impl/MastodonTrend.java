package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Trend;

/**
 * Trend implementation used for Mastodon API
 *
 * @author nuclearfog
 */
public class MastodonTrend implements Trend {

	private static final long serialVersionUID = 4328931229081239280L;

	private int popularity;
	private String name;

	/**
	 * @param json trend json object
	 */
	public MastodonTrend(JSONObject json) {
		JSONArray history = json.optJSONArray("history");
		name = '#' + json.optString("name", "");
		if (history != null && history.length() > 0) {
			JSONObject latest = history.optJSONObject(0);
			if (latest != null) {
				popularity = latest.optInt("uses", 0);
			}
		}
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public long getLocationId() {
		return -1L;
	}


	@Override
	public int getRank() {
		return -1;
	}


	@Override
	public int getPopularity() {
		return popularity;
	}


	@Override
	public int compareTo(Trend trend) {
		if (trend.getPopularity() > 0 && getPopularity() > 0)
			return Integer.compare(trend.getPopularity(), getPopularity());
		if (trend.getPopularity() > 0)
			return 1;
		if (getPopularity() > 0)
			return -1;
		return String.CASE_INSENSITIVE_ORDER.compare(getName(), trend.getName());
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + getName() + "\"";
	}
}