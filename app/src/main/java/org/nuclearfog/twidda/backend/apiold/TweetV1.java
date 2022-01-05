package org.nuclearfog.twidda.backend.apiold;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Tweet implementation for Twitter4J
 *
 * @author nuclearfog
 */
class TweetV1 implements Tweet {

    private static final String MIME_PHOTO = "photo";
    private static final String MIME_VIDEO = "video";
    private static final String MIME_ANGIF = "animated_gif";
    private static final String MIME_V_MP4 = "video/mp4";

    private long tweetID;
    private long time;
    @Nullable
    private TweetV1 embedded;
    private User user;
    private long replyId;
    private long replyUserId;
    private long myRetweetId;
    private int retweetCount;
    private int favoriteCount;
    private int mediaType;
    private String[] mediaLinks = {};
    private String userMentions = "";
    private String locationName = "";
    private String locationCoordinates = "";
    private String replyName = "";
    private String tweet = "";
    private String source = "";
    private boolean retweeted;
    private boolean favorited;
    private boolean sensitiveMedia;

    /**
     * @param status    tweet
     * @param twitterId ID of the current user
     */
    TweetV1(Status status, long twitterId) {
        if (status.getRetweetedStatus() != null) {
            Status retweet = status.getRetweetedStatus();
            embedded = new TweetV1(retweet, twitterId);
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
    TweetV1(Status status, long twitterId, long myRetweetId, int retweetCount, boolean retweeted, int favoriteCount, boolean favored) {
        if (status.getRetweetedStatus() != null) {
            Status retweet = status.getRetweetedStatus();
            embedded = new TweetV1(retweet, twitterId, myRetweetId, retweetCount, retweeted, favoriteCount, favored);
        }
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
        this.myRetweetId = myRetweetId;
        this.retweeted = retweeted;
        this.favorited = favored;
        setTweet(status, tweetID);
    }

    @Override
    public long getId() {
        return tweetID;
    }

    @Override
    public String getTweet() {
        return tweet;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Nullable
    @Override
    public Tweet getEmbeddedTweet() {
        return embedded;
    }

    @Override
    public String getReplyName() {
        return replyName;
    }

    @Override
    public long getReplyUserId() {
        return replyUserId;
    }

    @Override
    public long getReplyId() {
        return replyId;
    }

    @Override
    public long getMyRetweetId() {
        return myRetweetId;
    }

    @Override
    public int getRetweetCount() {
        return retweetCount;
    }

    @Override
    public int getFavoriteCount() {
        return favoriteCount;
    }

    @Override
    public String[] getMediaLinks() {
        return mediaLinks;
    }

    @Override
    public String getMentionedUsers() {
        return userMentions;
    }

    @Override
    public int getMediaType() {
        return mediaType;
    }

    @Override
    public boolean isSensitive() {
        return sensitiveMedia;
    }

    @Override
    public boolean isRetweeted() {
        return retweeted;
    }

    @Override
    public boolean isFavorited() {
        return favorited;
    }

    @Override
    public String getLocationName() {
        return locationName;
    }

    @Override
    public String getLocationCoordinates() {
        return locationCoordinates;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TweetV1) {
            TweetV1 tweet = (TweetV1) object;
            return tweet.tweetID == tweetID;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "from:" + user.getScreenname() + " text:" + tweet;
    }

    /**
     * set tweet information
     *
     * @param status Tweet
     */
    private void setTweet(Status status, long twitterId) {
        tweetID = status.getId();
        time = status.getCreatedAt().getTime();
        user = new UserV1(status.getUser(), twitterId);
        replyId = status.getInReplyToStatusId();
        replyUserId = status.getInReplyToUserId();
        sensitiveMedia = status.isPossiblySensitive();
        // add screen name of the replied user
        if (status.getInReplyToScreenName() != null)
            replyName = '@' + status.getInReplyToScreenName();
        // add media links
        if (status.getMediaEntities() != null)
            getMedia(status.getMediaEntities());
        // add location information
        if (status.getPlace() != null && status.getPlace().getFullName() != null)
            locationName = status.getPlace().getFullName();
        if (status.getGeoLocation() != null)
            locationCoordinates = status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude();
        // build tweet text, expand all URLs
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
        // remove xml tag from source string
        String source = status.getSource();
        if (source != null) {
            int start = source.indexOf('>') + 1;
            int end = source.lastIndexOf('<');
            if (start > 0 && end > start)
                this.source = source.substring(start, end);
        }
        // add reply mention
        StringBuilder userMentions = new StringBuilder(17 /*max screen name length*/);
        if (!user.isCurrentUser()) {
            // prevent self mentioning
            userMentions.append(user.getScreenname()).append(' ');
        }
        // add user mentions
        UserMentionEntity[] mentionedUsers = status.getUserMentionEntities();
        if (mentionedUsers != null && mentionedUsers.length > 0) {
            for (UserMentionEntity mention : mentionedUsers) {
                if (mention.getId() != twitterId && userMentions.indexOf(mention.getScreenName()) < 0) {
                    // filter out current user's screen name and duplicates
                    userMentions.append('@').append(mention.getScreenName()).append(' ');
                }
            }
        }
        this.userMentions = userMentions.toString();
    }

    /**
     * add media information to the Tweet
     *
     * @param mediaEntities media information
     */
    private void getMedia(MediaEntity[] mediaEntities) {
        mediaLinks = new String[mediaEntities.length];
        if (mediaLinks.length == 0) {
            mediaType = NONE;
        } else {
            switch (mediaEntities[0].getType()) {
                case MIME_PHOTO:
                    mediaType = IMAGE;
                    for (int i = 0; i < mediaEntities.length; i++) {
                        mediaLinks[i] = mediaEntities[i].getMediaURLHttps();
                    }
                    break;

                case MIME_VIDEO:
                    mediaType = VIDEO;
                    for (MediaEntity.Variant type : mediaEntities[0].getVideoVariants()) {
                        if (type.getContentType().equals(MIME_V_MP4)) {
                            // get link with selected video format
                            // a tweet can only have one video
                            mediaLinks[0] = type.getUrl();
                        }
                    }
                    break;

                case MIME_ANGIF:
                    mediaType = GIF;
                    mediaLinks[0] = mediaEntities[0].getVideoVariants()[0].getUrl();
                    break;

                default:
                    mediaType = NONE;
                    break;
            }
        }
    }
}