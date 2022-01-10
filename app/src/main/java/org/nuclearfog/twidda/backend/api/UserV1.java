package org.nuclearfog.twidda.backend.api;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.User;

/**
 * API 1.1 implementation of User
 *
 * @author nuclearfog
 */
class UserV1 implements User {

    static final String SKIP_STAT = "skip_status=true";
    static final String INCLUDE_ENTITIES = "include_entities=true";

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


    UserV1(JSONObject json, long twitterId) {
        this(json);
        isCurrentUser = twitterId == userID;
    }


    UserV1(JSONObject json) {
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
        created = StringTools.getTime(json.optString("created_at"));

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

    @Override
    public int compareTo(User user) {
        return Long.compare(user.getId(), userID);
    }

    @NonNull
    @Override
    public String toString() {
        return screenName + ":" + username;
    }

    /**
     * expand URLs in the user description
     *
     * @param urls json object with url information
     */
    private void expandDescriptionUrls(@NonNull JSONArray urls) {
        try {
            // replace new line symbol with new line character
            StringBuilder builder = new StringBuilder(description);
            for (int i = urls.length() - 1; i >= 0; i--) {
                JSONObject entry = urls.getJSONObject(i);
                String link = entry.getString("expanded_url");
                JSONArray indices = entry.getJSONArray("indices");
                int start = indices.getInt(0);
                int end = indices.getInt(1);
                int offset = StringTools.calculateIndexOffset(description, start);
                builder.replace(start + offset, end + offset, link);
            }
            this.description = builder.toString();
        } catch (JSONException e) {
            // use default description
        }
    }
}