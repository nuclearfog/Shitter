package org.nuclearfog.twidda.backend.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Tweet class containing information about a tweet
 *
 * @author nuclearfog
 */
public class Tweet implements Serializable {

    /**
     * type of media attached to the tweet
     */
    public enum MediaType {
        IMAGE,
        VIDEO,
        GIF,
        NONE
    }

    private static final String PHOTO = "photo";
    private static final String VIDEO = "video";
    private static final String ANGIF = "animated_gif";
    private static final String MEDIA_VIDEO = "video/mp4";

    private long tweetID;
    private long time;

    private User user;
    @Nullable
    private Tweet embedded;
    private MediaType mediaType;

    private long replyID;
    private long replyUserId;

    private int retweetCount;
    private int favoriteCount;
    private long myRetweetId;
    private boolean retweeted;
    private boolean favorited;
    private boolean sensitiveMedia;

    private String[] medias = {};
    private String locationName = "";
    private String locationCoordinates = "";
    private String replyName = "";
    private String tweet = "";
    private String source = "";

    /**
     * @param status    tweet
     * @param twitterId ID of the current user
     */
    public Tweet(Status status, long twitterId) {
        if (status.getRetweetedStatus() != null) {
            Status retweet = status.getRetweetedStatus();
            embedded = new Tweet(retweet, twitterId);
            this.retweetCount = retweet.getRetweetCount();
            this.favoriteCount = retweet.getFavoriteCount();
        } else {
            this.retweetCount = status.getRetweetCount();
            this.favoriteCount = status.getFavoriteCount();
        }
        retweeted = status.isRetweeted();
        favorited = status.isFavorited();
        myRetweetId = status.getCurrentUserRetweetId();
        setTweet(status, twitterId);
    }

    /**
     * @param status        twitter4j status
     * @param retweetCount  set retweet count
     * @param retweeted     set if tweet is retweeted by current user
     * @param favoriteCount set favor count
     * @param favored       set if tweet is favored by current user
     */
    public Tweet(Status status, long twitterId, long myRetweetId, int retweetCount, boolean retweeted, int favoriteCount, boolean favored) {
        if (status.getRetweetedStatus() != null) {
            Status retweet = status.getRetweetedStatus();
            embedded = new Tweet(retweet, twitterId, myRetweetId, retweetCount, retweeted, favoriteCount, favored);
        }
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
        this.myRetweetId = myRetweetId;
        this.retweeted = retweeted;
        this.favorited = favored;
        setTweet(status, tweetID);
    }

    /**
     * Tweet constructor for database tweets
     *
     * @param tweetID        unique id of tweet
     * @param retweetCount   number of retweets
     * @param favoriteCount  number of favors
     * @param user           tweet author
     * @param tweet          tweet text
     * @param time           time long format
     * @param replyName      author's name of replied tweet
     * @param replyUserId    quthor's ID of replied tweet
     * @param medias         Media links attached to tweet
     * @param source         used API of the tweet
     * @param replyID        ID of replied tweet
     * @param embedded       quoted tweet
     * @param myRetweetId    ID of the current users retweeted tweet
     * @param retweeted      tweet is retweeted by current user
     * @param favored        tweet is favored by current user
     * @param sensitiveMedia tweet contains sensitie media content
     * @param geo            location gps coordinates
     * @param place          location full place name
     */
    public Tweet(long tweetID, int retweetCount, int favoriteCount, User user, String tweet, long time, String replyName,
                 long replyUserId, String[] medias, MediaType mediaType, String source, long replyID, @Nullable Tweet embedded,
                 long myRetweetId, boolean retweeted, boolean favored, boolean sensitiveMedia, String place, String geo) {

        if (tweet != null)
            this.tweet = tweet;
        if (source != null)
            this.source = source;
        if (replyName != null)
            this.replyName = replyName;
        if (place != null)
            this.locationName = place;
        if (geo != null)
            this.locationCoordinates = geo;
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
        this.favorited = favored;
        this.sensitiveMedia = sensitiveMedia;
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
    public User getUser() {
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
     * @return tweet retweeted by this tweet
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
    public int getFavoriteCount() {
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
     * @return media type or NONE if there isn't any media
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * check if tweet contains text
     *
     * @return true if text is set
     */
    public boolean containsTweetText() {
        return !tweet.trim().isEmpty();
    }

    /**
     * check if tweet contains sensitive media
     *
     * @return true if media has sensitive conent
     */
    public boolean containsSensitiveMedia() {
        return sensitiveMedia;
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
        return favorited;
    }

    /**
     * check if Tweet is owned by the current user
     *
     * @return true if current user is author of the Tweet
     */
    public boolean currentUserIsOwner() {
        return user.isCurrentUser();
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
     * set tweet information
     *
     * @param status Tweet
     */
    private void setTweet(Status status, long twitterId) {
        tweetID = status.getId();
        time = status.getCreatedAt().getTime();
        user = new User(status.getUser(), status.getUser().getId() == twitterId);
        replyID = status.getInReplyToStatusId();
        replyUserId = status.getInReplyToUserId();
        sensitiveMedia = status.isPossiblySensitive();

        if (status.getInReplyToScreenName() != null)
            replyName = '@' + status.getInReplyToScreenName();
        if (status.getMediaEntities() != null)
            getMedia(status.getMediaEntities());
        if (status.getPlace() != null && status.getPlace().getFullName() != null)
            locationName = status.getPlace().getFullName();
        if (status.getGeoLocation() != null)
            locationCoordinates = status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude();
        if (status.getText() != null) {
            StringBuilder tweet = new StringBuilder(status.getText());
            // expand shortened links
            URLEntity[] urlEntities = status.getURLEntities();
            if (urlEntities != null && urlEntities.length > 0) {
                for (int i = urlEntities.length - 1; i >= 0; i--) { // expand shorten links
                    int start = urlEntities[i].getStart();
                    int end = urlEntities[i].getEnd();
                    String expanded = urlEntities[i].getExpandedURL();
                    tweet = tweet.replace(start, end, expanded);
                }
            }
            // remove twitter media link from tweet
            MediaEntity[] mediaEntities = status.getMediaEntities();
            if (mediaEntities != null && mediaEntities.length > 0) {
                int linkPos = tweet.indexOf("https://t.co/");
                if (linkPos >= 0)
                    tweet.delete(linkPos, tweet.length());
            }
            this.tweet = tweet.toString();
        }
        // remove HTML tag
        if (status.getSource() != null) {
            source = "" + status.getSource();
            int start = source.indexOf('>') + 1;
            int end = source.lastIndexOf('<');
            if (start > 0 && end > start)
                source = source.substring(start, end);
        }
    }

    /**
     * add media information to the Tweet
     *
     * @param mediaEntities media information
     */
    private void getMedia(MediaEntity[] mediaEntities) {
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


    @Override
    public boolean equals(Object object) {
        if (object instanceof Tweet) {
            Tweet tweet = (Tweet) object;
            return tweet.tweetID == tweetID;
        }
        return false;
    }


    @NonNull
    @Override
    public String toString() {
        return user.getScreenname() + ": " + tweet;
    }
}