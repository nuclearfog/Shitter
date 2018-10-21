package org.nuclearfog.twidda.backend.items;

public class TwitterUser {
    private final String username;
    private final String screenname;
    private final String bio;
    private final String profileImg;
    private final String bannerImg;
    private final String location;
    private final String link;
    private final boolean isVerified;
    private final boolean isLocked;
    private final long userID;
    private final long created;
    private final int following;
    private final int follower;

    public TwitterUser(long userID, String username, String screenname, String profileImg,
                       String bio, String location, boolean isVerified, boolean isLocked, String link,
                       String bannerImg, long created, int following, int follower) {
        this.userID = userID;
        this.username = username;
        this.screenname = '@' + screenname;
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
        return screenname;
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
     * get Profile image link
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
}