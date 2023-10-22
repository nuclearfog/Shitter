package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.EditedStatus;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.User;

/**
 * Mastodon implementation of {@link EditedStatus}
 *
 * @author nuclearfog
 */
public class EditedMastodonStatus implements EditedStatus {

	private static final long serialVersionUID = 7283516503545009772L;

	private long timestamp;
	private boolean sensitive;
	private boolean spoiler;
	private String text;
	private User author;
	private Poll poll;
	private Media[] medias = {};
	private Emoji[] emojis = {};

	/**
	 *
	 */
	public EditedMastodonStatus(JSONObject json, long currentUserId) throws JSONException {
		JSONObject pollJson = json.optJSONObject("poll");
		JSONArray mediaArray = json.optJSONArray("media_attachments");
		JSONArray emojiArray = json.optJSONArray("emojis");
		String content = json.optString("content", "");
		String spoilerText = json.optString("spoiler_text", "");
		text = StringUtils.extractText(content);
		spoiler = !content.equals(spoilerText);
		sensitive = json.optBoolean("sensitive", false);
		timestamp = StringUtils.getIsoTime(json.optString("created_at"));
		author = new MastodonUser(json.getJSONObject("account"), currentUserId);

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
		if (emojiArray != null && emojiArray.length() > 0) {
			emojis = new Emoji[emojiArray.length()];
			for (int i = 0; i < emojis.length; i++) {
				JSONObject emojiJson = emojiArray.getJSONObject(i);
				emojis[i] = new MastodonEmoji(emojiJson);
			}
		}
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public String getText() {
		return text;
	}


	@Override
	public User getAuthor() {
		return author;
	}


	@Override
	public boolean isSensitive() {
		return sensitive;
	}


	@Override
	public boolean isSpoiler() {
		return spoiler;
	}


	@Nullable
	@Override
	public Poll getPoll() {
		return poll;
	}


	@Override
	public Media[] getMedia() {
		return medias;
	}


	@Override
	public Emoji[] getEmojis() {
		return emojis;
	}
}