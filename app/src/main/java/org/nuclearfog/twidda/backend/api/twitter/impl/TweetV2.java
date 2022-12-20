package org.nuclearfog.twidda.backend.api.twitter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Card;
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
	 * parameter to add user object
	 */
	public static final String FIELDS_USER = UserV2.PARAMS;

	/**
	 * fields to enable tweet expansions with extra information
	 */
	public static final String FIELDS_EXPANSION = "attachments.poll_ids%2Cauthor_id%2Creferenced_tweets.id%2Cattachments.media_keys";

	/**
	 * default tweet fields
	 */
	public static final String FIELDS_TWEET = "id%2Ctext%2Cattachments%2Cconversation_id%2Centities%2Cpublic_metrics%2Creply_settings%2Cgeo%2Csource%2Cpossibly_sensitive";

	/**
	 * default tweet fields with non public metrics
	 * (only valid if current user is the author of this tweet, the tweet isn't a retweet and the tweet isn't older than 30 days)
	 */
	public static final String FIELDS_TWEET_PRIVATE = FIELDS_TWEET + "%2Cnon_public_metrics";

	/**
	 * fields to add twitter poll object
	 */
	public static final String FIELDS_POLL = "duration_minutes%2Cend_datetime%2Cid%2Coptions%2Cvoting_status";

	/**
	 * fields to add extra media information
	 */
	public static final String FIELDS_MEDIA = MediaV2.FIELDS_MEDIA;

	private long id;
	private long timestamp;
	private long replyUserId;
	private long conversationId;
	private long repliedTweetId;
	private long retweetId;
	private String tweetText;
	private String source;
	private String mentions = "";
	private String replyName = "";
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
	 * @param json      Tweet v2 json
	 * @param currentId Id of the current user
	 */
	public TweetV2(JSONObject json, long currentId) throws JSONException {
		this(json, null, currentId);
	}

	/**
	 * @param json        Tweet v2 json
	 * @param tweetCompat Tweet containing base informations
	 * @param currentId   Id of the current user
	 */
	public TweetV2(JSONObject json, @Nullable Status tweetCompat, long currentId) throws JSONException {
		JSONObject data = json.getJSONObject("data");
		JSONObject includes = json.getJSONObject("includes");
		JSONObject publicMetrics = data.getJSONObject("public_metrics");
		JSONObject nonPublicMetrics = data.optJSONObject("non_public_metrics");
		JSONObject entities = data.getJSONObject("entities");
		JSONObject geoJson = data.optJSONObject("geo");
		JSONArray mentionsJson = entities.optJSONArray("mentions");
		JSONArray tweetReferences = data.optJSONArray("referenced_tweets");
		JSONArray users = includes.getJSONArray("users");
		JSONArray urls = entities.optJSONArray("urls");
		JSONArray polls = includes.optJSONArray("polls");
		JSONArray mediaArray = includes.optJSONArray("media");
		String idStr = data.getString("id");
		String textStr = data.optString("text", "");
		String timeStr = data.optString("created_at", "");
		String replyUserIdStr = data.optString("in_reply_to_user_id", "-1");
		String conversationIdStr = data.optString("conversation_id", "-1");

		author = new UserV2(users.getJSONObject(0), currentId);
		replyCount = publicMetrics.getInt("reply_count");
		retweetCount = publicMetrics.getInt("retweet_count");
		favoriteCount = publicMetrics.getInt("like_count");
		timestamp = StringTools.getTime(timeStr, StringTools.TIME_TWITTER_V2);//fehler
		source = data.optString("source", "unknown");
		sensitive = data.optBoolean("possibly_sensitive", false);

		// add attributes missing from API V2.0
		if (tweetCompat != null) {
			replyName = tweetCompat.getReplyName();
			embedded = tweetCompat.getEmbeddedStatus();
			retweeted = tweetCompat.isReposted();
			favorited = tweetCompat.isFavorited();
		}
		// add poll
		if (polls != null && polls.length() > 0) {
			poll = new TwitterPoll(polls.getJSONObject(0));
		}
		// add metrics
		if (nonPublicMetrics != null) {
			metrics = new MetricsV2(publicMetrics, nonPublicMetrics);
		}
		// add mentioned usernames
		if (mentionsJson != null && mentionsJson.length() > 0) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < mentionsJson.length(); i++) {
				JSONObject mentionJson = mentionsJson.getJSONObject(i);
				builder.append('@').append(mentionJson.getString("username")).append(' ');
			}
			mentions = builder.toString();
		}
		// add location
		if (geoJson != null) {
			location = new LocationV2(geoJson);
		}
		// add media
		if (mediaArray != null && mediaArray.length() > 0) {
			medias = new Media[mediaArray.length()];
			for (int i = 0; i < mediaArray.length(); i++) {
				JSONObject media = mediaArray.getJSONObject(i);
				medias[i] = new MediaV2(media);
			}
		}
		// expand urls
		if (urls != null) {
			// check for shortened urls and replace them with full urls
			StringBuilder builder = new StringBuilder(textStr);
			List<Card> cardsList = new LinkedList<>();
			for (int i = urls.length() - 1; i >= 0; i--) {
				// expand shortened links
				JSONObject entry = urls.getJSONObject(i);
				String link = entry.getString("expanded_url");
				String mediaKey = entry.optString("media_key", "");
				int start = entry.optInt("start", -1);
				int end = entry.optInt("end", -1);
				if (start >= 0 && end > start) {
					int offset = StringTools.calculateIndexOffset(textStr, start);
					builder.replace(start + offset, end + offset, link);
				}
				// create Twitter card if link is not a media link
				if (mediaKey.isEmpty()) {
					TwitterCard item = new TwitterCard(urls.getJSONObject(i));
					cardsList.add(item);
				}
			}
			tweetText = StringTools.unescapeString(builder.toString());
			cards = cardsList.toArray(cards);
		} else {
			tweetText = textStr;
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
		// add IDs
		try {
			id = Long.parseLong(idStr);
			replyUserId = Long.parseLong(replyUserIdStr);
			if (!conversationIdStr.equals("null")) {
				conversationId = Long.parseLong(conversationIdStr);
			} else {
				conversationId = -1L;
			}
		} catch (NumberFormatException e) {
			throw new JSONException("Bad IDs: " + conversationIdStr + "," + idStr + "," + replyUserIdStr);
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
	public long getConversationId() {
		return conversationId;
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


	@NonNull
	@Override
	public Media[] getMedia() {
		return medias;
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
	public boolean isReposted() {
		return retweeted;
	}


	@Override
	public boolean isFavorited() {
		return favorited;
	}


	@Override
	public boolean isHidden() {
		return false;
	}


	@Override
	public String getLinkPath() {
		if (!author.getScreenname().isEmpty()) {
			String username = '/' + author.getScreenname().substring(1);
			return username + "/status/" + id;
		}
		return "";
	}


	@Override
	public Location getLocation() {
		return location;
	}


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
		return metrics;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Status))
			return false;
		return ((Status) obj).getId() == id;
	}


	@NonNull
	@Override
	public String toString() {
		return "id=" + id + " from=" + author;
	}
}