package org.nuclearfog.twidda.backend.api.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.User;

import java.util.regex.Pattern;

/**
 * User implementation of API V2
 *
 * @author nuclearfog
 */
public class UserV2 implements User {

    public static final long serialVersionUID = 1136243062864162774L;

    private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

    /**
     * extra parameters required to fetch additional data
     */
    public static final String PARAMS = "user.fields=profile_image_url%2Cpublic_metrics%2Cverified" +
            "%2Cprotected%2Cdescription%2Ccreated_at%2Curl%2Centities";

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
    private int likeCount;
    private boolean isCurrentUser;
    private boolean isVerified;
    private boolean isProtected;
    private boolean followReqSent;
    private boolean defaultImage;

    /**
     * @param json      JSON object containing user data
     * @param twitterId ID of the current user
     */
    public UserV2(JSONObject json, long twitterId) throws JSONException {
        String idStr = json.getString("id");
        if (ID_PATTERN.matcher(idStr).matches()) {
            id = Long.parseLong(idStr);
        } else {
            throw new JSONException("bad ID: " + idStr);
        }
        username = json.optString("name");
        screenName = '@' + json.optString("username");
        isProtected = json.optBoolean("protected");
        location = json.optString("location");
        isVerified = json.optBoolean("verified");
        profileImageUrl = json.optString("profile_image_url");
        profileBannerUrl = json.optString("profile_banner_url");
        created = StringTools.getTime2(json.optString("created_at"));
        description = getDescription(json);
        url = getUrl(json);
        isCurrentUser = id == twitterId;

        JSONObject metrics = json.optJSONObject("public_metrics");
        if (metrics != null) {
            following = metrics.optInt("following_count");
            follower = metrics.optInt("followers_count");
            tweetCount = metrics.optInt("tweet_count");
        }

        // not yet implemented in API 2.0
        // todo check if Twitter added these values
        likeCount = -1;
        followReqSent = false;
        defaultImage = false;
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
        return likeCount;
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
        return "name=\"" + screenName + "\"";
    }

    /**
     * expand URLs of the user description
     *
     * @param json root json object of user v1
     * @return user description
     */
    private String getDescription(JSONObject json) {
        String description = json.optString("description");
        JSONObject entities = json.optJSONObject("entities");
        if (entities != null) {
            try {
                JSONObject descrEntities = entities.getJSONObject("description");
                JSONArray urls = descrEntities.getJSONArray("urls");
                // expand shortened urls
                StringBuilder builder = new StringBuilder(description);
                for (int i = urls.length() - 1; i >= 0; i--) {
                    JSONObject entry = urls.getJSONObject(i);
                    String link = entry.getString("expanded_url");
                    int start = entry.getInt("start");
                    int end = entry.getInt("end");
                    int offset = StringTools.calculateIndexOffset(description, start);
                    builder.replace(start + offset, end + offset, link);
                }
                return builder.toString();
            } catch (JSONException e) {
                // ignore, use default description
            }
        }
        return description;
    }

    /**
     * get expanded profile url
     *
     * @param json root json object of user v1
     * @return expanded url
     */
    private String getUrl(JSONObject json) {
        JSONObject entities = json.optJSONObject("entities");
        if (entities != null) {
            JSONObject urlJson = entities.optJSONObject("url");
            if (urlJson != null) {
                try {
                    JSONArray urls = urlJson.getJSONArray("urls");
                    if (urls.length() > 0) {
                        return urls.getJSONObject(0).getString("display_url");
                    }
                } catch (JSONException e) {
                    // ignore
                }
            }
        }
        return "";
    }
}