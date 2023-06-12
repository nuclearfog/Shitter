package org.nuclearfog.twidda.backend.api.mastodon.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Safelist;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Metrics;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

import java.util.Locale;

/**
 * Status implementation for Mastodon
 *
 * @author nuclearfog
 */
public class MastodonStatus implements Status {

	private static final long serialVersionUID = 1184375228249441241L;

	private static final OutputSettings OUTPUT_SETTINGS = new OutputSettings().prettyPrint(false);

	private long id;
	private long replyId;
	private long replyUserId;
	private long createdAt;

	private int replyCount;
	private int favoriteCount;
	private int reblogCount;
	private int visibility;
	private boolean favorited;
	private boolean reposted;
	private boolean bookmarked;
	private boolean sensitive;
	private boolean spoiler;
	private boolean muted;

	private String text;
	private String mentions = "";
	private String language = "";
	private String source = "";
	private String url = "";

	private User author;
	private Poll poll;
	private Status embeddedStatus;
	private Card[] cards = {};
	private Media[] medias = {};
	private Emoji[] emojis = {};

	/**
	 * @param json          Mastodon status json object
	 * @param currentUserId Id of the current user
	 */
	public MastodonStatus(JSONObject json, long currentUserId) throws JSONException {
		JSONObject embeddedJson = json.optJSONObject("reblog");
		JSONObject appJson = json.optJSONObject("application");
		JSONObject cardJson = json.optJSONObject("card");
		JSONObject pollJson = json.optJSONObject("poll");
		JSONArray mentionsJson = json.optJSONArray("mentions");
		JSONArray mediaArray = json.optJSONArray("media_attachments");
		JSONArray emojiArray = json.optJSONArray("emojis");
		String replyIdStr = json.optString("in_reply_to_id", "0");
		String replyUserIdStr = json.optString("in_reply_to_account_id", "0");
		String idStr = json.getString("id");
		String visibilityStr = json.getString("visibility");

		author = new MastodonUser(json.getJSONObject("account"), currentUserId);
		createdAt = StringUtils.getTime(json.optString("created_at"), StringUtils.TIME_MASTODON);
		replyCount = json.optInt("replies_count");
		reblogCount = json.optInt("reblogs_count");
		favoriteCount = json.optInt("favourites_count");
		muted = json.optBoolean("muted", false);
		favorited = json.optBoolean("favourited", false);
		reposted = json.optBoolean("reblogged", false);
		sensitive = json.optBoolean("sensitive", false);
		spoiler = json.optBoolean("spoiler_text", false);
		bookmarked = json.optBoolean("bookmarked", false);
		String text = json.optString("content", "");

		try {
			// create newlines at every <br> or <p> tag
			Document jsoupDoc = Jsoup.parse(text);
			jsoupDoc.outputSettings(OUTPUT_SETTINGS);
			jsoupDoc.select("br").after("\\n");
			jsoupDoc.select("p").before("\\n");
			String str = jsoupDoc.html().replaceAll("\\\\n", "\n");
			text = Jsoup.clean(str, "", Safelist.none(), OUTPUT_SETTINGS);
			if (text.startsWith("\n")) {
				text = text.substring(1);
			}
		} catch (Exception exception) {
			// use fallback text string from json
		}
		this.text = text;

		if (author.getId() != currentUserId)
			mentions = author.getScreenname() + ' ';
		if (embeddedJson != null) {
			embeddedStatus = new MastodonStatus(embeddedJson, currentUserId);
			this.url = embeddedStatus.getUrl();
		} else if (!json.isNull("url")) {
			this.url = json.optString("url", "");
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
		if (mentionsJson != null && mentionsJson.length() > 0) {
			StringBuilder mentionsBuilder = new StringBuilder(mentions);
			for (int i = 0; i < mentionsJson.length(); i++) {
				long mentionUserId = 0L;
				JSONObject mentionJson = mentionsJson.getJSONObject(i);
				String mention = '@' + mentionJson.getString("acct");
				String mentionedUserIdStr = mentionJson.optString("id", "0");
				if (mentionedUserIdStr.matches("\\d+"))
					mentionUserId = Long.parseLong(mentionsJson.getJSONObject(i).getString("id"));
				if (mentionUserId != 0L && mentionUserId != currentUserId) {
					mentionsBuilder.append(mention).append(' ');
				}
			}
			mentions = mentionsBuilder.toString();
		}
		if (emojiArray != null && emojiArray.length() > 0) {
			emojis = new Emoji[emojiArray.length()];
			for (int i = 0; i < emojis.length; i++) {
				JSONObject emojiJson = emojiArray.getJSONObject(i);
				emojis[i] = new MastodonEmoji(emojiJson);
			}
		}
		if (appJson != null) {
			source = appJson.optString("name", "");
		} else if (embeddedStatus != null) {
			source = embeddedStatus.getSource();
		}
		if (cardJson != null) {
			cards = new Card[]{new MastodonCard(cardJson)};
		}
		if (json.has("language") && !json.isNull("language")) {
			String language = json.getString("language");
			if (!language.equals(Locale.getDefault().getLanguage())) {
				this.language = language;
			}
		}
		switch (visibilityStr) {
			default:
			case "public":
				visibility = VISIBLE_PUBLIC;
				break;

			case "private":
				visibility = VISIBLE_PRIVATE;
				break;

			case "direct":
				visibility = VISIBLE_DIRECT;
				break;

			case "unlisted":
				visibility = VISIBLE_UNLISTED;
				break;
		}
		try {
			id = Long.parseLong(idStr);
			if (!replyIdStr.equals("null"))
				replyId = Long.parseLong(replyIdStr);
			if (!replyUserIdStr.equals("null"))
				replyUserId = Long.parseLong(replyUserIdStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad ID:" + idStr + ' ' + replyIdStr + ' ' + replyUserIdStr);
		}
	}


	@Override
	public long getId() {
		return id;
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
	public long getTimestamp() {
		return createdAt;
	}


	@Override
	public String getSource() {
		return source;
	}


	@Nullable
	@Override
	public Status getEmbeddedStatus() {
		return embeddedStatus;
	}


	@Override
	public String getReplyName() {
		return "";
	}


	@Override
	public long getRepliedUserId() {
		return replyUserId;
	}


	@Override
	public long getRepliedStatusId() {
		return replyId;
	}


	@Override
	public long getRepostId() {
		return 0L;
	}


	@Override
	public int getRepostCount() {
		return reblogCount;
	}


	@Override
	public int getFavoriteCount() {
		return favoriteCount;
	}


	@Override
	public int getReplyCount() {
		return replyCount;
	}


	@Override
	public int getVisibility() {
		return visibility;
	}


	@NonNull
	@Override
	public Media[] getMedia() {
		return medias;
	}


	@NonNull
	@Override
	public Emoji[] getEmojis() {
		return emojis;
	}


	@Override
	public String getUserMentions() {
		return mentions;
	}


	@Override
	public boolean isSensitive() {
		return sensitive;
	}


	@Override
	public boolean isSpoiler() {
		return spoiler;
	}


	@Override
	public boolean isReposted() {
		return reposted;
	}


	@Override
	public boolean isFavorited() {
		return favorited;
	}


	@Override
	public boolean isBookmarked() {
		return bookmarked;
	}


	@Override
	public boolean isHidden() {
		return muted;
	}


	@Override
	public String getUrl() {
		return url;
	}


	@Override
	@Nullable
	public Location getLocation() {
		return null; // todo add implementation if supported by API
	}


	@Override
	public String getLanguage() {
		return language;
	}


	@NonNull
	@Override
	public Card[] getCards() {
		return cards;
	}


	@Nullable
	@Override
	public Poll getPoll() {
		return poll;
	}


	@Nullable
	@Override
	public Metrics getMetrics() {
		return null;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Status))
			return false;
		Status status = ((Status) obj);
		return status.getId() == id && status.getTimestamp() == getTimestamp() && status.getAuthor().equals(getAuthor());
	}


	@NonNull
	@Override
	public String toString() {
		return "from=\"" + getAuthor().getScreenname() + "\" text=\"" + getText() + "\"";
	}

	/**
	 * set repost status
	 */
	public void setRepost(boolean reposted) {
		this.reposted = reposted;
		if (embeddedStatus instanceof MastodonStatus) {
			((MastodonStatus) embeddedStatus).setRepost(reposted);
		}
		if (!reposted && reblogCount > 0) {
			reblogCount--;
		}
	}

	/**
	 * set favorite status
	 */
	public void setFavorite(boolean favorited) {
		this.favorited = favorited;
		if (embeddedStatus instanceof MastodonStatus) {
			((MastodonStatus) embeddedStatus).setFavorite(favorited);
		}
		if (!favorited && favoriteCount > 0) {
			favoriteCount--;
		}
	}

	/**
	 * set bookmark status
	 */
	public void setBookmark(boolean bookmarked) {
		this.bookmarked = bookmarked;
		if (embeddedStatus instanceof MastodonStatus) {
			((MastodonStatus) embeddedStatus).setBookmark(bookmarked);
		}
	}
}