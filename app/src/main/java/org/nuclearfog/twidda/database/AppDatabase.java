package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.items.Trend;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.User;
import org.nuclearfog.twidda.backend.lists.MessageList;

import java.util.LinkedList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static org.nuclearfog.twidda.database.DatabaseAdapter.ANSWER_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.HOMETL_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MENTION_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.MESSAGE_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.SINGLE_TWEET_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.STATUS_EXIST_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TREND_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.TWEETFLAG_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USERFAVORIT_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USERFLAG_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USERTWEET_QUERY;
import static org.nuclearfog.twidda.database.DatabaseAdapter.USER_QUERY;

/**
 * Connection Class to SQLite Database of the app
 * All tweet, user and message information are stored here
 *
 * @author nuclearfog
 */
public class AppDatabase {

    // Tweet flags
    private static final int FAV_MASK = 1;          //  tweet is favored by user
    private static final int RTW_MASK = 1 << 1;     //  tweet is retweeted by user
    private static final int HOM_MASK = 1 << 2;     //  tweet is from home timeline
    private static final int MEN_MASK = 1 << 3;     //  tweet is from mention timeline
    private static final int UTW_MASK = 1 << 4;     //  tweet is from an users timeline
    private static final int RPL_MASK = 1 << 5;     //  tweet is from a reply timeline

    // Media content flags
    private static final int MEDIA_IMAGE_MASK = 1 << 6; // tweet contains images
    private static final int MEDIA_VIDEO_MASK = 2 << 6; // tweet contains a video
    private static final int MEDIA_ANGIF_MASK = 3 << 6; // tweet contains an animation
    private static final int MEDIA_SENS_MASK = 1 << 8;  // tweet contains sensitive media

    // user flags
    private static final int VER_MASK = 1;          //  user is verified
    private static final int LCK_MASK = 1 << 1;     //  user is private
    private static final int FRQ_MASK = 1 << 2;     //  a follow request is pending
    private static final int EXCL_USR = 1 << 3;     //  user excluded from mention timeline
    private static final int DEF_IMG = 1 << 4;      //  user has a default profile image

    private final int limit;       //  DATABASE ENTRY limit
    private final long homeId;

    private DatabaseAdapter dataHelper;

    public AppDatabase(Context context) {
        dataHelper = DatabaseAdapter.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        homeId = settings.getCurrentUserId();
        limit = settings.getListSize();
    }

    /**
     * Store user information
     *
     * @param user Twitter user
     */
    public void storeUser(User user) {
        SQLiteDatabase db = getDbWrite();
        storeUser(user, db, CONFLICT_REPLACE);
        commit(db);
    }

