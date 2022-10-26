package org.nuclearfog.twidda.backend.api.impl;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * API v 1.1 implementation of a tweet
 *
 * @author nuclearfog
 */
public class TweetV1 implements Tweet {

	public static final long serialVersionUID = 70666106496232760L;

	/**
	 * query parameter to enable extended mode to show tweets with more than 140 characters
	 */
	public static final String EXT_MODE = "tweet_mode=extended";

	/**
	 * query parameter to include ID of the retweet if available
	 */
	public static final String INCL_RT_ID = "include_my_retweet=true";

	/**
	 * query parameter to include entities like urls, media or user mentions
	 */
	public static final String INCL_ENTITIES = "include_entities=true";

	/**
	 * twitter video/gif MIME
	 */
	private static final String MIME_V_MP4 = "video/mp4";

	private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

	private long id;
	private long timestamp;
	private User author;
	private Tweet embeddedTweet;

	private long retweetId;
	private int retweetCount;
	private int favoriteCount;
	private boolean isSensitive;
	private boolean isRetweeted;
	private boolean isFavorited;
	private String[] mediaLinks;
	private String userMentions;
	private String coordinates;
	private String text;
	private String source;

	private long replyUserId = -1L;
	private long replyTweetId = -1L;
	private int mediaType = MEDIA_NONE;
	private String location = "";
	private String replyName = "";

