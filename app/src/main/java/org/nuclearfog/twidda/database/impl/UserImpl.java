package org.nuclearfog.twidda.database.impl;

import static org.nuclearfog.twidda.database.AppDatabase.DEFAULT_IMAGE_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.FOLLOW_REQUEST_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.LOCKED_MASK;
import static org.nuclearfog.twidda.database.AppDatabase.VERIFIED_MASK;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.UserRegisterTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.User;

/**
 * database implementation of an user
 *
 * @author nuclearfog
 */
public class UserImpl implements User {

	private static final long serialVersionUID = 2367055336838212570L;

	private long id;
	private long createdAt;
	private int following;
	private int follower;
	private int statusCount;
	private int favorCount;
	private String username;
	private String screenName;
	private String bio;
	private String location;
	private String link;
	private String profileImageSmall;
	private String profileImageOrig;
	private String profileBannerSmall;
	private String profileBannerOrig;
	private boolean isCurrentUser;
	private boolean isVerified;
	private boolean isLocked;
	private boolean followReqSent;
	private boolean defaultImage;

	/**
	 * @param cursor  database cursor containing user column
	 * @param account current user login
	 */
	public UserImpl(Cursor cursor, Account account) {
		id = cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.ID));
		username = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.USERNAME));
		screenName = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.SCREENNAME));
		profileImageOrig = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.IMAGE));
		bio = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.DESCRIPTION));
		link = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.LINK));
		location = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.LOCATION));
		profileBannerOrig = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.BANNER));
		createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.SINCE));
		following = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.FRIENDS));
		follower = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.FOLLOWER));
		statusCount = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.STATUSES));
		favorCount = cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.FAVORITS));
		int register = cursor.getInt(cursor.getColumnIndexOrThrow(UserRegisterTable.REGISTER));
		isVerified = (register & VERIFIED_MASK) != 0;
		isLocked = (register & LOCKED_MASK) != 0;
		followReqSent = (register & FOLLOW_REQUEST_MASK) != 0;
		defaultImage = (register & DEFAULT_IMAGE_MASK) != 0;
		isCurrentUser = account.getId() == id;

		if (account.getApiType() != Account.API_TWITTER || defaultImage || profileImageOrig.isEmpty()) {
			profileImageSmall = profileImageOrig;
		} else{
			profileImageSmall = profileImageOrig + "_bigger";
		}
		if (account.getApiType() != Account.API_TWITTER || profileBannerOrig.isEmpty()) {
			profileBannerSmall = profileBannerOrig;
		} else if (profileBannerOrig.endsWith("/1500x500")) {
			profileBannerSmall = profileBannerOrig.substring(0, profileBannerOrig.length() - 9) + "/600x200";
		} else {
			profileBannerSmall = profileBannerOrig + "/600x200";
		}
	}


	@Override
	public long getId() {
		return id;
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
		return createdAt;
	}


	@Override
	public String getOriginalProfileImageUrl() {
		return profileImageOrig;
	}


	@Override
	public String getProfileImageThumbnailUrl() {
		return profileImageSmall;
	}


	@Override
	public String getOriginalBannerImageUrl() {
		return profileBannerOrig;
	}


	@Override
	public String getBannerImageThumbnailUrl() {
		return profileBannerSmall;
	}


	@Override
	public boolean hasDefaultProfileImage() {
		return defaultImage;
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
	public int getStatusCount() {
		return statusCount;
	}


	@Override
	public int getFavoriteCount() {
		return favorCount;
	}


	@Override
	public boolean isCurrentUser() {
		return isCurrentUser;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof User))
			return false;
		return ((User) obj).getId() == id;
	}


	@Override
	public int compareTo(User o) {
		return Long.compare(o.getCreatedAt(), createdAt);
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + screenName + "\"";
	}
}