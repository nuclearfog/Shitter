package org.nuclearfog.twidda.backend.api.twitter.v2.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.LocationV2Map;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.MediaV2Map;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.PollV2Map;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.UserV2Map;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Metrics;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Tweet implementation of Twitter API v2 adding extra information to Tweet V1.1
 *
 * @author nuclearfog
 */
public class TweetV2 implements Status {

	private static final long serialVersionUID = -2740140825640061692L;

	/**
	 * fields to enable tweet expansions with extra information
	 */
	public static final String FIELDS_EXPANSION = "expansions=attachments.poll_ids%2Cattachments.media_keys%2Cauthor_id%2Cgeo.place_id" +
			"%2Cin_reply_to_user_id%2Centities.mentions.username";

	/**
	 * default tweet fields
	 */
	public static final String FIELDS_TWEET = "tweet.fields=attachments%2Cauthor_id%2Cconversation_id%2Ccreated_at%2Centities%2Cgeo%2Cid" +
			"%2Cin_reply_to_user_id%2Cpossibly_sensitive%2Cpublic_metrics%2Creferenced_tweets%2Creply_settings%2Csource%2Ctext";

	/**
	 * default tweet fields with non public metrics
	 * (only valid if current user is the author of this tweet, the tweet isn't a retweet and the tweet isn't older than 28 days)
	 */
	public static final String FIELDS_TWEET_PRIVATE = FIELDS_TWEET + "%2Cnon_public_metrics";

	private long id;
	private long timestamp;
	private long replyUserId;
	private long repliedTweetId;
	private long retweetId;
	private String tweetText;
	private String source;
	private String mentions;
	private String replyName = "";
	private String host;
	private Location location;

	private boolean retweeted;
	private boolean favorited;
	private boolean sensitive;
	private int replyCount;
	private int retweetCount;
	private int favoriteCount;

	private User author;
	private Status embedded;
	private Metrics metrics;
	private Poll poll;
	private Media[] medias = {};
	private Card[] cards = {};

