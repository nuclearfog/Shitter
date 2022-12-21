package org.nuclearfog.twidda.backend.api.twitter.impl.v2.maps;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.twitter.impl.v2.PollV2;
import org.nuclearfog.twidda.model.Poll;

import java.util.TreeMap;

/**
 * This class keeps references to {@link Poll} so multiple tweets can use a single reference
 *
 * @author nuclearfog
 */
public class PollV2Map extends TreeMap<Long, Poll> {

	private static final long serialVersionUID = 4594328734611101686L;

	/**
	 * @param json json object from a tweet
	 */
	public PollV2Map(JSONObject json) throws JSONException {
		JSONObject includesJson = json.getJSONObject("includes");
		JSONArray pollsArray = includesJson.optJSONArray("polls");
		if (pollsArray != null) {
			for (int i = 0; i < pollsArray.length(); i++) {
				JSONObject item = pollsArray.getJSONObject(i);
				Poll poll = new PollV2(item);
				put(poll.getId(), poll);
			}
		}
	}


	@NonNull
	@Override
	public String toString() {
		return "size=" + size();
	}
}