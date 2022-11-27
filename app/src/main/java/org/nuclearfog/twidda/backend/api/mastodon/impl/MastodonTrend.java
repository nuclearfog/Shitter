package org.nuclearfog.twidda.backend.api.mastodon.impl;

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

	private int rank;
	private int popularity;
	private String name;


	public MastodonTrend(JSONObject json, int pos) {
		JSONArray history = json.optJSONArray("history");
		name = json.optString("name", "");
		rank = pos + 1;
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
	public int getLocationId() {
		return -1;
	}


	@Override
	public int getRank() {
		return rank;
	}


	@Override
	public int getPopularity() {
		return popularity;
	}
}