package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.URLEntity;

public class Tweet {

    public enum MediaType {
        IMAGE,
        VIDEO,
        GIF,
        NONE
    }

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

    private final MediaType mediaType;


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
        myRetweetId = status.getCurrentUserRetweetId();
        replyUserId = status.getInReplyToUserId();

        // remove HTML tag
        String api = "" + status.getSource();
        int start = api.indexOf('>') + 1;
        int end = api.lastIndexOf('<');
        if (start > 0 && end > start)
            api = api.substring(start, end);
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
        if (status.getInReplyToScreenName() != null)
            replyName = '@' + status.getInReplyToScreenName();
        else
            replyName = "";
        if (status.getRetweetedStatus() != null)
            embedded = new Tweet(status.getRetweetedStatus());
        else
            embedded = null;

        MediaEntity[] mediaEntities = status.getMediaEntities();
        medias = new String[mediaEntities.length];
        if (medias.length == 0) {
            mediaType = MediaType.NONE;
        } else {
            switch (mediaEntities[0].getType()) {
                case PHOTO:
                    mediaType = MediaType.IMAGE;
                    for (int i = 0; i < mediaEntities.length; i++)
                        medias[i] = mediaEntities[i].getMediaURLHttps();
                    break;

                case VIDEO:
                    mediaType = MediaType.VIDEO;
                    for (MediaEntity.Variant type : mediaEntities[0].getVideoVariants()) {
                        if (type.getContentType().equals(MEDIA_VIDEO))
                            medias[0] = type.getUrl();
                    }
                    break;

                case ANGIF:
                    mediaType = MediaType.GIF;
                    medias[0] = mediaEntities[0].getVideoVariants()[0].getUrl();
                    break;

                default:
                    mediaType = MediaType.NONE;
                    break;
            }
        }
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
     * @param geo location gps coordinates
     * @param place location full place name
     */
    public Tweet(long tweetID, int retweetCount, int favoriteCount, TwitterUser user, String tweet, long time,
                 String replyName, long replyUserId, String[] medias, MediaType mediaType, String source, long replyID,
                 Tweet embedded, long myRetweetId, boolean retweeted, boolean favored, String place, String geo) {

        this.tweetID = tweetID;
        this.user = user;
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
        this.time = time;
        this.replyID = replyID;
        this.embedded = embedded;
        this.medias = medias;
        this.mediaType = mediaType;
        this.retweeted = retweeted;
        this.favored = favored;
        this.myRetweetId = myRetweetId;
        this.replyUserId = replyUserId;
        this.tweet = tweet != null ? tweet : "";
        this.source = source != null ? source : "";
        this.replyName = replyName != null ? replyName : "";
        this.locationName = place != null ? place : "";
        this.locationCoordinates = geo != null ? geo : "";
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
     * check tweet media type
     *
     * @return media type or NONE if there isnt any media
     */
    public MediaType getMediaType() {
        return mediaType;
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
     * Resolve shortened tweet links
     *
     * @param status Tweet
     * @return Tweet string with resolved URL entities
     */
    private String getText(Status status) {
        URLEntity[] urlEntities = status.getURLEntities();
        MediaEntity[] mediaEntities = status.getMediaEntities();
        StringBuilder tweet = new StringBuilder("" + status.getText());
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

    @NonNull
    @Override
    public String toString() {
        return user.getScreenname() + ": " + tweet;
    }
}