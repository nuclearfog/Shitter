package org.nuclearfog.twidda.backend.api.mastodon.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.ScheduledStatus;
import org.nuclearfog.twidda.model.Status;

/**
 * Mastodon implementation of a scheduled status
 *
 * @author nuclearfog
 */
public class ScheduledMastodonStatus implements ScheduledStatus {

	private static final long serialVersionUID = -1340937182294786469L;

	private long id;
	private long time;
	private String text;
	private String language = "";
	private int visibility;
	private boolean sensitive;
	private boolean spoiler;
	private Media[] medias = {};
	private Poll poll;

	/**
	 *
	 */
	public ScheduledMastodonStatus(JSONObject json) throws JSONException {
		JSONObject params = json.getJSONObject("params");
		JSONObject pollJson = params.optJSONObject("poll");
		JSONArray mediaArray = json.optJSONArray("media_attachments");
		String idStr = json.getString("id");
		String visibilityStr = json.getString("visibility");
		text = StringUtils.extractText(json.optString("text", ""));
		time = StringUtils.getIsoTime(json.optString("scheduled_at", ""));
		sensitive = params.optBoolean("sensitive", false);
		spoiler = params.optBoolean("spoiler_text", false);

		if (!params.isNull("language")) {
			language = params.optString("language");
		}
		if (pollJson != null) {
			poll = new MastodonPoll(pollJson);
		}
		if (mediaArray != null && mediaArray.length() > 0) {
			medias = new Media[mediaArray.length()];
			for (int i = 0; i < mediaArray.length(); i++) {
				JSONObject mediaItem = mediaArray.getJSONObject(i);
				medias[i] = new MastodonMedia(mediaItem);
			}
		}
		switch (visibilityStr) {
			case "public":
				visibility = Status.VISIBLE_PUBLIC;
				break;

			case "private":
				visibility = Status.VISIBLE_PRIVATE;
				break;

			case "direct":
				visibility = Status.VISIBLE_DIRECT;
				break;

			case "unlisted":
				visibility = Status.VISIBLE_UNLISTED;
				break;

			default:
				visibility = Status.VISIBLE_DEFAULT;
				break;
		}
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException exception) {
			throw new JSONException("Bad ID: " + idStr);
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public long getPublishTime() {
		return time;
	}


	@Override
	public String getText() {
		return text;
	}


	@Override
	public String getLanguage() {
		return language;
	}


	@Override
	public Media[] getMedia() {
		return medias;
	}


	@Override
	public Poll getPoll() {
		return poll;
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
	public boolean isSpoiler() {
		return spoiler;
	}
}