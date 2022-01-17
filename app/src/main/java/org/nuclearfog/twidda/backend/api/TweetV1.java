package org.nuclearfog.twidda.backend.api;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

/**
 * API v 1.1 implementation of a tweet
 *
 * @author nuclearfog
 */
class TweetV1 implements Tweet {

    private static final long serialVersionUID = 70666106496232760L;

    /**
     * query parameter to enable extended mode to show tweets with more than 140 characters
     */
    static final String EXT_MODE = "tweet_mode=extended";

    /**
     * query parameter to include ID of the reteet if available
     */
    static final String INCL_RT_ID = "include_my_retweet=true";

    /**
     * query parameter to include entities like urls, media or user mentions
     */
    static final String INCL_ENTITIES = "include_entities=true";

    /**
     * twitter video/gif MIME
     */
    private static final String MIME_V_MP4 = "video/mp4";

    private long id;
    private String text;
    private User author;
    private long timestamp;
    private String source;
    private Tweet embeddedTweet;
    private long replyUserId;
    private long replyId;
    private long retweetId;
    private int retweetCount;
    private int favoriteCount;
    private boolean isSensitive;
    private boolean isRetweeted;
    private boolean isFavorited;
    private String userMentions;
    private String location = "";
    private String replyName = "";
    private String coordinates = "";
    private String[] mediaLinks = {};
    private String mediaType = "";


    TweetV1(JSONObject json, long twitterId) throws JSONException {
        author = new UserV1(json.getJSONObject("user"), twitterId);
        id = Long.parseLong(json.optString("id_str", "-1"));
        replyId = json.optLong("in_reply_to_status_id", -1);
        replyUserId = json.optLong("in_reply_to_status_id", -1);
        retweetCount = json.optInt("retweet_count");
        favoriteCount = json.optInt("favorite_count");
        isFavorited = json.optBoolean("favorited");
        isRetweeted = json.optBoolean("retweeted");
        isSensitive = json.optBoolean("possibly_sensitive");
        timestamp = StringTools.getTime1(json.optString("created_at"));
        source = StringTools.getSource(json.optString("source"));
        text = createText(json);

        String replyName = json.optString("in_reply_to_screen_name");
        String userMentions = StringTools.getUserMentions(text);

        JSONObject locationJson = json.optJSONObject("place");
        JSONObject coordinateJson = json.optJSONObject("coordinates");
        JSONObject quoted_tweet = json.optJSONObject("retweeted_status");
        JSONObject user_retweet = json.optJSONObject("current_user_retweet");
        JSONObject extEntities = json.optJSONObject("extended_entities");

        if (coordinateJson != null) {
            if (coordinateJson.optString("type").equals("Point")) {
                JSONArray coordinateArray = coordinateJson.optJSONArray("coordinates");
                if (coordinateArray != null && coordinateArray.length() == 2) {
                    double lon = coordinateArray.getDouble(0);
                    double lat = coordinateArray.getDouble(1);
                    coordinates = lon + "," + lat;
                }
            }
        }
        if (locationJson != null) {
            location = locationJson.optString("full_name");
        }
        if (!replyName.equals("null")) {
            this.replyName = '@' + replyName;
        }
        if (author.isCurrentUser()) {
            this.userMentions = userMentions;
        } else {
            this.userMentions = author.getScreenname() + ' ' + userMentions;
        }
        if (user_retweet != null)
            retweetId = user_retweet.optLong("id");
        if (quoted_tweet != null) {
            embeddedTweet = new TweetV1(quoted_tweet, twitterId);
            isRetweeted = embeddedTweet.isRetweeted();
            isFavorited = embeddedTweet.isFavorited();
        }
        if (extEntities != null) {
            addMedia(extEntities);
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
    public long getReplyUserId() {
        return replyUserId;
    }

    @Override
    public long getReplyId() {
        return replyId;
    }

    @Override
    public long getMyRetweetId() {
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
    public Uri[] getMediaLinks() {
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
    public String getMediaType() {
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
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Tweet))
            return false;
        return ((Tweet) obj).getId() == id;
    }

    @NonNull
    @Override
    public String toString() {
        return "from:" + author.getScreenname() + " text:" + text;
    }

    /**
     * enable/disable retweet status and count
     *
     * @param isRetweeted true if this tweet should be retweeted
     */
    void setRetweet(boolean isRetweeted) {
        this.isRetweeted = isRetweeted;
        if (isRetweeted) {
            retweetCount++;
        } else if (retweetCount > 0) {
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
    void setFavorite(boolean isFavorited) {
        this.isFavorited = isFavorited;
        if (isFavorited) {
            favoriteCount++;
        } else if (favoriteCount > 0) {
            favoriteCount--;
        }
        if (embeddedTweet instanceof TweetV1) {
            ((TweetV1) embeddedTweet).setFavorite(isFavorited);
        }
    }

    /**
     * add media links to tweet if any
     */
    private void addMedia(JSONObject json) {
        try {
            JSONArray media = json.getJSONArray("media");
            if (media.length() > 0) {
                // determine MIME type
                JSONObject mediaItem = media.getJSONObject(0);
                mediaLinks = new String[media.length()];
                String mime = mediaItem.getString("type");
                switch (mime) {
                    case MIME_PHOTO:
                        mediaType = MIME_PHOTO;
                        // get media URLs
                        for (int pos = 0; pos < mediaLinks.length; pos++) {
                            JSONObject item = media.getJSONObject(pos);
                            if (item != null) {
                                mediaLinks[pos] = item.getString("media_url_https");
                            }
                        }
                        break;

                    case MIME_VIDEO:
                        mediaType = MIME_VIDEO;
                        JSONObject video = mediaItem.getJSONObject("video_info");
                        JSONArray videoVariants = video.getJSONArray("variants");
                        for (int pos = 0; pos < videoVariants.length(); pos++) {
                            JSONObject variant = videoVariants.getJSONObject(pos);
                            if (MIME_V_MP4.equals(variant.getString("content_type"))) {
                                mediaLinks[0] = variant.getString("url");
                                break;
                            }
                        }
                        break;

                    case MIME_ANGIF:
                        mediaType = MIME_ANGIF;
                        JSONObject gif = mediaItem.getJSONObject("video_info");
                        JSONObject gifVariant = gif.getJSONArray("variants").getJSONObject(0);
                        if (MIME_V_MP4.equals(gifVariant.getString("content_type"))) {
                            mediaLinks[0] = gifVariant.getString("url");
                            break;
                        }
                        break;

                    default:
                        mediaType = MIME_NONE;
                        break;
                }
                // remove short media link
                int linkPos = text.lastIndexOf("https://t.co/");
                if (linkPos >= 0) {
                    text = text.substring(0, linkPos);
                }

            }
        } catch (JSONException e) {
            // ignore
        }
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

        // replace "&amp;" string
        int index = builder.lastIndexOf("&amp;");
        while (index >= 0) {
            builder.replace(index, index + 5, "&");
            index = builder.lastIndexOf("&amp;");
        }
        return builder.toString();
    }
}