package org.nuclearfog.twidda.backend.api.twitter.v2.maps;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.twitter.v2.impl.LocationV2;
import org.nuclearfog.twidda.model.Location;

import java.util.TreeMap;

/**
 * This class keeps references to {@link Location} so multiple tweets can use a single reference
 *
 * @author nuclearfog
 */
public class LocationV2Map extends TreeMap<Long, Location> {

	private static final long serialVersionUID = -4697349855656098425L;

	/**
	 * @param json json tweet object
	 */
	public LocationV2Map(JSONObject json) throws JSONException {
		JSONObject includesJson = json.getJSONObject("includes");
		JSONArray placesArray = includesJson.optJSONArray("places");
		if (placesArray != null) {
			for (int i = 0; i < placesArray.length(); i++) {
				JSONObject placeJson = placesArray.getJSONObject(i);
				Location location = new LocationV2(placeJson);
				put(location.getId(), location);
			}
		}
	}


	@NonNull
	@Override
	public String toString() {
		return "size=" + size();
	}
}