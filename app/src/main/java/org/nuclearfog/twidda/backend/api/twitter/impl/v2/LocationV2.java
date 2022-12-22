package org.nuclearfog.twidda.backend.api.twitter.impl.v2;

import androidx.annotation.NonNull;

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

	/**
	 * parameter to add extra fields
	 */
	public static final String FIELDS_PLACE = "place.fields=country%2Ccountry_code%2Cfull_name%2Cgeo%2Cid%2Cname%2Cplace_type";

	/**
	 * geojson location type
	 */
	private static final String LOCATION_TYPE = "Point";

	private long id;
	private String fullName;
	private String country;
	private String name;
	private String coordinates = "";

	/**
	 * @param json "places" json format
	 */
	public LocationV2(JSONObject json) throws JSONException {
		JSONObject geoJson = json.optJSONObject("geo");
		String idStr = json.getString("id");
		fullName = json.optString("full_name", "");
		country = json.optString("country", "");
		name = json.optString("name", "");

		if (geoJson != null) {
			String type = geoJson.optString("type");
			if (LOCATION_TYPE.equals(type)) {
				JSONArray coordinateArray = geoJson.optJSONArray("coordinates");
				if (coordinateArray != null && coordinateArray.length() == 2) {
					double lon = coordinateArray.getDouble(0);
					double lat = coordinateArray.getDouble(1);
					coordinates = String.format(Locale.US, "%.6f,%.6f", lat, lon);
				}
			}
			// todo add other location types like bbox
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


	@NonNull
	@Override
	public String toString() {
		return "id=\"" + id + " full_name=" + fullName + " country=\"" + country + "\"";
	}
}