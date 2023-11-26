package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Reaction;

/**
 * @author nuclearfog
 */
public class MastodonReaction implements Reaction {

	private static final long serialVersionUID = 6127823131972079805L;

	private String name, url;
	private int count;
	private boolean selected;

	/**
	 *
	 */
	public MastodonReaction(JSONObject json) throws JSONException {
		name = json.getString("name");
		count = json.optInt("count", 0);
		selected = json.optBoolean("me", false);
		url = json.optString("static_url", "");
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public String getImageUrl() {
		return url;
	}


	@Override
	public int getCount() {
		return count;
	}


	@Override
	public boolean isSelected() {
		return selected;
	}
}