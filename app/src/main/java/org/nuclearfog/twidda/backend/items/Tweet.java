package org.nuclearfog.twidda.backend.items;

import androidx.annotation.Nullable;

import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.URLEntity;

public class Tweet {

    private static final String PHOTO = "photo";
    private static final String VIDEO = "video";
    private static final String ANGIF = "animated_gif";
    private static final String MEDIA_VIDEO = "application/x-mpegURL";

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
    private final boolean retweeted;
    private final boolean favored;

    private final String locationName;
    private final String locationCoordinates;


    /**
     * Tweet Constructor
     *
     * @param status Twitter4J status
     */
    public Tweet(Status status) {
        this(status, status.getRetweetCount(), status.isRetweeted(), status.getFavoriteCount(), status.isFavorited());
    }

    /**
     * Tweet constructor
     * @param status twitter4j status
     * @param retweetCount set retweet count
     * @param retweeted set if tweet is retweeted by current user
     * @param favoriteCount set favor count
     * @param favored set if tweet is favored by current user
     */
    public Tweet(Status status, int retweetCount, boolean retweeted, int favoriteCount, boolean favored) {
        this.retweetCount = retweetCount;
        this.retweeted = retweeted;
        this.favoriteCount = favoriteCount;
        this.favored = favored;
        tweetID = status.getId();
        user = new TwitterUser(status.getUser());
        tweet = getText(status);
        time = status.getCreatedAt().getTime();
        replyID = status.getInReplyToStatusId();
        medias = getMediaLinks(status);
        myRetweetId = status.getCurrentUserRetweetId();
        replyUserId = status.getInReplyToUserId();

        String api = status.getSource();
        api = api.substring(api.indexOf('>') + 1);
        api = api.substring(0, api.indexOf('<'));
        source = api;

        Place place = status.getPlace();
        GeoLocation geo = status.getGeoLocation();
        if (place != null)
            locationName = place.getFullName();
        else
            locationName = "";
        if (geo != null)
            locationCoordinates = geo.getLatitude() + "," + geo.getLongitude();
        else
            locationCoordinates = "";
        if (status.getInReplyToScreenName() == null)
            replyName = "";
        else
            replyName = '@' + status.getInReplyToScreenName();
        if (status.getRetweetedStatus() != null)
            embedded = new Tweet(status.getRetweetedStatus());
        else
            embedded = null;
    }

    /**
     * Tweet constructor for database tweets
     * @param tweetID unique id of tweet
     * @param retweetCount number of retweets
     * @param favoriteCount number of favors
     * @param user tweet author
     * @param tweet tweet text
     * @param time time long format
     * @param replyName author's name of replied tweet
     * @param replyUserId quthor's ID of replied tweet
     * @param medias Media links attached to tweet
     * @param source used API of the tweet
     * @param replyID ID of replied tweet
     * @param embedded quoted tweet
     * @param myRetweetId ID of the current users retweeted tweet
     * @param retweeted tweet is retweeted by current user
     * @param favored tweet is favored by current user
     * @param coordinates location gps coordinates
     * @param place location full place name
     */
    public Tweet(long tweetID, int retweetCount, int favoriteCount, TwitterUser user, String tweet, long time,
                 String replyName, long replyUserId, String[] medias, String source, long replyID,
                 Tweet embedded, long myRetweetId, boolean retweeted, boolean favored, String place, String coordinates) {
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
        this.locationName = place;
        this.locationCoordinates = coordinates;
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
     * get location of tweet if any
     *
     * @return full location name
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * get location coordinate
     *
     * @return latitude and longitude
     */
    public String getLocationCoordinates() {
        return locationCoordinates;
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
                        if (type.getContentType().equals(MEDIA_VIDEO))
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
     * Resolve shortened tweet links
     *
     * @param status Tweet
     * @return Tweet string with resolved URL entities
     */
    private String getText(Status status) {
        URLEntity[] urlEntities = status.getURLEntities();
        MediaEntity[] mediaEntities = status.getMediaEntities();
        StringBuilder tweet = new StringBuilder(status.getText());
        for (int i = urlEntities.length - 1; i >= 0; i--) { // expand shorten links
            int start = urlEntities[i].getStart();
            int end = urlEntities[i].getEnd();
            String expanded = urlEntities[i].getExpandedURL();
            tweet = tweet.replace(start, end, expanded);
        }
        if (mediaEntities.length > 0) { // remove twitter media links from tweet
            int linkpos = tweet.indexOf("https://t.co/");
            int lastpos = tweet.length();
            if (linkpos >= 0)
                tweet.delete(linkpos, lastpos);
        }
        return tweet.toString();
    }
}