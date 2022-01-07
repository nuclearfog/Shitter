package org.nuclearfog.twidda.backend.api;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.User;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * implementation of User accessed by API 2.0
 *
 * @author nuclearfog
 */
class UserV2 implements User {

    /**
     * extra parameters required to fetch additional data
     */
    public static final String PARAMS = "user.fields=profile_image_url";
    // ISO8601 time format used by twitter
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

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
    private boolean isCurrentUser;
    private boolean isVerified;
    private boolean isProtected;
    private boolean followReqSent;
    private boolean defaultImage;


    UserV2(JSONObject json, long twitterId) {
        userID = json.optLong("id");
        username = json.optString("name");
        screenName = json.optString("username"); // username -> screenname
        isProtected = json.optBoolean("protected");
        location = json.optString("location");
        profileUrl = json.optString("profile_image_url");
        description = json.optString("description");
        isVerified = json.optBoolean("verified");
        profileImageUrl = json.optString("profile_image_url");
        following = json.optInt("public_metrics.following_count");
        follower = json.optInt("public_metrics.followers_count");
        tweetCount = json.optInt("public_metrics.tweet_count");
        isCurrentUser = userID == twitterId;
        setDate(json.optString("created_at"));
        // not defined jet in V2
        profileBannerUrl = "";
        favorCount = 0;
        followReqSent = false;
        defaultImage = true;
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
        return isProtected;
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
            created = sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            // make date invalid so it will be not shown
            e.printStackTrace();
        }
    }
}
