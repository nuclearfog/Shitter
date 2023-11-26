package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.Instance;

/**
 * Mastodon instance information containing configuration
 *
 * @author nuclearfog
 */
public class MastodonInstance implements Instance {

	private static final long serialVersionUID = 1312362549415764187L;

	private String title;
	private String domain;
	private String description;
	private String version;
	private String bannerImage;
	private String mail;
	private String[] mimeTypes;
	private String[] languages = {};
	private long timestamp;
	private int maxTagFeature;
	private int maxCharacters;
	private int maxImages;
	private int maxImageSize;
	private int maxVideoSize;
	private int maxPollOptions;
	private int maxOptionTitleLength;
	private int minPollDuration;
	private int maxPollDuration;
	private int activeUsers;
	private boolean translationSupported;

	/**
	 *
	 */
	public MastodonInstance(JSONObject json) throws JSONException {
		JSONObject configuration = json.getJSONObject("configuration");
		JSONObject accounts = configuration.getJSONObject("accounts");
		JSONObject statuses = configuration.getJSONObject("statuses");
		JSONObject media = configuration.getJSONObject("media_attachments");
		JSONObject polls = configuration.getJSONObject("polls");
		JSONObject translations = configuration.getJSONObject("translation");
		JSONArray mediaTypes = media.getJSONArray("supported_mime_types");
		JSONArray language = json.getJSONArray("languages");
		JSONObject thumbnail = json.optJSONObject( "thumbnail");
		JSONObject contact = json.optJSONObject("contact");
		JSONObject usage = json.optJSONObject("usage");

		timestamp = System.currentTimeMillis();
		title = json.getString("title");
		domain = json.getString("domain");
		description = json.getString("description");
		version = json.getString("version");
		maxTagFeature = accounts.getInt("max_featured_tags");
		maxCharacters = statuses.getInt("max_characters");
		maxImages = statuses.getInt("max_media_attachments");
		maxImageSize = media.getInt("image_size_limit");
		maxVideoSize = media.getInt("video_size_limit");
		maxPollOptions = polls.getInt("max_options");
		maxOptionTitleLength = polls.getInt("max_characters_per_option");
		minPollDuration = polls.getInt("min_expiration");
		maxPollDuration = polls.getInt("max_expiration");
		translationSupported = translations.getBoolean("enabled");

		mimeTypes = new String[mediaTypes.length()];
		for (int i = 0; i < mimeTypes.length; i++) {
			mimeTypes[i] = mediaTypes.getString(i);
		}
		languages = new String[language.length()];
		for (int i = 0; i < languages.length; i++) {
			languages[i] = language.getString(i);
		}
		if (!domain.startsWith("http")) {
			domain = "https://" + domain;
		}
		if (thumbnail != null) {
			bannerImage = thumbnail.optString("url", "");
		}
		if (contact != null) {
			mail = contact.optString("email", "");
		}
		if (usage != null && usage.has("users")) {
			activeUsers = usage.getJSONObject("users").optInt("active_month");
		}
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public String getDomain() {
		return domain;
	}


	@Override
	public String getVersion() {
		return version;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public String getBannerImageUrl() {
		return bannerImage;
	}


	@Override
	public String getMail() {
		return mail;
	}


	@Override
	public String[] getLanguages() {
		return languages;
	}


	@Override
	public int getActiveUsers() {
		return activeUsers;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public int getTagFollowLimit() {
		return maxTagFeature;
	}


	@Override
	public int getStatusCharacterLimit() {
		return maxCharacters;
	}

	@Override
	public int getImageLimit() {
		return maxImages;
	}


	@Override
	public int getVideoLimit() {
		return 1;
	}


	@Override
	public int getGifLimit() {
		return 1;
	}


	@Override
	public int getAudioLimit() {
		return 1;
	}


	@Override
	public String[] getSupportedFormats() {
		return mimeTypes;
	}


	@Override
	public int getImageSizeLimit() {
		return maxImageSize;
	}


	@Override
	public int getGifSizeLimit() {
		return maxImageSize;
	}


	@Override
	public int getVideoSizeLimit() {
		return maxVideoSize;
	}


	@Override
	public int getAudioSizeLimit() {
		return 40000000;
	}


	@Override
	public int getPollOptionsLimit() {
		return maxPollOptions;
	}


	@Override
	public int getPollOptionCharacterLimit() {
		return maxOptionTitleLength;
	}


	@Override
	public int getMinPollDuration() {
		return minPollDuration;
	}


	@Override
	public int getMaxPollDuration() {
		return maxPollDuration;
	}


	@Override
	public boolean isTranslationSupported() {
		return translationSupported;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Instance))
			return false;
		Instance instance = (Instance) obj;
		return instance.getDomain().equals(getDomain()) && instance.getTimestamp() == getTimestamp();
	}


	@NonNull
	@Override
	public String toString() {
		return "domain=\"" + getDomain() + " \" version=\"" + getVersion() + "\"";
	}
}