package org.nuclearfog.twidda.backend.apiold;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.User;

import twitter4j.URLEntity;

/**
 * User implementation for Twitter4J
 *
 * @author nuclearfog
 */
class UserV1 implements User {

    private long userID;
    private long created;
    private int following;
    private int follower;
    private int tweetCount;
    private int favorCount;
    private String username = "";
    private String screenName = "";
    private String description = "";
    private String location = "";
    private String profileUrl = "";
    private String profileImageUrl = "";
    private String bannerImageUrl = "";
    private boolean isCurrentUser;
    private boolean isVerified;
    private boolean isLocked;
    private boolean followRequested;
    private boolean hasDefaultImage;


    UserV1(twitter4j.User user, long twitterId) {
        String bannerLink = user.getProfileBannerURL();
        String bio = user.getDescription();

        if (user.getName() != null)
            this.username = user.getName();
        if (user.getScreenName() != null)
            this.screenName = '@' + user.getScreenName();
        if (user.getOriginalProfileImageURLHttps() != null)
            this.profileImageUrl = user.getOriginalProfileImageURLHttps();
        if (bannerLink != null && bannerLink.length() > 4)
            bannerImageUrl = bannerLink.substring(0, bannerLink.length() - 4);
        if (user.getURLEntity().getExpandedURL() != null)
            this.profileUrl = user.getURLEntity().getExpandedURL();
        if (user.getLocation() != null)
            this.location = user.getLocation();
        if (bio != null && !bio.isEmpty()) {
            URLEntity[] entities = user.getDescriptionURLEntities();
            StringBuilder builder = new StringBuilder(user.getDescription());
            for (int i = entities.length - 1; i >= 0; i--) {
                URLEntity entity = entities[i];
                builder.replace(entity.getStart(), entity.getEnd(), entity.getExpandedURL());
            }
            this.description = builder.toString();
        }
        userID = user.getId();
        isVerified = user.isVerified();
        isLocked = user.isProtected();
        created = user.getCreatedAt().getTime();
        following = user.getFriendsCount();
        follower = user.getFollowersCount();
        tweetCount = user.getStatusesCount();
        favorCount = user.getFavouritesCount();
        followRequested = user.isFollowRequestSent();
        hasDefaultImage = user.isDefaultProfileImage();
        isCurrentUser = twitterId == userID;
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
        return bannerImageUrl;
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
        return followRequested;
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
        return hasDefaultImage;
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
        return username + " " + screenName;
    }
}