package org.nuclearfog.twidda.backend.api.twitter.impl.v2.maps;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.twitter.impl.v2.UserV2;
import org.nuclearfog.twidda.model.User;

import java.util.TreeMap;

/**
 * This class keeps references to {@link UserV2} so multiple tweets with the same author can use the same {@link User} reference
 *
 * @author nuclearfog
 */
public class UserV2Map extends TreeMap<Long, User> {

	private static final long serialVersionUID = 3107064180725473583L;

	/**
	 * @param json      json object from a tweet
	 * @param twitterId current user's ID
	 */
	public UserV2Map(JSONObject json, long twitterId) throws JSONException {
		JSONObject includesJson = json.getJSONObject("includes");
		JSONArray userArray = includesJson.getJSONArray("users");
		for (int i = 0; i < userArray.length(); i++) {
			JSONObject item = userArray.getJSONObject(i);
			User user = new UserV2(item, twitterId);
			put(user.getId(), user);
		}
	}


	@NonNull
	@Override
	public String toString() {
		return "size=" + size();
	}
}