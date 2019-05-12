package org.nuclearfog.twidda.backend.items;

import androidx.annotation.Nullable;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

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
    private final long myRetweetId;
    private boolean retweeted;
    private boolean favored;


    public Tweet(Status status) {
        tweetID = status.getId();
        user = new TwitterUser(status.getUser());
        retweetCount = status.getRetweetCount();
        favoriteCount = status.getFavoriteCount();
        tweet = getText(status);
        time = status.getCreatedAt().getTime();
        replyID = status.getInReplyToStatusId();
        replyName = '@' + status.getInReplyToScreenName();
        media = getMediaLinks(status);
        retweeted = status.isRetweeted();
        favored = status.isFavorited();
        myRetweetId = status.getCurrentUserRetweetId();
        replyUserId = status.getInReplyToUserId();

        String api = status.getSource();
        api = api.substring(api.indexOf('>') + 1);
        api = api.substring(0, api.indexOf('<'));
        source = api;

        if (status.getRetweetedStatus() != null)
            embedded = new Tweet(status.getRetweetedStatus());
        else
            embedded = null;
    }


    public Tweet(long tweetID, int retweetCount, int favoriteCount, TwitterUser user, String tweet, long time,
                 String replyName, long replyUserId, String[] media, String source, long replyID,
                 Tweet embedded, long myRetweetId, boolean retweeted, boolean favored) {
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
        this.myRetweetId = myRetweetId;
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
    public String getTweet() {
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
        return myRetweetId;
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

    /**
     * @param status Twitter4J status
     * @return Array of Medialinks
     */
    private String[] getMediaLinks(Status status) {
        MediaEntity[] mediaEntities = status.getMediaEntities();
        String[] medias = new String[mediaEntities.length];
        for (int i = 0; i < medias.length; i++)
            medias[i] = mediaEntities[i].getMediaURLHttps();
        return medias;
    }

    /**
     * set retweet false
     *
     * @return tweet
     */
    public Tweet removeRetweet() {
        retweeted = false;
        return this;
    }

    /**
     * Resolve shortened tweet links
     *
     * @param status Tweet
     * @return Tweet string with resolved URL entities
     */
    private String getText(Status status) {
        URLEntity entities[] = status.getURLEntities();
        StringBuilder tweet = new StringBuilder(status.getText());
        for (int i = entities.length - 1; i >= 0; i--)
            tweet.replace(entities[i].getStart(), entities[i].getEnd(), entities[i].getExpandedURL());
        return tweet.toString();
    }
}