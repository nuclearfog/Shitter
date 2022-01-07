package org.nuclearfog.twidda.backend.api;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * API 1.1 implementation of User
 *
 * @author nuclearfog
 */
class UserV1 implements User {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);

    private long userID;
    private long created;
    private String username;
    private String screenName;
    private String description;
    private String location;
    private String profileUrl;
    private String profileImageUrl;
    private String profileBannerUrl;
    private int following;
    private int follower;
    private int tweetCount;
    private int favorCount;
    private boolean isVerified;
    private boolean isLocked;
    private boolean followReqSent;
    private boolean defaultImage;
    private boolean isCurrentUser = true;


    UserV1(JSONObject json, long twitterId) throws JSONException {
        this(json);
        isCurrentUser = twitterId == userID;
    }


    UserV1(JSONObject json) throws JSONException {
        String bannerLink = json.optString("profile_banner_url");
        description = json.optString("description");
        username = json.optString("name");
        screenName = '@' + json.optString("screen_name");
        profileImageUrl = json.optString("profile_image_url_https");
        location = json.optString("location");
        userID = json.optLong("id");
        isVerified = json.optBoolean("verified");
        isLocked = json.optBoolean("protected");
        following = json.optInt("friends_count");
        follower = json.optInt("followers_count");
        tweetCount = json.optInt("statuses_count");
        favorCount = json.optInt("favourites_count");
        followReqSent = json.optBoolean("follow_request_sent");
        defaultImage = json.optBoolean("default_profile_image");
        profileUrl = json.optString("profile_image_url_https");
        setDate(json.optString("created_at"));

        // remove link suffix from banner URL
        if (bannerLink.length() > 4) {
            profileBannerUrl = bannerLink.substring(0, bannerLink.length() - 4);
        }

        // expand URLs
        JSONObject entities = json.optJSONObject("entities");
        if (entities != null) {
            JSONObject url = entities.optJSONObject("url");
            if (url != null) {
                JSONArray urls = url.optJSONArray("urls");
                if (urls != null && urls.length() > 0) {
                    profileUrl = urls.optJSONObject(0).optString("display_url");
                }
            }
            JSONObject descrEntities = entities.optJSONObject("description");
            if (descrEntities != null) {
                JSONArray urls = descrEntities.optJSONArray("urls");
                if (urls != null) {
                    expandDescriptionUrls(urls);
                }
            }
        }
    }


    @Override
    public long getId() {
        return userID;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getScreenname() {
        return screenName;
    }

    @Override
    public long getCreatedAt() {
        return created;
    }

    @Override
    public String getImageUrl() {
        return profileImageUrl;
    }

    @Override
    public String getBannerUrl() {
        return profileBannerUrl;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public boolean isVerified() {
        return isVerified;
    }

    @Override
    public boolean isProtected() {
        return isLocked;
    }

    @Override
    public boolean followRequested() {
        return followReqSent;
    }

    @Override
    public int getFollowing() {
        return following;
    }

    @Override
    public int getFollower() {
        return follower;
    }

    @Override
    public int getTweetCount() {
        return tweetCount;
    }

    @Override
    public int getFavoriteCount() {
        return favorCount;
    }

    @Override
    public boolean hasDefaultProfileImage() {
        return defaultImage;
    }

    @Override
    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    /**
     * set time of account creation
     *
     * @param dateStr date string from twitter
     */
    private void setDate(String dateStr) {
        try {
            Date date = sdf.parse(dateStr);
            if (date != null)
                created = date.getTime();
        } catch (Exception e) {
            // make date invalid so it will be not shown
            e.printStackTrace();
        }
    }

    /**
     * expand URLs in the user description
     *
     * @param urls json object with url information
     */
    private void expandDescriptionUrls(@NonNull JSONArray urls) throws JSONException {
        StringBuilder builder = new StringBuilder(description);
        // twitter counts emojis twice so the indices have an offset
        int offset = 0;
        for (int c = 0 ; c < description.length() - 1 ; c++) {
            // determine if a pair of chars represent an emoji
            if (Character.isSurrogatePair(description.charAt(c), description.charAt(c + 1))) {
                offset++;
            }
        }
        // replace new line symbol with new line character
        for (int i = urls.length() - 1; i >= 0; i--) {
            JSONObject entry = urls.getJSONObject(i);
            String link = entry.getString("expanded_url");
            JSONArray indices = entry.getJSONArray("indices");
            int start = indices.getInt(0) + offset;
            int end = indices.getInt(1) + offset;
            builder.replace(start, end, link);
        }
        this.description = builder.toString();
    }
}