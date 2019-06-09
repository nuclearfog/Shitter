package org.nuclearfog.twidda.backend.items;

import androidx.annotation.Nullable;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

public class Tweet {

    private static final String PHOTO = "photo";
    private static final String VIDEO = "video";
    private static final String ANGIF = "animated_gif";

    private final long tweetID;
    private final long time;
    private final String tweet;
    private final String[] medias;
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
        medias = getMediaLinks(status);
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
                 String replyName, long replyUserId, String[] medias, String source, long replyID,
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
        this.medias = medias;
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
     * get medias links of tweet
     *
     * @return medias links array
     */
    public String[] getMediaLinks() {
        return medias;
    }

    /**
     * check if tweet contains media
     *
     * @return true if tweet contains media
     */
    public boolean hasMedia() {
        return medias != null && medias.length > 0;
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
        for (int i = 0; i < medias.length; i++) {
            MediaEntity mediaEntity = mediaEntities[i];
            switch (mediaEntity.getType()) {
                case PHOTO:
                    medias[i] = mediaEntity.getMediaURLHttps();
                    break;

                case VIDEO:
                    for (MediaEntity.Variant type : mediaEntity.getVideoVariants()) {
                        if (type.getContentType().equals("application/x-mpegURL"))
                            medias[i] = type.getUrl();
                    }
                    break;

                case ANGIF:
                    medias[i] = mediaEntity.getVideoVariants()[0].getUrl();
                    break;
            }
        }
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
        URLEntity urlEntities[] = status.getURLEntities();
        MediaEntity mediaEntities[] = status.getMediaEntities();
        StringBuilder tweet = new StringBuilder(status.getText());
        for (int i = urlEntities.length - 1; i >= 0; i--)
            tweet = tweet.replace(urlEntities[i].getStart(), urlEntities[i].getEnd(), urlEntities[i].getExpandedURL());
        for (int i = mediaEntities.length - 1; i >= 0; i--)
            tweet = tweet.delete(mediaEntities[i].getStart(), mediaEntities[i].getEnd());
        return tweet.toString();
    }
}