package org.nuclearfog.twidda.backend.api.mastodon.impl;

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


	public MastodonCard(JSONObject json) {
		title = json.optString("title", "");
		description = json.optString("description", "");
		imageLink = json.optString("image", "");
		url = json.optString("url");
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
}