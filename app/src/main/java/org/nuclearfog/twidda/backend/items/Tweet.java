package org.nuclearfog.twidda.backend.items;

import android.support.annotation.Nullable;

public class Tweet {
    private final TwitterUser user;
    private final Tweet embedded;
    private final long tweetID;
    private final String tweet;
    private final String replyName;
    private final String source;
    private final long time;
    private final long replyID;
    private final long retweetId;
    private final long replyUserId;
    private final int retweet;
    private final int favorit;
    private final String[] media;
    private final boolean retweeted;
    private final boolean favorized;


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


    /**
     * Tweet ID
     *
     * @return tweetID
     */
    public long getId() {
        return tweetID;
    }

    /**
     * Tweet Content
     *
     * @return tweet text
     */
    public String getText() {
        return tweet;
    }

    /**
     * get author
     *
     * @return tweet owner
     */
    public TwitterUser getUser() {
        return user;
    }

    /**
     * get time
     *
     * @return raw time
     */
    public long getTime() {
        return time;
    }

    /**
     * get used tweet api
     *
     * @return api name
     */
    public String getSource() {
        return source;
    }

    /**
     * get embedded Tweet
     *
     * @return tweet
     */
    @Nullable
    public Tweet getEmbeddedTweet() {
        return embedded;
    }

    /**
     * name of replied user
     *
     * @return username
     */
    public String getReplyName() {
        return replyName;
    }

    /**
     * ID of replied user
     *
     * @return user Id
     */
    public long getReplyUserId() {
        return replyUserId;
    }

    /**
     * ID of replied tweet
     *
     * @return tweet id
     */
    public long getReplyId() {
        return replyID;
    }

    /**
     * ID of my retweet
     *
     * @return tweet ID
     */
    public long getMyRetweetId() {
        return retweetId;
    }

    /**
     * get number of retweets
     *
     * @return retweet count
     */
    public int getRetweetCount() {
        return retweet;
    }

    /**
     * get number of favors
     *
     * @return favor count
     */
    public int getFavorCount() {
        return favorit;
    }

    /**
     * get media links of tweet
     *
     * @return media links array
     */
    public String[] getMediaLinks() {
        return media;
    }

    /**
     * is tweet retweeted by me
     *
     * @return retweet status
     */
    public boolean retweeted() {
        return retweeted;
    }

    /**
     * is tweet favored by me
     *
     * @return favor status
     */
    public boolean favorized() {
        return favorized;
    }
}