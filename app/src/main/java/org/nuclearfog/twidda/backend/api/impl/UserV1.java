package org.nuclearfog.twidda.backend.api.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
public class UserV1 implements User {

    private static final long serialVersionUID = 7893496988800499358L;

    private long id;
    private long created;
    private String username;
    private String screenName;
    private String description;
    private String location;
    private String url;
    private String profileImageUrl;
    private String profileBannerUrl;
    private int following;
    private int follower;
    private int tweetCount;
    private int favoriteCount;
    private boolean isVerified;
    private boolean isLocked;
    private boolean followReqSent;
    private boolean defaultImage;
    private boolean isCurrentUser = true;


    public UserV1(JSONObject json, long twitterId) {
        this(json);
        isCurrentUser = twitterId == id;
    }


    public UserV1(JSONObject json) {
        id = Long.parseLong(json.optString("id_str", "-1"));
        username = json.optString("name");
        screenName = '@' + json.optString("screen_name");
        isVerified = json.optBoolean("verified");
        isLocked = json.optBoolean("protected");
        profileImageUrl = getProfileImage(json);
        profileBannerUrl = json.optString("profile_banner_url");
        description = getDescription(json);
        location = json.optString("location");
        following = json.optInt("friends_count");
        follower = json.optInt("followers_count");
        tweetCount = json.optInt("statuses_count");
        favoriteCount = json.optInt("favourites_count");
        followReqSent = json.optBoolean("follow_request_sent");
        defaultImage = json.optBoolean("default_profile_image");
        created = StringTools.getTime1(json.optString("created_at"));
        url = getUrl(json);
    }

    @Override
    public long getId() {
        return id;
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
        return url;
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
        return favoriteCount;
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
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof User))
            return false;
        return ((User) obj).getId() == id;
    }

    @NonNull
    @Override
    public String toString() {
        return "name:\"" + screenName + "\"";
    }

    /**
     * expand URLs of the user description
     *
     * @param json root json object of user v1
     * @return user description
     */
    private String getDescription(JSONObject json) {
        try {
            JSONObject entities = json.getJSONObject("entities");
            String description = json.getString("description");
            JSONObject descrEntities = entities.getJSONObject("description");
            JSONArray urls = descrEntities.getJSONArray("urls");

            // expand shortened urls
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
            return builder.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    /**
     * get expanded profile url
     *
     * @param json root json object of user v1
     * @return expanded url
     */
    private String getUrl(JSONObject json) {
        try {
            JSONObject entities = json.getJSONObject("entities");
            JSONObject urlJson = entities.getJSONObject("url");
            JSONArray urls = urlJson.getJSONArray("urls");
            if (urls.length() > 0) {
                return urls.getJSONObject(0).getString("display_url");
            }
        } catch (JSONException e) {
            // ignore
        }
        return "";
    }

    /**
     * get original sized profile image url
     *
     * @param json root json object of user v1
     * @return profile image url
     */
    private String getProfileImage(JSONObject json) {
        String profileImage = json.optString("profile_image_url_https");
        // set profile image url
        int start = profileImage.lastIndexOf('_');
        int end = profileImage.lastIndexOf('.');
        if (!defaultImage && start > 0 && end > 0)
            return profileImage.substring(0, start) + profileImage.substring(end);
        return profileImage;
    }
}