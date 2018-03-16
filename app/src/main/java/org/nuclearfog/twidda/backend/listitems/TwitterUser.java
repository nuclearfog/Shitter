package org.nuclearfog.twidda.backend.listitems;

public class TwitterUser {
    public final String username,screenname,bio;
    public final String profileImg,fullpb,bannerImg;
    public final String location,link;
    public final boolean isVerified,isLocked;
    public final long userID;
    public final long created;
    public final int following, follower;

    public TwitterUser(long userID, String username, String screenname, String profileImg,
                String fullpb, String bio, String location, boolean isVerified, boolean isLocked,
                String link, String bannerImg, long created, int following, int follower) {
        this.userID = userID;
        this.username = username;
        this.screenname = screenname;
        this.profileImg = profileImg;
        this.fullpb = fullpb;
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