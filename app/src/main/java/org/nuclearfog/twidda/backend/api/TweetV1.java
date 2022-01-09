package org.nuclearfog.twidda.backend.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

/**
 * new tweet implementation for API v1.1
 *
 * @author nuclearfog
 */
class TweetV1 implements Tweet {

    /**
     * query to enable extended mode to show tweets with more than 140 characters
     */
    static final String EXT_MODE = "tweet_mode=extended";

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
    private String replyName;
    private long replyUserId;
    private long replyId;
    private long retweetId;
    private int retweetCount;
    private int favoriteCount;
    private String[] mediaUrls;
    private String userMentions;
    private String mediaType;
    private boolean isSensitive;
    private boolean isRetweeted;
    private boolean isFavorited;
    private String location;
    private String coordinates;


    TweetV1(JSONObject json, long twitterId) throws JSONException{
        id = json.optLong("id");
        text = json.optString("full_text");
        timestamp = StringTools.getTime(json.optString("created_at"));
        replyId = json.optLong("in_reply_to_status_id", -1);
        replyUserId = json.optLong("in_reply_to_status_id", -1);
        location = json.optString("place");
        replyName = '@' + json.optString("in_reply_to_screen_name");
        retweetCount = json.optInt("retweet_count");
        favoriteCount = json.optInt("favorite_count");
        isFavorited = json.optBoolean("favorited");
        isRetweeted = json.optBoolean("retweeted");
        isSensitive = json.optBoolean("possibly_sensitive");
        source = StringTools.getSource(json.optString("source"));

        JSONObject userJson = json.getJSONObject("user");
        JSONObject tweetJson = json.optJSONObject("quoted_status");
        JSONObject retweetJson = json.optJSONObject("current_user_retweet");
        JSONObject entities = json.optJSONObject("entities");
        JSONObject extEntities = json.optJSONObject("extended_entities");
        JSONObject geo = json.optJSONObject("geo");

        author = new UserV1(userJson, twitterId);
        if (location.equals("null"))
            location = "";
        if (geo != null)
            coordinates = geo.optString("coordinates");
        if (retweetJson != null)
            retweetId = retweetJson.optLong("id");
        if (tweetJson != null)
            embeddedTweet = new TweetV1(tweetJson, twitterId);
        if (entities != null)
            addURLs(entities);
        if (extEntities != null) {
            addMedia(extEntities);
            addUserMentions(extEntities);
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

    @Override
    public String[] getMediaLinks() {
        return mediaUrls;
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

    @NonNull
    @Override
    public String toString() {
        return "from:" + author.getScreenname() + " text:" + text;
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
                mediaUrls = new String[media.length()];
                String mime = mediaItem.getString("type");
                switch (mime) {
                    case MIME_PHOTO:
                        mediaType = MIME_PHOTO;
                        // get media URLs
                        for (int pos = 0; pos < mediaUrls.length; pos++) {
                            JSONObject item = media.getJSONObject(pos);
                            if (item != null) {
                                mediaUrls[pos] = item.getString("media_url_https");
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
                                mediaUrls[0] = variant.getString("url");
                                break;
                            }
                        }
                        break;

                    case MIME_ANGIF:
                        mediaType = MIME_ANGIF;
                        JSONObject gif = mediaItem.getJSONObject("video_info");
                        JSONObject gifVariant = gif.getJSONArray("variants").getJSONObject(0);
                        if (MIME_V_MP4.equals(gifVariant.getString("content_type"))) {
                            mediaUrls[0] = gifVariant.getString("url");
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
     * expand URLs int the tweet text
     *
     * @param entities json object with url information
     */
    private void addURLs(@NonNull JSONObject entities) {
        try {
            JSONArray urls = entities.getJSONArray("urls");
            // replace new line symbol with new line character
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
            this.text = builder.toString();
        } catch (JSONException e) {
            // use default description
        }
    }

    /**
     * get mentioned user's screen name
     */
    private void addUserMentions(JSONObject json) {
        StringBuilder buf = new StringBuilder();
        if (!replyName.isEmpty()) {
            buf.append(replyName).append(' ');
        }
        JSONArray mentions = json.optJSONArray("user_mentions");
        if (mentions != null && mentions.length() > 0) {
            for (int pos = 0 ; pos < mentions.length() ; pos++){
                JSONObject mention = mentions.optJSONObject(pos);
                if (mention != null) {
                    buf.append('@').append(mention.optString("screen_name")).append(' ');
                }
            }
        }
        userMentions = buf.toString();
    }
}