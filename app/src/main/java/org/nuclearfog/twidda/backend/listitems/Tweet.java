package org.nuclearfog.twidda.backend.listitems;

import android.support.annotation.Nullable;

public class Tweet {
    public final TwitterUser user;  // Tweet Author
    public final Tweet embedded;    // Retweetet Tweet
    public final long tweetID;      // Unique Tweet ID
    public final String tweet;      // Tweet Text
    public final String replyName;  // Screenname of Answered User
    public final String source;     // Used Tweet API
    public final long time;         // Tweet Time in millisecond
    public final long replyID;      // Unique ID of Replied Tweet
    public final long retweetId;    // Unique ID of My retweet
    public final long replyUserId;  // ID of Replied User
    public final int retweet;       // Retweet Count
    public final int favorit;       // Favorite Count
    public final String[] media;    // Media Link container
    public final boolean retweeted; // Retweeted by me
    public final boolean favorized; // Favorited by me


    public Tweet(long tweetID, int retweet, int favorit, TwitterUser user, String tweet, long time,
                 String replyName, long replyUserId, String[] media, String source, long replyID,
                 @Nullable Tweet embedded, long retweetId, boolean retweeted, boolean favorized) {
        this.tweetID = tweetID;
        this.user = user;
        this.retweet = retweet;
        this.favorit = favorit;
        this.tweet = tweet;
        this.time = time;
        this.replyID = replyID;
        this.embedded = embedded;
        this.replyName = replyName;
        this.media = media;
        this.source = source;
        this.retweeted = retweeted;
        this.favorized = favorized;
        this.retweetId = retweetId;
        this.replyUserId = replyUserId;
    }
}