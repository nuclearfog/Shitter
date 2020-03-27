package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

import twitter4j.URLEntity;
import twitter4j.User;

public class TwitterUser {
    private final long userID;
    private final long created;

    private final String username;
    private final String screenname;

    private final boolean isVerified;
    private final boolean isLocked;
    private final boolean isFollowReqSent;

    private final int following;
    private final int follower;

    private final int tweetCount;
    private final int favorCount;

    private final String bio;
    private final String location;
    private final String link;

    private final String profileImg;
    private final String bannerImg;

    public TwitterUser(User user) {
        userID = user.getId();
        username = user.getName();
        screenname = '@' + user.getScreenName();
        profileImg = "" + user.getOriginalProfileImageURLHttps();
        bio = getBio(user);
        link = "" + user.getURLEntity().getExpandedURL();
        location = "" + user.getLocation();
        bannerImg = "" + user.getProfileBannerURL();
        isVerified = user.isVerified();
        isLocked = user.isProtected();
        created = user.getCreatedAt().getTime();
        following = user.getFriendsCount();
        follower = user.getFollowersCount();
        tweetCount = user.getStatusesCount();
        favorCount = user.getFavouritesCount();
        isFollowReqSent = user.isFollowRequestSent();
    }

    public TwitterUser(long userID, String username, String screenname, String profileImg, String bio, String location,
                       boolean isVerified, boolean isLocked, boolean isFollowReqSent, String link, String bannerImg,
                       long created, int following, int follower, int tweetCount, int favorCount) {

        this.userID = userID;
        this.username = username;
        this.screenname = screenname;
        this.profileImg = profileImg;
        this.bio = bio;
        this.link = link;
        this.location = location;
        this.bannerImg = bannerImg;
        this.isVerified = isVerified;
        this.isLocked = isLocked;
        this.created = created;
        this.following = following;
        this.follower = follower;
        this.tweetCount = tweetCount;
        this.favorCount = favorCount;
        this.isFollowReqSent = isFollowReqSent;
    }

    /**
     * get user id
     * @return id
     */
    public long getId() {
        return userID;
    }

    /**
     * get User name
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * get @screenname
     * @return screen name
     */
    public String getScreenname() {
        return screenname;
    }

    /**
     * get date of creation
     * @return raw time
     */
    public long getCreatedAt() {
        return created;
    }

    /**
     * get Profile image_add link
     * @return link
     */
    public String getImageLink() {
        return profileImg;
    }

    /**
     * get banner image_add link
     * @return link
     */
    public String getBannerLink() {
        return bannerImg;
    }

    /**
     * get user bio
     * @return bio text
     */
    public String getBio() {
        return bio;
    }

    /**
     * get location name
     * @return name
     */
    public String getLocation() {
        return location;
    }

    /**
     * get link
     * @return link
     */
    public String getLink() {
        return link;
    }

    /**
     * user verified
     * @return if verified
     */
    public boolean isVerified() {
        return isVerified;
    }

    /**
     * user locked
     * @return if locked
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * requested follow
     * @return if a follow was requested
     */
    public boolean followRequested() {
        return isFollowReqSent;
    }

    /**
     * get following count
     * @return following
     */
    public int getFollowing() {
        return following;
    }

    /**
     * get follower count
     * @return follower count
     */
    public int getFollower() {
        return follower;
    }

    /**
     * get Tweet count of user
     * @return tweet count
     */
    public int getTweetCount() {
        return tweetCount;
    }

    /**
     * get count of favored tweets
     * @return tweet count
     */
    public int getFavorCount() {
        return favorCount;
    }

    /**
     * Resolve shortened links from user description
     * @param user Twitter user
     * @return Description String
     */
    private String getBio(User user) {
        URLEntity[] entities = user.getDescriptionURLEntities();
        StringBuilder bio = new StringBuilder("" + user.getDescription());
        for (int i = entities.length - 1; i >= 0; i--) {
            URLEntity entity = entities[i];
            bio.replace(entity.getStart(), entity.getEnd(), entity.getExpandedURL());
        }
        return bio.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return username + " " + screenname;
    }
}