package org.nuclearfog.twidda.backend.api.twitter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Location;

/**
 * API v1.1 implementation of Trend location
 *
 * @author nuclearfog
 */
public class LocationV1 implements Location {

	private static final long serialVersionUID = 4225779906497681090L;

	private int id;
	private String name;

	/**
	 * @param json JSON object containing location information
	 */
	public LocationV1(JSONObject json) {
		id = json.optInt("woeid", 1);
		String placeName = json.optString("name", "");
		String country = json.optString("country", "");

		if (!country.isEmpty() && !country.equals(placeName)) {
			name = country + ", " + placeName;
		} else {
			name = placeName;
		}
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Location))
			return false;
		return ((Location) obj).getId() == id;
	}

	@NonNull
	@Override
	public String toString() {
		return "id=" + id + " name=\"" + name + "\"";
	}
}