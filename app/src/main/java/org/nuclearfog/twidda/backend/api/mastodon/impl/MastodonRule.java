package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Rule;

/**
 * Mastodon implementation of a {@link Rule}
 *
 * @author nuclearfog
 */
public class MastodonRule implements Rule {

	private static final long serialVersionUID = 735539108133555221L;

	private long id;
	private String description;

	/**
	 *
	 */
	public MastodonRule(JSONObject json) throws JSONException {
		String idStr = json.getString("id");
		description = json.getString("text");
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException exception) {
			throw new JSONException("bad ID: " + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Rule))
			return false;
		return ((Rule) obj).getId() == getId();
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + getId() + " description=\"" + getDescription() + "\"";
	}
}