	/**
	 * @param json      JSON object of a single tweet
	 * @param twitterId ID of the current user
	 * @throws JSONException if values are missing
	 */
	public TweetV1(JSONObject json, long twitterId) throws JSONException {
		JSONObject locationJson = json.optJSONObject("place");
		JSONObject currentUserJson = json.optJSONObject("current_user_retweet");
		JSONObject embeddedTweetJson = json.optJSONObject("retweeted_status");
		String tweetIdStr = json.optString("id_str");
		String replyName = json.optString("in_reply_to_screen_name");
		String replyTweetIdStr = json.optString("in_reply_to_status_id_str");
		String replyUsrIdStr = json.optString("in_reply_to_user_id_str");
		String text = createText(json);

		author = new UserV1(json.getJSONObject("user"), twitterId);
		retweetCount = json.optInt("retweet_count");
		favoriteCount = json.optInt("favorite_count");
		isFavorited = json.optBoolean("favorited");
		isRetweeted = json.optBoolean("retweeted");
		isSensitive = json.optBoolean("possibly_sensitive");
		timestamp = StringTools.getTime1(json.optString("created_at"));
		source = StringTools.getSource(json.optString("source"));
		coordinates = getLocation(json);
		mediaLinks = addMedia(json);
		userMentions = StringTools.getUserMentions(text, author.getScreenname());

		if (ID_PATTERN.matcher(tweetIdStr).matches()) {
			id = Long.parseLong(tweetIdStr);
		} else {
			throw new JSONException("bad ID: " + tweetIdStr);
		}
		if (!replyName.isEmpty() && !replyName.equals("null")) {
			this.replyName = '@' + replyName;
		}
		if (ID_PATTERN.matcher(replyTweetIdStr).matches()) {
			replyTweetId = Long.parseLong(replyTweetIdStr);
		}
		if (ID_PATTERN.matcher(replyUsrIdStr).matches()) {
			replyUserId = Long.parseLong(replyUsrIdStr);
		}
		if (locationJson != null) {
			location = locationJson.optString("full_name");
		}
		if (currentUserJson != null) {
			String retweetIdStr = currentUserJson.optString("id_str");
			if (ID_PATTERN.matcher(retweetIdStr).matches()) {
				retweetId = Long.parseLong(retweetIdStr);
			}
		}
		if (embeddedTweetJson != null) {
			embeddedTweet = new TweetV1(embeddedTweetJson, twitterId);
		}
		// remove short media link
		int linkPos = text.lastIndexOf("https://t.co/");
		if (linkPos >= 0) {
			this.text = text.substring(0, linkPos);
		} else {
			this.text = text;
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
		return timestamp;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Nullable
	@Override
	public Tweet getEmbeddedTweet() {
		return embeddedTweet;
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
	public long getRepliedTweetId() {
		return replyTweetId;
	}

	@Override
	public long getRetweetId() {
		return retweetId;
	}

	@Override
	public int getRetweetCount() {
		return retweetCount;
	}

	@Override
	public int getFavoriteCount() {
		return favoriteCount;
	}

	@NonNull
	@Override
	public Uri[] getMediaUris() {
		Uri[] result = new Uri[mediaLinks.length];
		for (int i = 0; i < result.length; i++)
			result[i] = Uri.parse(mediaLinks[i]);
		return result;
	}

	@Override
	public String getUserMentions() {
		return userMentions;
	}

	@Override
	public int getMediaType() {
		return mediaType;
	}

	@Override
	public boolean isSensitive() {
		return isSensitive;
	}

	@Override
	public boolean isRetweeted() {
		return isRetweeted;
	}

	@Override
	public boolean isFavorited() {
		return isFavorited;
	}

	@Override
	public String getLocationName() {
		return location;
	}

	@Override
	public String getLocationCoordinates() {
		return coordinates;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof Tweet))
			return false;
		return ((Tweet) obj).getId() == id;
	}

	@NonNull
	@Override
	public String toString() {
		return "from=\"" + author.getScreenname() + "\" text=\"" + text + "\"";
	}

	/**
	 * enable/disable retweet status and count
	 *
	 * @param isRetweeted true if this tweet should be retweeted
	 */
	public void setRetweet(boolean isRetweeted) {
		this.isRetweeted = isRetweeted;
		// note: Twitter API v1.1 doesn't increment/decrement retweet count right
		// so we have to correct this number
		if (!isRetweeted && retweetCount > 0) {
			retweetCount--;
		}
		if (embeddedTweet instanceof TweetV1) {
			((TweetV1) embeddedTweet).setRetweet(isRetweeted);
		}
	}

	/**
	 * enable/disable favorite status and count
	 *
	 * @param isFavorited true if this tweet should be favorited
	 */
	public void setFavorite(boolean isFavorited) {
		this.isFavorited = isFavorited;
		if (embeddedTweet instanceof TweetV1) {
			((TweetV1) embeddedTweet).setFavorite(isFavorited);
		}
	}

	/**
	 * overwrite embedded tweet information
	 *
	 * @param tweet new embedded tweet
	 */
	public void setEmbeddedTweet(Tweet tweet) {
		this.embeddedTweet = tweet;
	}

	/**
	 * add media links to tweet if any
	 */
	private String[] addMedia(JSONObject json) {
		try {
			JSONObject extEntities = json.getJSONObject("extended_entities");
			JSONArray media = extEntities.getJSONArray("media");
			if (media.length() > 0) {
				// determine MIME type
				JSONObject mediaItem = media.getJSONObject(0);
				String[] links = new String[media.length()];
				String mime = mediaItem.getString("type");
				switch (mime) {
					case "photo":
						mediaType = MEDIA_PHOTO;
						// get media URLs
						for (int pos = 0; pos < links.length; pos++) {
							JSONObject item = media.getJSONObject(pos);
							if (item != null) {
								links[pos] = item.getString("media_url_https");
							}
						}
						return links;

					case "video":
						mediaType = MEDIA_VIDEO;
						int maxBitrate = -1;
						JSONObject video = mediaItem.getJSONObject("video_info");
						JSONArray videoVariants = video.getJSONArray("variants");
						for (int pos = 0; pos < videoVariants.length(); pos++) {
							JSONObject variant = videoVariants.getJSONObject(pos);
							int bitRate = variant.optInt("bitrate", 0);
							if (bitRate > maxBitrate && MIME_V_MP4.equals(variant.getString("content_type"))) {
								links[0] = variant.getString("url");
								maxBitrate = bitRate;
							}
						}
						return links;

					case "animated_gif":
						mediaType = MEDIA_GIF;
						JSONObject gif = mediaItem.getJSONObject("video_info");
						JSONObject gifVariant = gif.getJSONArray("variants").getJSONObject(0);
						if (MIME_V_MP4.equals(gifVariant.getString("content_type"))) {
							links[0] = gifVariant.getString("url");
						}
						return links;

					default:
						mediaType = MEDIA_NONE;
						break;
				}
			}
		} catch (JSONException e) {
			// ignore, return empty array
		}
		return new String[0];
	}

	/**
	 * read tweet and expand urls
	 */
	private String createText(@NonNull JSONObject json) {
		String text = json.optString("full_text");
		StringBuilder builder = new StringBuilder(text);

		// check for shortened urls and replace them with full urls
		try {
			JSONObject entities = json.getJSONObject("entities");
			JSONArray urls = entities.getJSONArray("urls");
			for (int i = urls.length() - 1; i >= 0; i--) {
				JSONObject entry = urls.getJSONObject(i);
				String link = entry.getString("expanded_url");
				JSONArray indices = entry.getJSONArray("indices");
				int start = indices.getInt(0);
				int end = indices.getInt(1);
				int offset = StringTools.calculateIndexOffset(text, start);
				builder.replace(start + offset, end + offset, link);
			}
		} catch (JSONException e) {
			// use default tweet text
			builder = new StringBuilder(text);
		}
		// remove html escape strings
		return StringTools.unescapeString(builder.toString());
	}

	/**
	 * create location coordinate string to use for uri link
	 *
	 * @param json root tweet json
	 * @return location uri scheme or empty string if tweet has no location information
	 */
	private String getLocation(JSONObject json) {
		try {
			JSONObject coordinateJson = json.optJSONObject("coordinates");
			if (coordinateJson != null) {
				if (coordinateJson.optString("type").equals("Point")) {
					JSONArray coordinateArray = coordinateJson.optJSONArray("coordinates");
					if (coordinateArray != null && coordinateArray.length() == 2) {
						double lon = coordinateArray.getDouble(0);
						double lat = coordinateArray.getDouble(1);
						return String.format(Locale.US, "%.6f,%.6f", lat, lon);
					}
				}
			}
		} catch (JSONException e) {
			// ignore, use empty string
		}
		return "";
	}
}