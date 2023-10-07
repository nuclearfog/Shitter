package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Credentials;
import org.nuclearfog.twidda.model.User.Field;

/**
 * Mastodon implementation of {@link Credentials}
 *
 * @author nuclearfog
 */
public class MastodonCredentials implements Credentials {

	private static final long serialVersionUID = 4255988093112848364L;

	private long id;
	private String username;
	private String description;
	private String language;
	private int visibility;
	private boolean sensitive;

	/**
	 * @param json Credentials json format
	 */
	public MastodonCredentials(JSONObject json) throws JSONException {
		JSONObject sourceJson = json.getJSONObject("source");
		String idStr = json.getString("id");
		String visStr = sourceJson.optString("privacy", "");
		username = json.getString("display_name");
		description = sourceJson.getString("note");
		language = sourceJson.getString("language");
		sensitive = sourceJson.getBoolean("sensitive");

		switch(visStr) {
			case "public":
				visibility = PUBLIC;
				break;

			case "private":
				visibility = PRIVATE;
				break;

			case "direct":
				visibility = DIRECT;
				break;

			case "unlisted":
				visibility = UNLISTED;
				break;

			default:
				visibility = DEFAULT;
				break;
		}
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad user ID:" + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getUsername() {
		return username;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public String getLanguage() {
		return language;
	}


	@Override
	public int getVisibility() {
		return visibility;
	}


	@Override
	public boolean isSensitive() {
		return sensitive;
	}


	@Override
	public Field[] getFields() {
		return new Field[0];
	}
}