	/**
	 * @param json        tweet json format
	 * @param userMap     map containing user instances
	 * @param mediaMap    map containing media instances
	 * @param pollMap     map containing poll instances
	 * @param locationMap map containing location instances
	 * @param tweetCompat tweet v1.1 object
	 */
	public TweetV2(JSONObject json, @NonNull UserV2Map userMap, @Nullable MediaV2Map mediaMap, @Nullable PollV2Map pollMap, @Nullable LocationV2Map locationMap, String host, @Nullable Status tweetCompat) throws JSONException {
		if (json.has("data"))
			json = json.getJSONObject("data");
		JSONObject publicMetrics = json.getJSONObject("public_metrics");
		JSONObject nonPublicMetrics = json.optJSONObject("non_public_metrics");
		JSONObject entities = json.optJSONObject("entities");
		JSONObject geoJson = json.optJSONObject("geo");
		JSONObject attachments = json.optJSONObject("attachments");
		JSONArray tweetReferences = json.optJSONArray("referenced_tweets");
		String idStr = json.getString("id");
		String textStr = json.optString("text", "");
		String timeStr = json.optString("created_at", "");
		String replyUserIdStr = json.optString("in_reply_to_user_id", "-1");
		String authorId = json.getString("author_id");
		// string to long conversion
		try {
			id = Long.parseLong(idStr);
			replyUserId = Long.parseLong(replyUserIdStr);
			author = userMap.get(Long.parseLong(authorId));
			if (attachments != null) {
				JSONArray pollIds = attachments.optJSONArray("poll_ids");
				if (pollMap != null && pollIds != null && pollIds.length() > 0)
					poll = pollMap.get(Long.parseLong(pollIds.getString(0)));
			}
			if (locationMap != null && geoJson != null) {
				String locIdStr = geoJson.getString("place_id");
				location = locationMap.get(Long.parseUnsignedLong(locIdStr, 16));
			}
		} catch (NumberFormatException e) {
			throw new JSONException("Bad IDs: " + idStr + "," + replyUserIdStr + "," + authorId);
		}
		replyCount = publicMetrics.getInt("reply_count");
		retweetCount = publicMetrics.getInt("retweet_count");
		favoriteCount = publicMetrics.getInt("like_count");
		timestamp = StringUtils.getTime(timeStr, StringUtils.TIME_TWITTER_V2);
		source = json.optString("source", "");
		sensitive = json.optBoolean("possibly_sensitive", false);
		mentions = author.getScreenname() + ' ';
		this.host = host;
		// add media
		if (attachments != null) {
			JSONArray mediaKeys = attachments.optJSONArray("media_keys");
			if (mediaMap != null && mediaKeys != null && mediaKeys.length() > 0) {
				medias = new Media[mediaKeys.length()];
				for (int i = 0; i < mediaKeys.length(); i++) {
					medias[i] = mediaMap.get(mediaKeys.getString(i));
				}
			}
		}
		// add metrics
		if (nonPublicMetrics != null) {
			metrics = new MetricsV2(publicMetrics, nonPublicMetrics);
		}
		if (entities != null) {
			JSONArray mentionsJson = entities.optJSONArray("mentions");
			JSONArray urls = entities.optJSONArray("urls");
			// add mentioned usernames
			if (mentionsJson != null && mentionsJson.length() > 0) {
				StringBuilder builder = new StringBuilder(mentions);
				for (int i = 0; i < mentionsJson.length(); i++) {
					JSONObject mentionJson = mentionsJson.getJSONObject(i);
					String mention_name = '@' + mentionJson.getString("username");
					builder.append(mention_name).append(' ');
				}
				mentions = builder.toString();
			}
			// expand urls
			if (urls != null) {
				// check for shortened urls and replace them with full urls
				StringBuilder builder = new StringBuilder(textStr);
				List<Card> cardsList = new LinkedList<>();
				for (int i = urls.length() - 1; i >= 0; i--) {
					// expand shortened links
					JSONObject entry = urls.getJSONObject(i);
					String expandedUrl = entry.getString("expanded_url");
					String displayUrl = entry.getString("display_url");
					String mediaKey = entry.optString("media_key", "");
					int start = entry.optInt("start", -1);
					int end = entry.optInt("end", -1);
					if (start >= 0 && end > start) {
						int offset = StringUtils.calculateIndexOffset(textStr, start);
						// replace shortened link
						if (!displayUrl.contains("pic.twitter.com")) {
							builder.replace(start + offset, end + offset, expandedUrl);
						}
						// remove shortened link if it is a media link
						else {
							builder.delete(start + offset, end + offset);
						}
					}
					// create Twitter card if link is not a media link
					if (mediaKey.isEmpty() && entry.has("title")) {
						TwitterCard item = new TwitterCard(entry);
						cardsList.add(item);
					}
				}
				tweetText = StringUtils.unescapeString(builder.toString());
				cards = cardsList.toArray(cards);
			} else {
				tweetText = StringUtils.unescapeString(textStr);
			}
		} else {
			tweetText = StringUtils.unescapeString(textStr);
		}
		// add references to other tweets
		if (tweetReferences != null && tweetReferences.length() > 0) {
			for (int i = 0; i < tweetReferences.length(); i++) {
				JSONObject tweetReference = tweetReferences.getJSONObject(i);
				String referenceType = tweetReference.optString("type", "");
				try {
					if (referenceType.equals("replied_to")) {
						repliedTweetId = Long.parseLong(tweetReference.optString("id"));
					} else if (referenceType.equals("retweeted")) {
						retweetId = Long.parseLong(tweetReference.optString("id"));
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		// add/override missing attributes using API v1.1
		if (tweetCompat != null) {
			replyName = tweetCompat.getReplyName();
			embedded = tweetCompat.getEmbeddedStatus();
			retweeted = tweetCompat.isReposted();
			favorited = tweetCompat.isFavorited();
			// fixme: for any reason Twitter API 2.0 doesn't return the attributes below
			source = tweetCompat.getSource();
		}
	}


	@Override
	public long getId() {
		return id;
	}


	@Override
	public String getText() {
		return tweetText;
	}


	@Override
	public User getAuthor() {
		return author;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@Override
	public String getSource() {
		return source;
	}


	@Nullable
	@Override
	public Status getEmbeddedStatus() {
		return embedded;
	}


	@Override
	public String getReplyName() {
		return replyName;
	}


	@Override
	public long getRepliedUserId() {
		return replyUserId;
	}


	@Override
	public long getRepliedStatusId() {
		return repliedTweetId;
	}


	@Override
	public long getRepostId() {
		return retweetId;
	}


	@Override
	public int getRepostCount() {
		return retweetCount;
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
		return author.isProtected() ? VISIBLE_PRIVATE : VISIBLE_PUBLIC;
	}


	@NonNull
	@Override
	public Media[] getMedia() {
		return medias;
	}


	@NonNull
	@Override
	public Emoji[] getEmojis() {
		return new Emoji[0];
	}


	@Override
	public String getUserMentions() {
		return mentions;
	}


	@Override
	public String getLanguage() {
		return ""; // todo implement this
	}


	@Override
	public boolean isSensitive() {
		return sensitive;
	}


	@Override
	public boolean isSpoiler() {
		return false;
	}


	@Override
	public boolean isReposted() {
		return retweeted;
	}


	@Override
	public boolean isFavorited() {
		return favorited;
	}


	@Override
	public boolean isBookmarked() {
		return false;
	}


	@Override
	public boolean isHidden() {
		return false;
	}


	@Override
	public String getUrl() {
		if (author.getScreenname().length() > 1) {
			return host + '/' + author.getScreenname().substring(1) + "/status/" + id;
		}
		return "";
	}


	@NonNull
	@Override
	public Card[] getCards() {
		return cards;
	}


	@Override
	@Nullable
	public Location getLocation() {
		return location;
	}


	@Nullable
	@Override
	public Poll getPoll() {
		return poll;
	}


	@Nullable
	@Override
	public Metrics getMetrics() {
		return metrics;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Status))
			return false;
		Status status = ((Status) obj);
		return status.getId() == id && status.getTimestamp() == getTimestamp() && status.getAuthor().equals(getAuthor());
	}


	@Override
	public int compareTo(Status status) {
		if (status.getTimestamp() != getTimestamp())
			return Long.compare(status.getTimestamp(), getTimestamp());
		return Long.compare(status.getId(), getId());
	}


	@NonNull
	@Override
	public String toString() {
		return "from=\"" + getAuthor().getScreenname() + "\" text=\"" + getText() + "\"";
	}
}