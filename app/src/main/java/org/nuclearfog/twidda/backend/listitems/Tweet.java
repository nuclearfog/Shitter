package org.nuclearfog.twidda.backend.listitems;

public class Tweet {
    public final long tweetID, userID;
    public final Tweet embedded;
    public final String username, screenname, profileImg, tweet, replyName,source;
    public final long time, replyID;
    public final int retweet, favorit;
    public final boolean verified, retweeted, favorized;
    public final String[] media;

    public Tweet(long tweetID, long userID, String username, String screenname, int retweet, int favorit,
                 String profileImg, String tweet, long time, String replyName, String[] media, String source,
                 long replyID, boolean verified, Tweet embedded, boolean retweeted, boolean favorized) {
        this.tweetID = tweetID;
        this.userID = userID;
        this.username = username;
        this.screenname = '@'+screenname;
        this.profileImg = profileImg;
        this.retweet = retweet;
        this.favorit = favorit;
        this.tweet = tweet;
        this.time = time;
        this.replyID = replyID;
        this.verified = verified;
        this.embedded = embedded;
        this.favorized = favorized;
        this.retweeted = retweeted;
        this.replyName = replyName;
        this.media = media;
        this.source = source;
    }
}