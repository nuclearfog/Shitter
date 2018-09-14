package org.nuclearfog.twidda.backend.listitems;

public class TwitterUser {
    public final String username;
    public final String screenname;
    public final String bio;
    public final String profileImg;
    public final String bannerImg;
    public final String location;
    public final String link;
    public final boolean isVerified;
    public final boolean isLocked;
    public final long userID;       // Unique User ID
    public final long created;      // User since
    public final int following;     // Following count
    public final int follower;      // Follower count

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
}