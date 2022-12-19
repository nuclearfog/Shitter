package org.nuclearfog.twidda.backend.api.twitter.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Location;

import java.util.Locale;

/**
 * location implementation for Twitter API V2
 *
 * @author nuclearfog
 */
public class LocationV2 implements Location {

	private static final long serialVersionUID = -8598770077546391747L;

	private long id;
	private String fullName;
	private String country;
	private String name;
	private String coordinates = "";


	public LocationV2(JSONObject json) throws JSONException {
		JSONObject coorindatesJson = json.optJSONObject("coordinates");
		String idStr = json.optString("place_id", "0");
		fullName = json.optString("full_name", "");
		country = json.optString("country", "");
		name = json.optString("name", "");

		if (coorindatesJson != null) {
			String type = coorindatesJson.optString("type");
			if (LOCATION_TYPE.equals(type)) {
				JSONArray coordinateArray = coorindatesJson.optJSONArray("coordinates");
				if (coordinateArray != null && coordinateArray.length() == 2) {
					double lon = coordinateArray.getDouble(0);
					double lat = coordinateArray.getDouble(1);
					coordinates = String.format(Locale.US, "%.6f,%.6f", lat, lon);
				}
			}
		}
		try {
			id = Long.parseUnsignedLong(idStr, 16);
		} catch (NumberFormatException e) {
			throw new JSONException("Bad ID: " + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public int getWorldId() {
		return 0;
	}


	@Override
	public String getCountry() {
		return country;
	}


	@Override
	public String getPlace() {
		return name;
	}


	@Override
	public String getCoordinates() {
		return coordinates;
	}


	@Override
	public String getFullName() {
		return fullName;
	}
}