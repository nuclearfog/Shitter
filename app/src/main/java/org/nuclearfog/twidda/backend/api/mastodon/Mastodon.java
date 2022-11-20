package org.nuclearfog.twidda.backend.api.mastodon;

import android.content.Context;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.lists.Messages;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.backend.update.MediaUpdate;
import org.nuclearfog.twidda.backend.update.ProfileUpdate;
import org.nuclearfog.twidda.backend.update.StatusUpdate;
import org.nuclearfog.twidda.backend.update.UserListUpdate;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Metrics;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

/**
 * Implementation of the Mastodon API
 *
 * @author nuclearfog
 */
public class Mastodon implements Connection {

	MediaType mediaType = MediaType.parse("text/plain");


	private GlobalSettings settings;
	private OkHttpClient client;


	public Mastodon(Context context) {
		settings = GlobalSettings.getInstance(context);
		client = ConnectionBuilder.create(context, 0);
	}


	public String getAuthorisationLink(String... paramsStr) throws MastodonException {
		return "";
	}


	public Account loginApp(String... paramsStr) throws MastodonException {
		return null;
	}

	@Override
	public User showUser(long id) throws MastodonException {
		return null;
	}

	@Override
	public User showUser(String name) throws MastodonException {
		return null;
	}

	@Override
	public Users searchUsers(String search, long page) throws MastodonException {
		return null;
	}

	@Override
	public Users getRepostingUsers(long id) throws MastodonException {
		return null;
	}

	@Override
	public Users getFavoritingUsers(long id) throws MastodonException {
		return null;
	}

	@Override
	public Users getFollowing(long id, long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Users getFollower(long id, long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Users getListMember(long id, long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Users getListSubscriber(long id, long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Users getBlockedUsers(long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Users getMutedUsers(long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Users getIncomingFollowRequests(long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Users getOutgoingFollowRequests(long cursor) throws MastodonException {
		return null;
	}

	@Override
	public Relation getUserRelationship(long id) throws MastodonException {
		return null;
	}

	@Override
	public User followUser(long id) throws MastodonException {
		return null;
	}

	@Override
	public User unfollowUser(long id) throws MastodonException {
		return null;
	}

	@Override
	public User blockUser(long id) throws MastodonException {
		return null;
	}

	@Override
	public User blockUser(String name) throws MastodonException {
		return null;
	}

	@Override
	public User unblockUser(long id) throws MastodonException {
		return null;
	}

	@Override
	public User muteUser(long id) throws MastodonException {
		return null;
	}

	@Override
	public User muteUser(String name) throws MastodonException {
		return null;
	}

	@Override
	public User unmuteUser(long id) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> searchStatuses(String search, long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Trend> getTrends(int id) throws MastodonException {
		return null;
	}

	@Override
	public List<Location> getLocations() throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getHomeTimeline(long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getMentionTimeline(long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getUserTimeline(long id, long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getUserTimeline(String name, long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getUserFavorits(long id, long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getUserFavorits(String name, long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getUserlistStatuses(long id, long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public List<Status> getStatusReplies(String name, long id, long minId, long maxId) throws MastodonException {
		return null;
	}

	@Override
	public Status showStatus(long id) throws MastodonException {
		return null;
	}

	@Override
	public Status favoriteStatus(long id) throws MastodonException {
		return null;
	}

	@Override
	public Status unfavoriteStatus(long id) throws MastodonException {
		return null;
	}

	@Override
	public Status repostStatus(long id) throws MastodonException {
		return null;
	}

	@Override
	public Status removeRepost(long id) throws MastodonException {
		return null;
	}

	@Override
	public void hideReply(long id, boolean hide) throws MastodonException {

	}

	@Override
	public void deleteStatus(long id) throws MastodonException {

	}

	@Override
	public void uploadStatus(StatusUpdate update, long[] mediaIds) throws MastodonException {

	}

	@Override
	public UserList createUserlist(UserListUpdate update) throws MastodonException {
		return null;
	}

	@Override
	public UserList updateUserlist(UserListUpdate update) throws MastodonException {
		return null;
	}

	@Override
	public UserList getUserlist(long id) throws MastodonException {
		return null;
	}

	@Override
	public UserList followUserlist(long id) throws MastodonException {
		return null;
	}

	@Override
	public UserList unfollowUserlist(long id) throws MastodonException {
		return null;
	}

	@Override
	public UserList deleteUserlist(long id) throws MastodonException {
		return null;
	}

	@Override
	public UserLists getUserlistOwnerships(long id, String name, long cursor) throws MastodonException {
		return null;
	}

	@Override
	public UserLists getUserlistMemberships(long id, String name, long cursor) throws MastodonException {
		return null;
	}

	@Override
	public void addUserToList(long id, String name) throws MastodonException {

	}

	@Override
	public void removeUserFromList(long id, String name) throws MastodonException {

	}

	@Override
	public void sendDirectmessage(long id, String message, long mediaId) throws MastodonException {

	}

	@Override
	public void deleteDirectmessage(long id) throws MastodonException {

	}

	@Override
	public Messages getDirectmessages(String cursor) throws MastodonException {
		return null;
	}

	@Override
	public Metrics getStatusMetrics(long id) throws MastodonException {
		return null;
	}

	@Override
	public List<Long> getIdBlocklist() throws MastodonException {
		return null;
	}

	@Override
	public MediaUpdate downloadImage(String link) throws MastodonException {
		return null;
	}

	@Override
	public User updateProfile(ProfileUpdate update) throws MastodonException {
		return null;
	}

	@Override
	public void updateProfileImage(InputStream inputStream) throws MastodonException {

	}

	@Override
	public void updateBannerImage(InputStream inputStream) throws MastodonException {

	}

	@Override
	public long uploadMedia(MediaUpdate mediaUpdate) throws MastodonException {
		return 0;
	}
}