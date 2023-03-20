package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nuclearfog.twidda.model.Translation;

import java.util.Locale;

/**
 * Mastodon implementation of a {@link org.nuclearfog.twidda.model.Status} translation
 *
 * @author nuclearfog
 */
public class MastodonTranslation implements Translation {

	private static final long serialVersionUID = 5431861840189539763L;

	private String text;
	private String source;
	private String language;

	/**
	 *
	 */
	public MastodonTranslation(JSONObject json) {
		String text = json.optString("content", "");
		source = json.optString("provider", "");
		String lang = json.optString("detected_source_language", "");
		Locale location = new Locale(lang);
		language = location.getDisplayLanguage(location);
		this.text = Jsoup.parse(text).text();
	}


	@Override
	public String getText() {
		return text;
	}


	@Override
	public String getSource() {
		return source;
	}


	@Override
	public String getOriginalLanguage() {
		return language;
	}
}