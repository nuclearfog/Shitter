package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Tag;

/**
 * Mastodon {@link Tag} implementation
 *
 * @author nuclearfog
 */
public class MastodonTag implements Tag {

	private static final long serialVersionUID = 4328931229081239280L;

	private int popularity;
	private String name;
	private boolean following;
	private long id;
	private int rank;

	/**
	 * @param json trend json object
	 */
	public MastodonTag(JSONObject json) {
		JSONArray history = json.optJSONArray("history");
		String idStr = json.optString("id", "0");
		name = '#' + json.optString("name", "");
		following = json.optBoolean("following", false);
		if (history != null && history.length() > 0) {
			JSONObject latest = history.optJSONObject(0);
			if (latest != null) {
				popularity = latest.optInt("uses", 0);
			}
		} else {
			popularity = json.optInt("statuses_count", 0);
		}
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException exception) {
			// proceed without ID
		}
	}


	@Override
	public long getId() {
		return id;
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
		return rank;
	}


	@Override
	public int getPopularity() {
		return popularity;
	}


	@Override
	public boolean following() {
		return following;
	}

	/**
	 *
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Tag))
			return false;
		Tag tag = (Tag) obj;
		return getName().equals(tag.getName()) && getLocationId() == tag.getLocationId();
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + getName() + "\"";
	}
}