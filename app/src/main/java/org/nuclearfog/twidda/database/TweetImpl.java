package org.nuclearfog.twidda.database;

import static org.nuclearfog.twidda.database.AppDatabase.*;

import android.database.Cursor;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

import java.util.regex.Pattern;

/**
 * Tweet implementation for database
 *
 * @author nuclearfog
 */
class TweetImpl implements Tweet {

    private static final Pattern SEPARATOR = Pattern.compile(";");

    private long tweetId;
    private long time;
    private long embeddedId;

    private User user;
    @Nullable
    private Tweet embedded;

    private long replyID;
    private long replyUserId;

    private int retweetCount;
    private int favoriteCount;
    private long myRetweetId;
    private boolean retweeted;
    private boolean favorited;
    private boolean sensitive;

    private String[] mediaLinks;
    private String mediaType;
    private String locationName;
    private String locationCoordinates;
    private String replyName;
    private String tweet;
    private String source;


    TweetImpl(Cursor cursor, long currentUserId) {
        time = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.SINCE));
        tweet = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.TWEET));
        retweetCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.RETWEET));
        favoriteCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.FAVORITE));
        tweetId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.ID));
        replyName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.REPLYNAME));
        replyID = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.REPLYTWEET));
        myRetweetId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.RETWEETUSER));
        source = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.SOURCE));
        String links = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.MEDIA));
        locationName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.PLACE));
        locationCoordinates = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.COORDINATE));
        replyUserId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.REPLYUSER));
        embeddedId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetTable.EMBEDDED));
        int tweetRegister = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.TweetRegisterTable.REGISTER));
        favorited = (tweetRegister & FAV_MASK) != 0;
        retweeted = (tweetRegister & RTW_MASK) != 0;
        sensitive = (tweetRegister & MEDIA_SENS_MASK) != 0;
        mediaLinks = SEPARATOR.split(links);
        // get media type
        if ((tweetRegister & MEDIA_ANGIF_MASK) == MEDIA_ANGIF_MASK)
            mediaType = MIME_ANGIF;
        else if ((tweetRegister & MEDIA_IMAGE_MASK) == MEDIA_IMAGE_MASK)
            mediaType = MIME_PHOTO;
        else if ((tweetRegister & MEDIA_VIDEO_MASK) == MEDIA_VIDEO_MASK)
            mediaType = MIME_VIDEO;
        else
            mediaType = MIME_NONE;
        this.user = new UserImpl(cursor, currentUserId);
    }

    @Override
    public long getId() {
        return tweetId;
    }

    @Override
    public String getText() {
        return tweet;
    }

    @Override
    public User getAuthor() {
        return user;
    }

    @Override
    public long getTimestamp() {
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
        return replyID;
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
    public String getUserMentions() {
        return replyName;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public boolean isSensitive() {
        return sensitive;
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

    /**
     * get ID of the embedded tweet
     *
     * @return ID of the
     */
    long getEmbeddedTweetId() {
        return embeddedId;
    }

    /**
     * attach tweet referenced by {@link #embeddedId}
     *
     * @param embedded embedded tweet
     */
    void addEmbeddedTweet(Tweet embedded) {
        this.embedded = embedded;
    }
}