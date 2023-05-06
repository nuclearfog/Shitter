package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Location;
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
	private boolean following;

	/**
	 * @param json trend json object
	 */
	public MastodonTrend(JSONObject json) {
		JSONArray history = json.optJSONArray("history");
		name = '#' + json.optString("name", "");
		following = json.optBoolean("following", false);
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
		return Location.NO_ID;
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
	public boolean following() {
		return following;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Trend))
			return false;
		Trend trend = (Trend) obj;
		return getName().equals(trend.getName()) && getLocationId() == trend.getLocationId();
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + getName() + "\"";
	}
}