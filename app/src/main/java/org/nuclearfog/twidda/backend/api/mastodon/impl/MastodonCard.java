package org.nuclearfog.twidda.backend.api.mastodon.impl;

import android.util.Patterns;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Card;

/**
 * Card implementation for Mastodon
 *
 * @author nuclearfog
 */
public class MastodonCard implements Card {

	private static final long serialVersionUID = 8529350626123145616L;

	private String title;
	private String description;
	private String url;
	private String imageLink;

	/**
	 * @param json Mastodon card json
	 */
	public MastodonCard(JSONObject json) throws JSONException {
		String imageLink = json.optString("image", "");
		url = json.getString("url");
		title = json.optString("title", "");
		description = json.optString("description", "");
		if (Patterns.WEB_URL.matcher(imageLink).matches()) {
			this.imageLink = imageLink;
		} else {
			this.imageLink = "";
		}
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public String getUrl() {
		return url;
	}


	@Override
	public String getImageUrl() {
		return imageLink;
	}


	@NonNull
	@Override
	public String toString() {
		return "title=\"" + getTitle() + " \" description=\"" + getDescription() + "\"";
	}
}