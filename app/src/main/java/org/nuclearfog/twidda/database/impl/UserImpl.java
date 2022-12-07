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
	private long created;
	private int following;
	private int follower;
	private int statusCount;
	private int favorCount;
	private int apiType;
	private String username;
	private String screenName;
	private String bio;
	private String location;
	private String link;
	private String profileImageUrl;
	private String profileBannerUrl;
	private boolean isCurrentUser;
	private boolean isVerified;
	private boolean isLocked;
	private boolean followReqSent;
	private boolean defaultImage;

	/**
	 * @param cursor database cursor containing user column
	 * @param account current user login
	 */
	public UserImpl(Cursor cursor, Account account) {
		id = cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.ID));
		username = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.USERNAME));
		screenName = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.SCREENNAME));
		profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.IMAGE));
		bio = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.DESCRIPTION));
		link = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.LINK));
		location = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.LOCATION));
		profileBannerUrl = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.BANNER));
		created = cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.SINCE));
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
		this.apiType = account.getApiType();
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
		return created;
	}


	@Override
	public String getOriginalProfileImageUrl() {
		return profileImageUrl;
	}


	@Override
	public String getProfileImageThumbnailUrl() {
		if (apiType != Account.API_TWITTER || defaultImage || profileImageUrl.isEmpty())
			return profileImageUrl;
		return profileImageUrl + "_bigger";
	}


	@Override
	public String getOriginalBannerImageUrl() {
		if (apiType != Account.API_TWITTER || profileBannerUrl.isEmpty())
			return profileBannerUrl;
		return profileBannerUrl + "/1500x500";
	}


	@Override
	public String getBannerImageThumbnailUrl() {
		if (apiType != Account.API_TWITTER || profileBannerUrl.isEmpty())
			return profileBannerUrl;
		return profileBannerUrl + "/600x200";
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


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + screenName + "\"";
	}
}