package org.nuclearfog.twidda.backend.items;

import android.support.annotation.Nullable;

public class Tweet {

    private final long tweetID;
    private final long time;
    private final String tweet;
    private final String[] media;
    private final String source;

    private final TwitterUser user;
    private final Tweet embedded;

    private final long replyID;
    private final long replyUserId;
    private final String replyName;

    private final int retweetCount;
    private final int favoriteCount;

    private final boolean retweeted;
    private final boolean favored;

    private final long retweetId;


    public Tweet(long tweetID, int retweetCount, int favoriteCount, TwitterUser user, String tweet, long time,
                 String replyName, long replyUserId, String[] media, String source, long replyID,
                 Tweet embedded, long retweetId, boolean retweeted, boolean favored) {
        this.tweetID = tweetID;
        this.user = user;
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
        this.tweet = tweet;
        this.time = time;
        this.replyID = replyID;
        this.embedded = embedded;
        this.replyName = replyName;
        this.media = media;
        this.source = source;
        this.retweeted = retweeted;
        this.favored = favored;
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
        return retweetCount;
    }

    /**
     * get number of favors
     *
     * @return favor count
     */
    public int getFavorCount() {
        return favoriteCount;
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
     * @return if status is retweeted
     */
    public boolean retweeted() {
        return retweeted;
    }

    /**
     * is tweet favored by me
     *
     * @return if status is favored
     */
    public boolean favored() {
        return favored;
    }

}