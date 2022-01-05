package org.nuclearfog.twidda.backend.api;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.User;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
    private boolean isCurrentUser;
    private boolean isVerified;
    private boolean isLocked;
    private boolean followReqSent;
    private boolean defaultImage;


    UserV1(JSONObject json, long twitterId) {
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
        isCurrentUser = twitterId == userID;

        if (bannerLink.length() > 4)
            profileBannerUrl = bannerLink.substring(0, bannerLink.length() - 4);

        setDate(json.optString("created_at"));
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
            created = sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            // make date invalid so it will be not shown
            e.printStackTrace();
        }
    }
}