    /**
     * store home timeline
     *
     * @param home tweet from home timeline
     */
    public void storeHomeTimeline(List<Tweet> home) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : home)
            storeStatus(tweet, HOM_MASK, db);
        commit(db);
    }

    /**
     * store mentions
     *
     * @param mentions tweets
     */
    public void storeMentions(List<Tweet> mentions) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : mentions)
            storeStatus(tweet, MEN_MASK, db);
        commit(db);
    }

    /**
     * store user timeline
     *
     * @param stats user timeline
     */
    public void storeUserTweets(List<Tweet> stats) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : stats)
            storeStatus(tweet, UTW_MASK, db);
        commit(db);
    }

    /**
     * store user favors
     *
     * @param fav     tweet favored by user
     * @param ownerId user ID
     */
    public void storeUserFavs(List<Tweet> fav, long ownerId) {
        SQLiteDatabase db = getDbWrite();
        removeOldFavorites(db, ownerId);
        for (Tweet tweet : fav) {
            storeStatus(tweet, 0, db);
            storeFavorite(tweet.getId(), ownerId, db);
        }
        commit(db);
    }

    /**
     * store replies of a tweet
     *
     * @param replies tweet replies
     */
    public void storeReplies(List<Tweet> replies) {
        SQLiteDatabase db = getDbWrite();
        for (Tweet tweet : replies)
            storeStatus(tweet, RPL_MASK, db);
        commit(db);
    }

    /**
     * store location specific trends
     *
     * @param trends List of Trends
     * @param woeId  Yahoo World ID
     */
    public void storeTrends(List<Trend> trends, int woeId) {
        String[] args = new String[]{Integer.toString(woeId)};
        SQLiteDatabase db = getDbWrite();
        db.delete("trend", "woeid=?", args);
        for (Trend trend : trends) {
            ContentValues trendColumn = new ContentValues();
            trendColumn.put("woeID", woeId);
            trendColumn.put("vol", trend.getRange());
            trendColumn.put("trendname", trend.getName());
            trendColumn.put("trendpos", trend.getRank());
            db.insertWithOnConflict("trend", null, trendColumn, CONFLICT_REPLACE);
        }
        commit(db);
    }

    /**
     * store tweet ID of a favored tweet by the current user
     *
     * @param tweet favored tweet
     */
    public void storeFavorite(Tweet tweet) {
        if (tweet.getEmbeddedTweet() != null)
            tweet = tweet.getEmbeddedTweet();
        SQLiteDatabase db = getDbWrite();
        storeStatus(tweet, 0, db);
        storeFavorite(tweet.getId(), homeId, db);
        commit(db);
    }

    /**
     * store direct messages
     *
     * @param messages list of direct messages
     */
    public void storeMessage(List<Message> messages) {
        SQLiteDatabase db = getDbWrite();
        for (Message message : messages)
            storeMessage(message, db);
        commit(db);
    }

    /**
     * load home timeline
     *
     * @return tweet list
     */
    public List<Tweet> getHomeTimeline() {
        String[] args = {Integer.toString(HOM_MASK), Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(HOMETL_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * load mentions
     *
     * @return tweet list
     */
    public List<Tweet> getMentions() {
        String[] args = {Integer.toString(MEN_MASK), Integer.toString(EXCL_USR), Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(MENTION_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * load user timeline
     *
     * @param userID user ID
     * @return Tweet list of user tweets
     */
    public List<Tweet> getUserTweets(long userID) {
        String[] args = {Integer.toString(UTW_MASK), Long.toString(userID), Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(USERTWEET_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * load user favored tweets
     *
     * @param ownerID user ID
     * @return favored tweets by user
     */
    public List<Tweet> getUserFavs(long ownerID) {
        String[] args = {Long.toString(ownerID), Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(USERFAVORIT_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * get user information
     *
     * @param userId ID of user
     * @return user information or null if not found
     */
    @Nullable
    public User getUser(long userId) {
        SQLiteDatabase db = getDbRead();
        return getUser(userId, db);
    }

    /**
     * load status
     *
     * @param tweetId tweet ID
     * @return tweet or null if not found
     */
    @Nullable
    public Tweet getStatus(long tweetId) {
        String[] args = {Long.toString(tweetId)};

        SQLiteDatabase db = getDbRead();
        Tweet result = null;
        Cursor cursor = db.rawQuery(SINGLE_TWEET_QUERY, args);
        if (cursor.moveToFirst())
            result = getStatus(cursor);
        cursor.close();
        return result;
    }

    /**
     * get tweet answers
     *
     * @param tweetId Tweet ID
     * @return list of tweet answers
     */
    public List<Tweet> getAnswers(long tweetId) {
        String[] args = {Long.toString(tweetId), Integer.toString(RPL_MASK),
                Integer.toString(EXCL_USR), Integer.toString(limit)};

        SQLiteDatabase db = getDbRead();
        List<Tweet> tweetList = new LinkedList<>();
        Cursor cursor = db.rawQuery(ANSWER_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                Tweet tweet = getStatus(cursor);
                tweetList.add(tweet);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tweetList;
    }

    /**
     * update status and author information
     *
     * @param tweet Tweet
     */
    public void updateStatus(Tweet tweet) {
        SQLiteDatabase db = getDbWrite();
        updateStatus(tweet, db);
        if (tweet.getEmbeddedTweet() != null)
            updateStatus(tweet.getEmbeddedTweet(), db);
        commit(db);
    }

    /**
     * remove status
     *
     * @param tweetId Tweet ID
     */
    public void removeStatus(long tweetId) {
        String[] args = {Long.toString(tweetId)};

        SQLiteDatabase db = getDbWrite();
        db.delete("tweet", "tweetID=?", args);
        db.delete("favorit", "tweetID=?", args);
        commit(db);
    }

    /**
     * remove status from favorites
     *
     * @param tweet Tweet to remove from the favorites
     */
    public void removeFavorite(Tweet tweet) {
        long tweetId = tweet.getId();
        if (tweet.getEmbeddedTweet() != null)
            tweetId = tweet.getEmbeddedTweet().getId();
        String[] delArgs = {Long.toString(tweetId), Long.toString(homeId)};
        String[] updateArgs = {Long.toString(tweetId)};

        SQLiteDatabase db = getDbWrite();
        int flags = getTweetFlags(db, tweetId);
        flags &= ~FAV_MASK;
        ContentValues status = new ContentValues();
        status.put("statusregister", flags);
        db.delete("favorit", "tweetID=? AND ownerID=?", delArgs);
        db.update("tweet", status, "tweet.tweetID=?", updateArgs);
        commit(db);
    }

    /**
     * Delete Direct Message
     *
     * @param id Direct Message ID
     */
    public void deleteMessage(long id) {
        String[] messageId = {Long.toString(id)};

        SQLiteDatabase db = getDbWrite();
        db.delete("message", "messageID=?", messageId);
        commit(db);
    }

    /**
     * Load trend List
     *
     * @param woeId Yahoo World ID
     * @return list of trends
     */
    public List<Trend> getTrends(int woeId) {
        String[] args = {Integer.toString(woeId)};

        List<Trend> trends = new LinkedList<>();
        SQLiteDatabase db = getDbRead();
        Cursor cursor = db.rawQuery(TREND_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("trendname");
                String trendName = cursor.getString(index);
                index = cursor.getColumnIndex("vol");
                int vol = cursor.getInt(index);
                index = cursor.getColumnIndex("trendpos");
                int pos = cursor.getInt(index);
                trends.add(new Trend(trendName, vol, pos));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trends;
    }

    /**
     * load direct messages
     *
     * @return list of direct messages
     */
    public MessageList getMessages() {
        String[] args = {Integer.toString(limit)};
        // TODO get next cursor from database
        MessageList result = new MessageList(null, null);
        SQLiteDatabase db = getDbRead();
        Cursor cursor = db.rawQuery(MESSAGE_QUERY, args);
        if (cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("senderID");
                long senderID = cursor.getLong(index);
                index = cursor.getColumnIndex("receiverID");
                long receiverID = cursor.getLong(index);
                index = cursor.getColumnIndex("message");
                String message = cursor.getString(index);
                index = cursor.getColumnIndex("time");
                long time = cursor.getLong(index);
                index = cursor.getColumnIndex("messageID");
                long messageId = cursor.getLong(index);

                User sender = getUser(senderID, db);
                User receiver = getUser(receiverID, db);

                result.add(new Message(messageId, sender, receiver, time, message));

            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /**
     * check if tweet exists in database
     *
     * @param id Tweet ID
     * @return true if found
     */
    public boolean containStatus(long id) {
        SQLiteDatabase db = getDbRead();
        return containStatus(id, db);
    }

    /**
     * remove user from mention results
     *
     * @param id   user ID
     * @param mute true remove user tweets from mention results
     */
    public void muteUser(long id, boolean mute) {
        String[] args = new String[]{Long.toString(id)};

        SQLiteDatabase db = getDbWrite();
        int flags = getUserFlags(db, id);
        if (mute)
            flags |= EXCL_USR;
        else
            flags &= ~EXCL_USR;

        ContentValues userColumn = new ContentValues();
        userColumn.put("userregister", flags);
        db.update("user", userColumn, "user.userID=?", args);
        commit(db);
    }

    /**
     * get tweet information from database
     *
     * @param cursor cursor containing tweet informations
     * @return tweet instance
     */
    private Tweet getStatus(Cursor cursor) {
        long time = cursor.getLong(cursor.getColumnIndex("time"));
        String tweettext = cursor.getString(cursor.getColumnIndex("tweet"));
        int retweet = cursor.getInt(cursor.getColumnIndex("retweet"));
        int favorit = cursor.getInt(cursor.getColumnIndex("favorite"));
        long tweetId = cursor.getLong(cursor.getColumnIndex("tweetID"));
        long retweetId = cursor.getLong(cursor.getColumnIndex("retweetID"));
        String replyname = cursor.getString(cursor.getColumnIndex("replyname"));
        long replyStatusId = cursor.getLong(cursor.getColumnIndex("replyID"));
        long retweeterId = cursor.getLong(cursor.getColumnIndex("retweeterID"));
        String source = cursor.getString(cursor.getColumnIndex("source"));
        String medialinks = cursor.getString(cursor.getColumnIndex("media"));
        String place = cursor.getString(cursor.getColumnIndex("place"));
        String geo = cursor.getString(cursor.getColumnIndex("geo"));
        long replyUserId = cursor.getLong(cursor.getColumnIndex("replyUserID"));
        int statusregister = cursor.getInt(cursor.getColumnIndex("statusregister"));
        boolean favorited = (statusregister & FAV_MASK) != 0;
        boolean retweeted = (statusregister & RTW_MASK) != 0;
        boolean sensitive = (statusregister & MEDIA_SENS_MASK) != 0;
        String[] medias = parseMedia(medialinks);
        // get media type
        Tweet.MediaType mediaType = Tweet.MediaType.NONE;
        if ((statusregister & MEDIA_ANGIF_MASK) == MEDIA_ANGIF_MASK)
            mediaType = Tweet.MediaType.GIF;
        else if ((statusregister & MEDIA_IMAGE_MASK) == MEDIA_IMAGE_MASK)
            mediaType = Tweet.MediaType.IMAGE;
        else if ((statusregister & MEDIA_VIDEO_MASK) == MEDIA_VIDEO_MASK)
            mediaType = Tweet.MediaType.VIDEO;
        User user = getUser(cursor);
        Tweet embeddedTweet = null;
        if (retweetId > 1)
            embeddedTweet = getStatus(retweetId);
        return new Tweet(tweetId, retweet, favorit, user, tweettext, time, replyname, replyUserId, medias,
                mediaType, source, replyStatusId, embeddedTweet, retweeterId, retweeted, favorited, sensitive, place, geo);
    }

    /**
     * get user information from table
     *
     * @param userId Id of the user
     * @param db     SQLITE DB
     * @return user instance
     */
    @Nullable
    private User getUser(long userId, SQLiteDatabase db) {
        String[] args = {Long.toString(userId)};

        User user = null;
        Cursor cursor = db.rawQuery(USER_QUERY, args);
        if (cursor.moveToFirst())
            user = getUser(cursor);
        cursor.close();
        return user;
    }

    /**
     * get user information from database
     *
     * @param cursor cursor containing user data
     * @return user instance
     */
    private User getUser(Cursor cursor) {
        long userId = cursor.getLong(cursor.getColumnIndex("userID"));
        String username = cursor.getString(cursor.getColumnIndex("username"));
        String screenname = cursor.getString(cursor.getColumnIndex("scrname"));
        int userRegister = cursor.getInt(cursor.getColumnIndex("userregister"));
        String profileImg = cursor.getString(cursor.getColumnIndex("pbLink"));
        String bio = cursor.getString(cursor.getColumnIndex("bio"));
        String link = cursor.getString(cursor.getColumnIndex("link"));
        String location = cursor.getString(cursor.getColumnIndex("location"));
        String banner = cursor.getString(cursor.getColumnIndex("banner"));
        long createdAt = cursor.getLong(cursor.getColumnIndex("createdAt"));
        int following = cursor.getInt(cursor.getColumnIndex("following"));
        int follower = cursor.getInt(cursor.getColumnIndex("follower"));
        int tCount = cursor.getInt(cursor.getColumnIndex("tweetCount"));
        int fCount = cursor.getInt(cursor.getColumnIndex("favorCount"));
        boolean isCurrentUser = homeId == userId;
        boolean isVerified = (userRegister & VER_MASK) != 0;
        boolean isLocked = (userRegister & LCK_MASK) != 0;
        boolean isReq = (userRegister & FRQ_MASK) != 0;
        boolean defaultImg = (userRegister & DEF_IMG) != 0;
        return new User(userId, username, screenname, profileImg, bio, location, isCurrentUser, isVerified,
                isLocked, isReq, defaultImg, link, banner, createdAt, following, follower, tCount, fCount);
    }

    /**
     * store user information into database
     *
     * @param user user information
     * @param db   SQLITE DB
     * @param mode SQLITE mode {@link SQLiteDatabase#CONFLICT_IGNORE} or {@link SQLiteDatabase#CONFLICT_REPLACE}
     */
    private void storeUser(User user, SQLiteDatabase db, int mode) {
        ContentValues userColumn = new ContentValues();
        int flags = getUserFlags(db, user.getId());
        if (user.isVerified())
            flags |= VER_MASK;
        else
            flags &= ~VER_MASK;
        if (user.isLocked())
            flags |= LCK_MASK;
        else
            flags &= ~LCK_MASK;
        if (user.followRequested())
            flags |= FRQ_MASK;
        else
            flags &= ~FRQ_MASK;
        if (user.hasDefaultProfileImage())
            flags |= DEF_IMG;
        else
            flags &= ~DEF_IMG;

        userColumn.put("userID", user.getId());
        userColumn.put("username", user.getUsername());
        userColumn.put("scrname", user.getScreenname());
        userColumn.put("pbLink", user.getImageLink());
        userColumn.put("userregister", flags);
        userColumn.put("bio", user.getBio());
        userColumn.put("link", user.getLink());
        userColumn.put("location", user.getLocation());
        userColumn.put("banner", user.getBannerLink());
        userColumn.put("createdAt", user.getCreatedAt());
        userColumn.put("following", user.getFollowing());
        userColumn.put("follower", user.getFollower());
        userColumn.put("tweetCount", user.getTweetCount());
        userColumn.put("favorCount", user.getFavorCount());

        db.insertWithOnConflict("user", null, userColumn, mode);
    }


    /**
     * save tweet into database
     *
     * @param tweet          Tweet information
     * @param statusRegister predefined statusregister or "0" if there isn't one
     * @param db             SQLite database
     */
    private void storeStatus(Tweet tweet, int statusRegister, SQLiteDatabase db) {
        ContentValues status = new ContentValues();
        User user = tweet.getUser();
        Tweet rtStat = tweet.getEmbeddedTweet();
        long rtId = -1L;

        if (rtStat != null) {
            storeStatus(rtStat, 0, db);
            rtId = rtStat.getId();
        }
        statusRegister |= getTweetFlags(db, tweet.getId());
        if (tweet.favored()) {
            statusRegister |= FAV_MASK;
        } else {
            statusRegister &= ~FAV_MASK;
        }
        if (tweet.retweeted()) {
            statusRegister |= RTW_MASK;
        } else {
            statusRegister &= ~RTW_MASK;
        }
        if (tweet.containsSensitiveMedia()) {
            statusRegister |= MEDIA_SENS_MASK;
        } else {
            statusRegister &= ~MEDIA_SENS_MASK;
        }
        switch (tweet.getMediaType()) {
            case IMAGE:
                statusRegister |= MEDIA_IMAGE_MASK;
                break;

            case VIDEO:
                statusRegister |= MEDIA_VIDEO_MASK;
                break;

            case GIF:
                statusRegister |= MEDIA_ANGIF_MASK;
                break;
        }

        status.put("media", getMediaLinks(tweet));
        status.put("statusregister", statusRegister);
        status.put("tweetID", tweet.getId());
        status.put("userID", user.getId());
        status.put("time", tweet.getTime());
        status.put("tweet", tweet.getTweet());
        status.put("retweetID", rtId);
        status.put("source", tweet.getSource());
        status.put("replyID", tweet.getReplyId());
        status.put("retweet", tweet.getRetweetCount());
        status.put("favorite", tweet.getFavoriteCount());
        status.put("retweeterID", tweet.getMyRetweetId());
        status.put("replyUserID", tweet.getReplyUserId());
        status.put("place", tweet.getLocationName());
        status.put("geo", tweet.getLocationCoordinates());
        status.put("replyUserID", tweet.getReplyUserId());
        if (tweet.getReplyUserId() > 0)
            status.put("replyname", tweet.getReplyName());
        storeUser(user, db, CONFLICT_IGNORE);
        db.insertWithOnConflict("tweet", null, status, CONFLICT_REPLACE);
    }

    /**
     * updates existing tweet
     *
     * @param tweet update of the tweet
     * @param db    database instance
     */
    private void updateStatus(Tweet tweet, SQLiteDatabase db) {
        String[] tweetIdArg = {Long.toString(tweet.getId())};
        String[] userIdArg = {Long.toString(tweet.getUser().getId())};

        ContentValues statColumn = new ContentValues();
        ContentValues userColumn = new ContentValues();
        int flags = getTweetFlags(db, tweet.getId());
        if (tweet.retweeted())
            flags |= RTW_MASK;
        else
            flags &= ~RTW_MASK;
        if (tweet.favored())
            flags |= FAV_MASK;
        else
            flags &= ~FAV_MASK;
        statColumn.put("tweet", tweet.getTweet());
        statColumn.put("retweet", tweet.getRetweetCount());
        statColumn.put("favorite", tweet.getFavoriteCount());
        statColumn.put("retweeterID", tweet.getMyRetweetId());
        statColumn.put("replyname", tweet.getReplyName());
        statColumn.put("statusregister", flags);
        statColumn.put("media", getMediaLinks(tweet));

        User user = tweet.getUser();
        userColumn.put("username", user.getUsername());
        userColumn.put("scrname", user.getScreenname());
        userColumn.put("pbLink", user.getImageLink());
        userColumn.put("bio", user.getBio());
        userColumn.put("link", user.getLink());
        userColumn.put("location", user.getLocation());
        userColumn.put("banner", user.getBannerLink());
        userColumn.put("following", user.getFollowing());
        userColumn.put("follower", user.getFollower());

        db.update("tweet", statColumn, "tweet.tweetID=?", tweetIdArg);
        db.update("user", userColumn, "user.userID=?", userIdArg);
    }

    /**
     * Store Tweet into favorite table of a user
     *
     * @param tweetId ID of the favored tweet
     * @param ownerId ID of the favorite list owner
     * @param db      database instance
     */
    private void storeFavorite(long tweetId, long ownerId, SQLiteDatabase db) {
        ContentValues favTable = new ContentValues();
        favTable.put("tweetID", tweetId);
        favTable.put("ownerID", ownerId);
        db.insertWithOnConflict("favorit", null, favTable, CONFLICT_REPLACE);
    }

    /**
     * clear old favorites from table
     *
     * @param db     database instance
     * @param userId ID of the favorite list owner
     */
    private void removeOldFavorites(SQLiteDatabase db, long userId) {
        String[] delArgs = {Long.toString(userId)};
        db.delete("favorit", "ownerID=?", delArgs);
    }

    /**
     * store direct message
     *
     * @param message direct message information
     * @param db      database instance
     */
    private void storeMessage(Message message, SQLiteDatabase db) {
        ContentValues messageColumn = new ContentValues();
        messageColumn.put("messageID", message.getId());
        messageColumn.put("time", message.getTime());
        messageColumn.put("senderID", message.getSender().getId());
        messageColumn.put("receiverID", message.getReceiver().getId());
        messageColumn.put("message", message.getText());
        storeUser(message.getSender(), db, CONFLICT_IGNORE);
        storeUser(message.getReceiver(), db, CONFLICT_IGNORE);
        db.insertWithOnConflict("message", null, messageColumn, CONFLICT_IGNORE);
    }

    /**
     * get statusregister of a tweet or "0" if tweet was not found
     *
     * @param db      database instance
     * @param tweetID ID of the tweet
     * @return tweet flags
     */
    private int getTweetFlags(SQLiteDatabase db, long tweetID) {
        String[] args = {Long.toString(tweetID)};

        Cursor c = db.rawQuery(TWEETFLAG_QUERY, args);
        int result = 0;
        if (c.moveToFirst()) {
            int pos = c.getColumnIndex("statusregister");
            result = c.getInt(pos);
        }
        c.close();
        return result;
    }

    /**
     * get flags of a twitter user or "0" if user was not found
     *
     * @param db     database instance
     * @param userID ID of the user
     * @return user flags
     */
    private int getUserFlags(SQLiteDatabase db, long userID) {
        String[] args = {Long.toString(userID)};

        Cursor c = db.rawQuery(USERFLAG_QUERY, args);
        int result = 0;
        if (c.moveToFirst()) {
            int pos = c.getColumnIndex("userregister");
            result = c.getInt(pos);
        }
        c.close();
        return result;
    }

    /**
     * check if tweet exists in database
     *
     * @param id Tweet ID
     * @param db database instance
     * @return true if found
     */
    private boolean containStatus(long id, SQLiteDatabase db) {
        String[] args = {Long.toString(id)};

        Cursor c = db.rawQuery(STATUS_EXIST_QUERY, args);
        boolean result = c.moveToFirst();
        c.close();
        return result;
    }

    /**
     * Get SQLite instance for reading database
     *
     * @return SQLite instance
     */
    private synchronized SQLiteDatabase getDbRead() {
        return dataHelper.getDatabase();
    }

    /**
     * GET SQLite instance for writing database
     *
     * @return SQLite instance
     */
    private synchronized SQLiteDatabase getDbWrite() {
        SQLiteDatabase db = dataHelper.getDatabase();
        db.beginTransaction();
        return db;
    }

    /**
     * Commit changes and close Database
     *
     * @param db database instance
     */
    private synchronized void commit(SQLiteDatabase db) {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Parse string where media links are separated by ' ; '
     *
     * @param media tweet media link
     * @return array of media links
     */
    private String[] parseMedia(String media) {
        int index;
        List<String> links = new LinkedList<>();
        do {
            index = media.indexOf(';');
            if (index > 0 && index < media.length()) {
                links.add(media.substring(0, index));
                media = media.substring(index + 1);
            }
        } while (index > 0);
        return links.toArray(new String[0]);
    }

    /**
     * create string where media links are separated by ' ; '
     *
     * @param tweet tweet informations
     * @return String of media links
     */
    private String getMediaLinks(Tweet tweet) {
        StringBuilder media = new StringBuilder();
        for (String link : tweet.getMediaLinks())
            media.append(link).append(";");
        return media.toString();
    }
}