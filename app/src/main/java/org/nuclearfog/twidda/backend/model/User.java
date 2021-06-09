package org.nuclearfog.twidda.backend.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

import twitter4j.URLEntity;

/**
 * Container class for a twitter user
 *
 * @author nuclearfog
 */
public class User implements Serializable {

    private long userID;
    private long created;

    private boolean isCurrentUser;
    private boolean isVerified;
    private boolean isLocked;
    private boolean isFollowReqSent;
    private boolean hasDefaultImage;

    private int following;
    private int follower;

    private int tweetCount;
    private int favorCount;

    private String username = "";
    private String screenName = "";

    private String bio = "";
    private String location = "";
    private String link = "";

    private String profileImg = "";
    private String bannerImg = "";

    /**
     * @param user      Twitter user
     * @param twitterId ID of the current user
     */
    public User(twitter4j.User user, long twitterId) {
        this(user, user.getId() == twitterId);
    }

    /**
     * @param user          Twitter user
     * @param isCurrentUser true if user is the authenticated user
     */
    public User(twitter4j.User user, boolean isCurrentUser) {
        String bannerLink = user.getProfileBannerURL();
        String bio = user.getDescription();

        if (user.getName() != null)
            this.username = user.getName();
        if (user.getScreenName() != null)
            this.screenName = '@' + user.getScreenName();
        if (user.getOriginalProfileImageURLHttps() != null)
            this.profileImg = user.getOriginalProfileImageURLHttps();
        if (user.getURLEntity().getExpandedURL() != null)
            this.link = user.getURLEntity().getExpandedURL();
        if (user.getLocation() != null)
            this.location = user.getLocation();
        if (bannerLink != null && bannerLink.length() > 4)
            bannerImg = bannerLink.substring(0, bannerLink.length() - 4);
        if (bio != null && !bio.isEmpty()) {
            URLEntity[] entities = user.getDescriptionURLEntities();
            StringBuilder builder = new StringBuilder(user.getDescription());
            for (int i = entities.length - 1; i >= 0; i--) {
                URLEntity entity = entities[i];
                builder.replace(entity.getStart(), entity.getEnd(), entity.getExpandedURL());
            }
            this.bio = builder.toString();
        }
        userID = user.getId();
        isVerified = user.isVerified();
        isLocked = user.isProtected();
        created = user.getCreatedAt().getTime();
        following = user.getFriendsCount();
        follower = user.getFollowersCount();
        tweetCount = user.getStatusesCount();
        favorCount = user.getFavouritesCount();
        isFollowReqSent = user.isFollowRequestSent();
        hasDefaultImage = user.isDefaultProfileImage();
        this.isCurrentUser = isCurrentUser;
    }

    /**
     * @param userID          ID of the user
     * @param username        username
     * @param screenName      screen name of the user
     * @param profileImg      profile image link
     * @param bio             bio of the user
     * @param location        location name
     * @param isCurrentUser   true if this user is the authenticated user
     * @param isVerified      true if user is verified
     * @param isLocked        true if users profile is locked
     * @param isFollowReqSent true if authenticated user has sent a follow request
     * @param hasDefaultImage true if user has not a profile image
     * @param link            internet link the user has set
     * @param bannerImg       link to the profile banner image
     * @param created         time where the profile was created
     * @param following       user count followed by the user
     * @param follower        follower count
     * @param tweetCount      number of tweets of the user
     * @param favorCount      number of tweets favored by the user
     */
    public User(long userID, String username, String screenName, String profileImg, String bio, String location, boolean isCurrentUser,
                boolean isVerified, boolean isLocked, boolean isFollowReqSent, boolean hasDefaultImage, String link,
                String bannerImg, long created, int following, int follower, int tweetCount, int favorCount) {

        if (username != null)
            this.username = username;
        if (screenName != null)
            this.screenName = screenName;
        if (profileImg != null)
            this.profileImg = profileImg;
        if (bio != null)
            this.bio = bio;
        if (link != null)
            this.link = link;
        if (location != null)
            this.location = location;
        if (bannerImg != null)
            this.bannerImg = bannerImg;
        this.userID = userID;
        this.isCurrentUser = isCurrentUser;
        this.isVerified = isVerified;
        this.isLocked = isLocked;
        this.created = created;
        this.following = following;
        this.follower = follower;
        this.tweetCount = tweetCount;
        this.favorCount = favorCount;
        this.isFollowReqSent = isFollowReqSent;
        this.hasDefaultImage = hasDefaultImage;
    }

    /**
     * get user id
     *
     * @return id
     */
    public long getId() {
        return userID;
    }

    /**
     * get User name
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * get @screenname
     *
     * @return screen name
     */
    public String getScreenname() {
        return screenName;
    }

    /**
     * get date of creation
     *
     * @return raw time
     */
    public long getCreatedAt() {
        return created;
    }

    /**
     * get profile image link
     *
     * @return link
     */
    public String getImageLink() {
        return profileImg;
    }

    /**
     * get banner image link
     *
     * @return link
     */
    public String getBannerLink() {
        return bannerImg;
    }

    /**
     * get user bio
     *
     * @return bio text
     */
    public String getBio() {
        return bio;
    }

    /**
     * get location name
     *
     * @return name
     */
    public String getLocation() {
        return location;
    }

    /**
     * get link
     *
     * @return link
     */
    public String getLink() {
        return link;
    }

    /**
     * check if user is the current user
     *
     * @return true if user is the current user logged in to twitter
     */
    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    /**
     * user verified
     *
     * @return if verified
     */
    public boolean isVerified() {
        return isVerified;
    }

    /**
     * user locked
     *
     * @return if locked
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * requested follow
     *
     * @return if a follow was requested
     */
    public boolean followRequested() {
        return isFollowReqSent;
    }

    /**
     * get following count
     *
     * @return following
     */
    public int getFollowing() {
        return following;
    }

    /**
     * get follower count
     *
     * @return follower count
     */
    public int getFollower() {
        return follower;
    }

    /**
     * get Tweet count of user
     *
     * @return tweet count
     */
    public int getTweetCount() {
        return tweetCount;
    }

    /**
     * get count of favored tweets
     *
     * @return tweet count
     */
    public int getFavorCount() {
        return favorCount;
    }

    /**
     * check if User has Profile image
     *
     * @return true if user has a profile image set
     */
    public boolean hasDefaultProfileImage() {
        return hasDefaultImage;
    }

    /**
     * check if user has a profile image
     *
     * @return true if user has a profile image
     */
    public boolean hasProfileImage() {
        return !profileImg.isEmpty();
    }

    /**
     * check if user has a banner image
     *
     * @return true if user has a banner image set
     */
    public boolean hasBannerImage() {
        return !bannerImg.isEmpty();
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            User user = (User) o;
            return user.userID == userID;
        }
        return false;
    }


    @NonNull
    @Override
    public String toString() {
        return username + " " + screenName;
    }
}