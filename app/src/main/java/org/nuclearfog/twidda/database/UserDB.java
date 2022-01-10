package org.nuclearfog.twidda.database;

import static org.nuclearfog.twidda.database.AppDatabase.*;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.User;

class UserDB implements User {

    private long userID;
    private long created;

    private boolean isCurrentUser;
    private boolean isVerified;
    private boolean isLocked;
    private boolean followReqSent;
    private boolean defaultImage;

    private int following;
    private int follower;

    private int tweetCount;
    private int favorCount;

    private String username;
    private String screenName;

    private String bio;
    private String location;
    private String link;

    private String profileImg;
    private String bannerImg;

    UserDB(Cursor cursor, long currentUserId) {
        this(cursor, "", currentUserId);
    }

    UserDB(Cursor cursor, String prefix, long currentUserId) {
        userID = cursor.getLong(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.ID));
        username = cursor.getString(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.USERNAME));
        screenName = cursor.getString(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.SCREENNAME));
        profileImg = cursor.getString(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.IMAGE));
        bio = cursor.getString(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.DESCRIPTION));
        link = cursor.getString(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.LINK));
        location = cursor.getString(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.LOCATION));
        bannerImg = cursor.getString(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.BANNER));
        created = cursor.getLong(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.SINCE));
        following = cursor.getInt(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.FRIENDS));
        follower = cursor.getInt(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.FOLLOWER));
        tweetCount = cursor.getInt(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.TWEETS));
        favorCount = cursor.getInt(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserTable.FAVORS));
        int register = cursor.getInt(cursor.getColumnIndexOrThrow(prefix + DatabaseAdapter.UserRegisterTable.REGISTER));
        isVerified = (register & VER_MASK) != 0;
        isLocked = (register & LCK_MASK) != 0;
        followReqSent = (register & FRQ_MASK) != 0;
        defaultImage = (register & DEF_IMG) != 0;
        isCurrentUser = currentUserId == userID;
    }


    @Override
    public long getId() {
        return userID;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getScreenname() {
        return screenName;
    }

    @Override
    public long getCreatedAt() {
        return created;
    }

    @Override
    public String getImageUrl() {
        return profileImg;
    }

    @Override
    public String getBannerUrl() {
        return bannerImg;
    }

    @Override
    public String getDescription() {
        return bio;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getProfileUrl() {
        return link;
    }

    @Override
    public boolean isVerified() {
        return isVerified;
    }

    @Override
    public boolean isProtected() {
        return isLocked;
    }

    @Override
    public boolean followRequested() {
        return followReqSent;
    }

    @Override
    public int getFollowing() {
        return following;
    }

    @Override
    public int getFollower() {
        return follower;
    }

    @Override
    public int getTweetCount() {
        return tweetCount;
    }

    @Override
    public int getFavoriteCount() {
        return favorCount;
    }

    @Override
    public boolean hasDefaultProfileImage() {
        return defaultImage;
    }

    @Override
    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    @Override
    public int compareTo(User user) {
        return Long.compare(user.getId(), userID);
    }

    @NonNull
    @Override
    public String toString() {
        return username + " " + screenName;
    }
}
