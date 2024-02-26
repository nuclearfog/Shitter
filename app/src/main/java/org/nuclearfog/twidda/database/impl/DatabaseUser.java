package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.DatabaseAdapter.UserPropertiesTable;
import org.nuclearfog.twidda.database.DatabaseAdapter.UserTable;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.User;

import java.util.regex.Pattern;

/**
 * database implementation of an user
 *
 * @author nuclearfog
 */
public class DatabaseUser implements User, UserTable, UserPropertiesTable {

	private static final long serialVersionUID = 2367055336838212570L;

	private static final Pattern KEY_SEPARATOR = Pattern.compile(";");

	private long id, createdAt;
	private int following, follower, statusCount, favorCount;
	private boolean isCurrentUser, isVerified, isLocked, defaultImage, indexable, discoverable;
	private String username = "";
	private String screen_name = "";
	private String description = "";
	private String location = "";
	private String profileUrl = "";
	private String profileImageSmall = "";
	private String profileImageOrig = "";
	private String profileBannerSmall = "";
	private String profileBannerOrig = "";
	private String[] emojiKeys = {};
	private Emoji[] emojis = {};

	/**
	 * @param cursor  database cursor containing user column
	 * @param account current user login
	 */
	public DatabaseUser(Cursor cursor, Account account) {
		id = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
		String username = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
		String screen_name = cursor.getString(cursor.getColumnIndexOrThrow(SCREENNAME));
		String profileImageOrig = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE));
		String description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION));
		String profileUrl = cursor.getString(cursor.getColumnIndexOrThrow(LINK));
		String location = cursor.getString(cursor.getColumnIndexOrThrow(LOCATION));
		String profileBannerOrig = cursor.getString(cursor.getColumnIndexOrThrow(BANNER));
		String emojiKeys = cursor.getString(cursor.getColumnIndexOrThrow(EMOJI));
		createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(SINCE));
		following = cursor.getInt(cursor.getColumnIndexOrThrow(FRIENDS));
		follower = cursor.getInt(cursor.getColumnIndexOrThrow(FOLLOWER));
		statusCount = cursor.getInt(cursor.getColumnIndexOrThrow(STATUSES));
		favorCount = cursor.getInt(cursor.getColumnIndexOrThrow(FAVORITS));
		int register = cursor.getInt(cursor.getColumnIndexOrThrow(REGISTER));
		isVerified = (register & MASK_USER_VERIFIED) != 0;
		isLocked = (register & MASK_USER_PRIVATE) != 0;
		defaultImage = (register & MASK_USER_DEFAULT_IMAGE) != 0;
		discoverable = (register & MASK_USER_DISCOVERABLE) != 0;
		indexable = (register & MASK_USER_DISCOVERABLE) != 0;

		if (emojiKeys != null && !emojiKeys.isEmpty())
			this.emojiKeys = KEY_SEPARATOR.split(emojiKeys);
		if (username != null)
			this.username = username;
		if (screen_name != null)
			this.screen_name = screen_name;
		if (profileImageOrig != null)
			this.profileImageOrig = profileImageOrig;
		if (description != null)
			this.description = description;
		if (profileUrl != null)
			this.profileUrl = profileUrl;
		if (location != null)
			this.location = location;
		if (profileBannerOrig != null)
			this.profileBannerOrig = profileBannerOrig;
		setAccountInformation(account);
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
		return screen_name;
	}


	@Override
	public long getTimestamp() {
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
		return description;
	}


	@Override
	public String getLocation() {
		return location;
	}


	@Override
	public String getProfileUrl() {
		return profileUrl;
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
	public boolean isIndexable() {
		return indexable;
	}


	@Override
	public boolean isDiscoverable() {
		return discoverable;
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
	public Emoji[] getEmojis() {
		return emojis;
	}


	@Override
	public Field[] getFields() {
		return new Field[0];// todo implement this
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof User))
			return false;
		return ((User) obj).getId() == getId();
	}


	@NonNull
	@Override
	public String toString() {
		return "name=\"" + getScreenname() + "\"";
	}

	/**
	 * @return used emoji keys
	 */
	public String[] getEmojiKeys() {
		return emojiKeys;
	}

	/**
	 * add status emojis
	 *
	 * @param emojis emoji array
	 */
	public void addEmojis(@NonNull Emoji[] emojis) {
		this.emojis = emojis;
	}

	/**
	 * setup account configuration
	 */
	public void setAccountInformation(Account account) {
		isCurrentUser = account.getId() == id;
		switch (account.getConfiguration()) {
			case MASTODON:
				profileImageSmall = profileImageOrig;
				profileBannerSmall = profileBannerOrig;
				break;

			default:
				profileImageSmall = "";
				profileBannerSmall = "";
				break;
		}
	}
}