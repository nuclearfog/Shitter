package org.nuclearfog.twidda.backend.api.twitter.impl.v1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Location;

import java.util.Locale;

/**
 * API v1.1 implementation of Trend location
 *
 * @author nuclearfog
 */
public class LocationV1 implements Location {

	private static final long serialVersionUID = 4225779906497681090L;

	/**
	 * geojson location type
	 */
	private static final String LOCATION_TYPE = "Point";

	private int woeid;
	private String country;
	private String place;
	private String fullName;
	private String coordinates = "";

	/**
	 * @param json JSON object containing location information
	 */
	public LocationV1(JSONObject json) throws JSONException {
		JSONObject coordinateJson = json.optJSONObject("coordinates");
		woeid = json.optInt("woeid", 1);
		place = json.optString("name", "");
		fullName = json.optString("full_name", "");
		country = json.optString("country", "");

		if (coordinateJson != null) {
			if (LOCATION_TYPE.equals(coordinateJson.optString("type"))) {
				JSONArray coordinateArray = coordinateJson.optJSONArray("coordinates");
				if (coordinateArray != null && coordinateArray.length() == 2) {
					double lon = coordinateArray.getDouble(0);
					double lat = coordinateArray.getDouble(1);
					coordinates = String.format(Locale.US, "%.6f,%.6f", lat, lon);
				}
			}
		}
	}


	@Override
	public long getId() {
		return woeid;
	}


	@Override
	public String getCountry() {
		return country;
	}


	@Override
	public String getPlace() {
		return place;
	}


	@Override
	public String getCoordinates() {
		return coordinates;
	}


	@Override
	public String getFullName() {
		if (!fullName.isEmpty())
			return fullName;
		if (!country.isEmpty() && !country.equals(place))
			return country + ", " + place;
		return place;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Location))
			return false;
		return ((Location) obj).getId() == woeid;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + woeid + " name=\"" + getFullName() + "\"";
	}
}