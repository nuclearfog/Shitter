package org.nuclearfog.twidda.backend.api.twitter.impl.v2.maps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.twitter.impl.v2.MediaV2;
import org.nuclearfog.twidda.model.Media;

import java.util.TreeMap;

/**
 * this class keeps references to Twitter {@link Media} so multiple tweets can use a single reference
 *
 * @author nuclerfog
 */
public class MediaV2Map extends TreeMap<String, Media> {

	private static final long serialVersionUID = 6833225544590465562L;

	/**
	 * @param json json object from a tweet
	 */
	public MediaV2Map(JSONObject json) throws JSONException {
		JSONObject includesJson = json.getJSONObject("includes");
		JSONArray mediaArray = includesJson.optJSONArray("media");
		if (mediaArray != null) {
			for (int i = 0; i < mediaArray.length(); i++) {
				JSONObject item = mediaArray.getJSONObject(i);
				Media media = new MediaV2(item);
				put(media.getKey(), media);
			}
		}
	}
}