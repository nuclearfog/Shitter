package org.nuclearfog.twidda.backend.api.twitter.impl;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;

import java.util.Locale;

/**
 * API v 1.1 implementation of a tweet
 *
 * @author nuclearfog
 */
public class TweetV1 implements Status {

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
	 * type of tweet location to use
	 */
	private static final String LOCATION_TYPE = "Point";

	/**
	 * twitter video/gif MIME
	 */
	private static final String MIME_V_MP4 = "video/mp4";

	private long id;
	private long timestamp;
	private User author;
	@Nullable
	private Status embeddedTweet;

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

	private long replyUserId;
	private long replyTweetId;
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
		String retweetIdStr = "0";
		String tweetIdStr = json.optString("id_str", "");
		String replyName = json.optString("in_reply_to_screen_name", "");
		String replyTweetIdStr = json.optString("in_reply_to_status_id_str", "0");
		String replyUsrIdStr = json.optString("in_reply_to_user_id_str", "0");
		String source = json.optString("source", "");
		String text = createText(json);

		author = new UserV1(json.getJSONObject("user"), twitterId);
		retweetCount = json.optInt("retweet_count");
		favoriteCount = json.optInt("favorite_count");
		isFavorited = json.optBoolean("favorited");
		isRetweeted = json.optBoolean("retweeted");
		isSensitive = json.optBoolean("possibly_sensitive");
		timestamp = StringTools.getTime(json.optString("created_at", ""), StringTools.TIME_TWITTER_V1);
		coordinates = getLocation(json);
		mediaLinks = addMedia(json);
		userMentions = StringTools.getUserMentions(text, author.getScreenname());
		this.source = Jsoup.parse(source).text();
		try {
			id = Long.parseLong(tweetIdStr);
			if (currentUserJson != null)
				retweetIdStr = currentUserJson.optString("id_str", "0");
			if (!replyTweetIdStr.equals("null"))
				replyTweetId = Long.parseLong(replyTweetIdStr);
			if (!replyUsrIdStr.equals("null"))
				replyUserId = Long.parseLong(replyUsrIdStr);
			if (!retweetIdStr.equals("null"))
				retweetId = Long.parseLong(retweetIdStr);
		} catch (NumberFormatException e) {
			throw new JSONException("bad IDs:" + tweetIdStr + "," + replyUsrIdStr + "," + retweetIdStr);
		}
		if (!replyName.isEmpty() && !replyName.equals("null")) {
			this.replyName = '@' + replyName;
		}
		if (locationJson != null) {
			location = locationJson.optString("full_name", "");
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
	public Status getEmbeddedStatus() {
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
	public long getRepliedStatusId() {
		return replyTweetId;
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
		// not implemented in API V1.1
		return 0;
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
	public boolean isReposted() {
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
		if (!(obj instanceof Status))
			return false;
		return ((Status) obj).getId() == id;
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
		// fix: Twitter API v1.1 doesn't increment/decrement retweet count right
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
		// fix: Twitter API v1.1 doesn't increment/decrement favorite count right
		if (!isFavorited && favoriteCount > 0) {
			favoriteCount--;
		}
		if (embeddedTweet instanceof TweetV1) {
			((TweetV1) embeddedTweet).setFavorite(isFavorited);
		}
	}

	/**
	 * overwrite embedded tweet information
	 *
	 * @param tweet new embedded tweet
	 */
	public void setEmbeddedTweet(@Nullable Status tweet) {
		this.embeddedTweet = tweet;
	}

	/**
	 * add media links to tweet if any
	 */
	@NonNull
	private String[] addMedia(JSONObject json) {
		try {
			JSONObject extEntities = json.getJSONObject("extended_entities");
			JSONArray media = extEntities.getJSONArray("media");
			if (media.length() > 0) {
				// determine MIME type
				JSONObject mediaItem = media.getJSONObject(0);
				String mime = mediaItem.getString("type");
				switch (mime) {
					case "photo":
						mediaType = MEDIA_PHOTO;
						// get media URLs
						String[] links = new String[media.length()];
						for (int pos = 0; pos < media.length(); pos++) {
							JSONObject item = media.getJSONObject(pos);
							links[pos] = item.getString("media_url_https");
						}
						return links;

					case "video":
						mediaType = MEDIA_VIDEO;
						int maxBitrate = -1;
						links = new String[1];
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
						links = new String[1];
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
	@NonNull
	private String createText(JSONObject json) {
		String text = json.optString("full_text", "");
		JSONObject entities = json.optJSONObject("entities");
		if (entities != null) {
			JSONArray urls = entities.optJSONArray("urls");
			if (urls != null) {
				try {
					// check for shortened urls and replace them with full urls
					StringBuilder builder = new StringBuilder(text);
					for (int i = urls.length() - 1; i >= 0; i--) {
						JSONObject entry = urls.getJSONObject(i);
						String link = entry.getString("expanded_url");
						JSONArray indices = entry.getJSONArray("indices");
						int start = indices.getInt(0);
						int end = indices.getInt(1);
						int offset = StringTools.calculateIndexOffset(text, start);
						builder.replace(start + offset, end + offset, link);
					}
					return StringTools.unescapeString(builder.toString());
				} catch (JSONException e) {
					// use default tweet text
				}
			}
		}
		return StringTools.unescapeString(text);
	}

	/**
	 * create location coordinate string to use for uri link
	 *
	 * @param json root tweet json
	 * @return location uri scheme or empty string if tweet has no location information
	 */
	@NonNull
	private String getLocation(JSONObject json) {
		try {
			JSONObject coordinateJson = json.optJSONObject("coordinates");
			if (coordinateJson != null) {
				if (LOCATION_TYPE.equals(coordinateJson.optString("type"))) {
					JSONArray coordinateArray = coordinateJson.optJSONArray("coordinates");
					if (coordinateArray != null && coordinateArray.length() == 2) {
						double lon = coordinateArray.getDouble(0);
						double lat = coordinateArray.getDouble(1);
						return String.format(Locale.US, "%.6f,%.6f", lat, lon);
					}
				}
			}
		} catch (JSONException e) {
			// use empty string
		}
		return "";
	}
}