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
     * include ID of the reteet if available
     */
    static final String INCL_RT_ID = "include_my_retweet=true";

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
    private String userMentions = "";
    private String location = "";
    private String replyName = "";
    private String coordinates = "";
    private String[] mediaUrls = {};
    private String mediaType = "";


    TweetV1(JSONObject json, long twitterId) throws JSONException{
        id = json.optLong("id");
        text = json.optString("full_text");
        replyId = json.optLong("in_reply_to_status_id", -1);
        replyUserId = json.optLong("in_reply_to_status_id", -1);
        retweetCount = json.optInt("retweet_count");
        favoriteCount = json.optInt("favorite_count");
        isFavorited = json.optBoolean("favorited");
        isRetweeted = json.optBoolean("retweeted");
        isSensitive = json.optBoolean("possibly_sensitive");
        timestamp = StringTools.getTime(json.optString("created_at"));
        source = StringTools.getSource(json.optString("source"));
        String replyName = json.optString("in_reply_to_screen_name");

        JSONObject locationJson = json.optJSONObject("place");
        JSONObject coordinateJson = json.optJSONObject("coordinates");
        JSONObject user = json.getJSONObject("user");
        JSONObject quoted_tweet = json.optJSONObject("retweeted_status");
        JSONObject user_retweet = json.optJSONObject("current_user_retweet");
        JSONObject entities = json.optJSONObject("entities");
        JSONObject extEntities = json.optJSONObject("extended_entities");

        author = new UserV1(user, twitterId);
        if (locationJson != null) {
            location = locationJson.optString("full_name");
        }
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
        if (!replyName.equals("null"))
            this.replyName = '@' + replyName;
        if (user_retweet != null)
            retweetId = user_retweet.optLong("id");
        if (quoted_tweet != null)
            embeddedTweet = new TweetV1(quoted_tweet, twitterId);
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
     * @param entities json object with tweet entities
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
     *
     * @param extEntities JSON object with extended entities
     */
    private void addUserMentions(JSONObject extEntities) {
        StringBuilder buf = new StringBuilder();
        if (!replyName.isEmpty() && replyName.equals(author.getScreenname())) {
            buf.append(replyName).append(' ');
        }
        JSONArray mentions = extEntities.optJSONArray("user_mentions");
        if (mentions != null && mentions.length() > 0) {
            for (int pos = 0 ; pos < mentions.length() ; pos++){
                JSONObject mention = mentions.optJSONObject(pos);
                if (mention != null) {
                    long mentionedUserId = mention.optLong("id");
                    String mentionedUsername = mention.optString("screen_name");
                    if (mentionedUserId != author.getId() && !mentionedUsername.isEmpty()) {
                        buf.append('@').append(mentionedUsername).append(' ');
                    }
                }
            }
        }
        userMentions = buf.toString();
    }
}