package org.nuclearfog.twidda.backend.api.mastodon;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonAccount;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonEmoji;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonInstance;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonList;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonNotification;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonPoll;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonRelation;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonStatus;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonTranslation;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonTrend;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonUser;
import org.nuclearfog.twidda.backend.helper.ConnectionConfig;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.lists.Messages;
import org.nuclearfog.twidda.backend.helper.update.PollUpdate;
import org.nuclearfog.twidda.backend.helper.update.ProfileUpdate;
import org.nuclearfog.twidda.backend.helper.update.StatusUpdate;
import org.nuclearfog.twidda.backend.helper.lists.Statuses;
import org.nuclearfog.twidda.backend.helper.update.UserListUpdate;
import org.nuclearfog.twidda.backend.helper.lists.UserLists;
import org.nuclearfog.twidda.backend.helper.lists.Users;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Translation;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

/**
 * Implementation of the Mastodon API
 *
 * @author nuclearfog
 */
public class Mastodon implements Connection {

	/**
	 * default Mastodon hostname
	 */
	private static final String DEFAULT_HOST = "https://mastodon.social";

	/**
	 * scopes used by this app
	 */
	private static final String AUTH_SCOPES = "read%20write%20follow";

	/**
	 * oauth no redirect (oob)
	 */
	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	// Mastodon endpoints see https://docs.joinmastodon.org/methods/
	private static final String ENDPOINT_REGISTER_APP = "/api/v1/apps";
	private static final String ENDPOINT_AUTHORIZE_APP = "/oauth/authorize";
	private static final String ENDPOINT_INSTANCE = "/api/v2/instance";
	private static final String ENDPOINT_LOGIN_APP = "/oauth/token";
	private static final String ENDPOINT_VERIFY_CREDENTIALS = "/api/v1/accounts/verify_credentials";
	private static final String ENDPOINT_HOME_TIMELINE = "/api/v1/timelines/home";
	private static final String ENDPOINT_LIST_TIMELINE = "/api/v1/timelines/list/";
	private static final String ENDPOINT_SEARCH_TIMELINE = "/api/v2/search";
	private static final String ENDPOINT_HASHTAG_TIMELINE = "/api/v1/timelines/tag/";
	private static final String ENDPOINT_USER_TIMELINE = "/api/v1/accounts/";
	private static final String ENDPOINT_USER_FAVORITS = "/api/v1/favourites";
	private static final String ENDPOINT_TRENDS = "/api/v1/trends/tags";
	private static final String ENDPOINT_GET_USER = "/api/v1/accounts/";
	private static final String ENDPOINT_RELATIONSHIP = "/api/v1/accounts/relationships";
	private static final String ENDPOINT_STATUS = "/api/v1/statuses/";
	private static final String ENDPOINT_ACCOUNTS = "/api/v1/accounts/";
	private static final String ENDPOINT_SEARCH_ACCOUNTS = "/api/v1/accounts/search";
	private static final String ENDPOINT_BLOCKS = "/api/v1/blocks";
	private static final String ENDPOINT_MUTES = "/api/v1/mutes";
	private static final String ENDPOINT_INCOMIN_REQUESTS = "/api/v1/follow_requests";
	private static final String ENDPOINT_LOOKUP_USER = "/api/v1/accounts/lookup";
	private static final String ENDPOINT_USERLIST = "/api/v1/lists/";
	private static final String ENDPOINT_NOTIFICATION = "/api/v1/notifications";
	private static final String ENDPOINT_BOOKMARKS = "/api/v1/bookmarks";
	private static final String ENDPOINT_UPLOAD_MEDIA = "/api/v2/media";
	private static final String ENDPOINT_MEDIA_STATUS = "/api/v1/media/";
	private static final String ENDPOINT_UPDATE_CREDENTIALS = "/api/v1/accounts/update_credentials";
	private static final String ENDPOINT_PUBLIC_TIMELINE = "/api/v1/timelines/public";
	private static final String ENDPOINT_CUSTOM_EMOJIS = "/api/v1/custom_emojis";
	private static final String ENDPOINT_POLL = "/api/v1/polls/";

	private static final MediaType TYPE_TEXT = MediaType.parse("text/plain");
	private static final MediaType TYPE_STREAM = MediaType.parse("application/octet-stream");

	private static final Pattern PATTERN_MIN_ID = Pattern.compile("min_id=\\d+");
	private static final Pattern PATTERN_MAX_ID = Pattern.compile("max_id=\\d+");

	private GlobalSettings settings;
	private OkHttpClient client;
	private String app_name;
	private String app_website;

	/**
	 *
	 */
	public Mastodon(Context context) {
		settings = GlobalSettings.getInstance(context);
		client = ConnectionBuilder.create(context);
		app_name = context.getString(R.string.app_name_api);
		app_website = context.getString(R.string.app_website);
	}


	@Override
	public String getAuthorisationLink(ConnectionConfig connection) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("scopes=" + AUTH_SCOPES);
		params.add("redirect_uris=" + REDIRECT_URI);
		params.add("client_name=" + app_name);
		params.add("website=" + app_website);
		String hostname = connection.useHost() ? connection.getHostname() : DEFAULT_HOST;
		try {
			Response response = post(hostname, ENDPOINT_REGISTER_APP, null, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				String client_id = json.getString("client_id");
				String client_secret = json.getString("client_secret");
				connection.setOauthTokens(client_id, client_secret);
				return hostname + ENDPOINT_AUTHORIZE_APP + "?scope=read%20write%20follow&response_type=code&redirect_uri=" + REDIRECT_URI + "&client_id=" + client_id;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Account loginApp(ConnectionConfig connection, String pin) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("client_id=" + connection.getOauthConsumerToken());
		params.add("client_secret=" + connection.getOauthTokenSecret());
		params.add("grant_type=authorization_code");
		params.add("code=" + pin);
		params.add("redirect_uri=" + REDIRECT_URI);
		params.add("scope=" + AUTH_SCOPES);
		String hostname = connection.useHost() ? connection.getHostname() : DEFAULT_HOST;
		try {
			Response response = post(hostname, ENDPOINT_LOGIN_APP, null, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				String bearer = json.getString("access_token");
				User user = getCredentials(hostname, bearer);
				Account account = new MastodonAccount(user, hostname, bearer, connection.getOauthConsumerToken(), connection.getOauthTokenSecret());
				settings.setLogin(account, false);
				return account;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Instance getInformation() throws ConnectionException {
		try {
			Response response = get(ENDPOINT_INSTANCE, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonInstance(json);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public User showUser(long id) throws MastodonException {
		try {
			return createUser(get(ENDPOINT_GET_USER + id, new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public User showUser(String name) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("acct=" + name);
		try {
			return createUser(get(ENDPOINT_LOOKUP_USER, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Users searchUsers(String search, long page) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("q=" + StringUtils.encode(search));
		params.add("type=accounts");
		return getUsers(ENDPOINT_SEARCH_ACCOUNTS, page, params);
	}


	@Override
	public Users getRepostingUsers(long id, long cursor) throws MastodonException {
		return getUsers(ENDPOINT_STATUS + id + "/reblogged_by", cursor, new ArrayList<>());
	}


	@Override
	public Users getFavoritingUsers(long id, long cursor) throws MastodonException {
		return getUsers(ENDPOINT_STATUS + id + "/favourited_by", cursor, new ArrayList<>());
	}


	@Override
	public Users getFollowing(long id, long cursor) throws MastodonException {
		return getUsers(ENDPOINT_ACCOUNTS + id + "/following", cursor, new ArrayList<>());
	}


	@Override
	public Users getFollower(long id, long cursor) throws MastodonException {
		return getUsers(ENDPOINT_ACCOUNTS + id + "/followers", cursor, new ArrayList<>());
	}


	@Override
	public Users getListMember(long id, long cursor) throws MastodonException {
		return getUsers(ENDPOINT_USERLIST + id + "/accounts", cursor, new ArrayList<>());
	}


	@Override
	public Users getListSubscriber(long id, long cursor) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public Users getBlockedUsers(long cursor) throws MastodonException {
		return getUsers(ENDPOINT_BLOCKS, cursor, new ArrayList<>());
	}


	@Override
	public Users getMutedUsers(long cursor) throws MastodonException {
		return getUsers(ENDPOINT_MUTES, cursor, new ArrayList<>());
	}


	@Override
	public Users getIncomingFollowRequests(long cursor) throws MastodonException {
		return getUsers(ENDPOINT_INCOMIN_REQUESTS, cursor, new ArrayList<>());
	}


	@Override
	public Users getOutgoingFollowRequests(long cursor) {
		return new Users(0L, 0L); // not yet implemented in the mastodon API
	}


	@Override
	public MastodonRelation getUserRelationship(long id) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("id[]=" + id);
		try {
			Response response = get(ENDPOINT_RELATIONSHIP, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONArray(body.string());
				return new MastodonRelation(array.getJSONObject(0));
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Relation followUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/follow", new ArrayList<>());
		MastodonRelation relation = getUserRelationship(id);
		relation.setFollowing(true);
		return relation;
	}


	@Override
	public Relation unfollowUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/unfollow", new ArrayList<>());
		MastodonRelation relation = getUserRelationship(id);
		relation.setFollowing(false);
		return relation;
	}


	@Override
	public Relation blockUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/block", new ArrayList<>());
		MastodonRelation relation = getUserRelationship(id);
		relation.setBlocked(true);
		return relation;
	}


	@Override
	public Relation blockUser(String name) throws MastodonException {
		User user = showUser(name);
		blockUser(user.getId());
		MastodonRelation relation = getUserRelationship(user.getId());
		relation.setBlocked(true);
		return relation;
	}


	@Override
	public Relation unblockUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/unblock", new ArrayList<>());
		MastodonRelation relation = getUserRelationship(id);
		relation.setBlocked(false);
		return relation;
	}


	@Override
	public Relation muteUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/mute", new ArrayList<>());
		MastodonRelation relation = getUserRelationship(id);
		relation.setMuted(true);
		return relation;
	}


	@Override
	public Relation muteUser(String name) throws MastodonException {
		User user = showUser(name);
		muteUser(user.getId());
		MastodonRelation relation = getUserRelationship(user.getId());
		relation.setMuted(false);
		return relation;
	}


	@Override
	public Relation unmuteUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/unmute", new ArrayList<>());
		MastodonRelation relation = getUserRelationship(id);
		relation.setMuted(false);
		return relation;
	}


	@Override
	public Statuses searchStatuses(String search, long minId, long maxId) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("local=" + settings.useLocalTimeline());
		if (search.matches("#\\S+")) {
			return getStatuses(ENDPOINT_HASHTAG_TIMELINE + search.substring(1), params, minId, maxId);
		} else {
			params.add("q=" + StringUtils.encode(search));
			params.add("type=statuses");
			return getStatuses(ENDPOINT_SEARCH_TIMELINE, params, minId, maxId);
		}
	}


	@Override
	public Statuses getPublicTimeline(long minId, long maxId) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("local=" + settings.useLocalTimeline());
		return getStatuses(ENDPOINT_PUBLIC_TIMELINE, params, minId, maxId);
	}


	@Override
	public List<Trend> getTrends() throws MastodonException {
		try {
			List<String> params = new ArrayList<>();
			params.add("limit=" + settings.getListSize());
			Response response = get(ENDPOINT_TRENDS, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONArray(body.string());
				List<Trend> result = new ArrayList<>(array.length());
				for (int i = 0; i < array.length(); i++) {
					result.add(new MastodonTrend(array.getJSONObject(i)));
				}
				Collections.sort(result);
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public List<Trend> searchHashtags(String search) throws MastodonException {
		try {
			List<String> params = new ArrayList<>();
			params.add("q=" + StringUtils.encode(search));
			params.add("limit=" + settings.getListSize());
			params.add("type=hashtags");
			Response response = get(ENDPOINT_SEARCH_TIMELINE, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONObject(body.string()).getJSONArray("hashtags");
				List<Trend> result = new ArrayList<>(array.length());
				for (int i = 0; i < array.length(); i++) {
					result.add(new MastodonTrend(array.getJSONObject(i)));
				}
				Collections.sort(result);
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public List<Location> getLocations() {
		return new ArrayList<>(0); // not supported yet
	}


	@Override
	public Statuses getHomeTimeline(long minId, long maxId) throws MastodonException {
		return getStatuses(ENDPOINT_HOME_TIMELINE, new ArrayList<>(0), minId, maxId);
	}


	@Override
	public Statuses getUserTimeline(long id, long minId, long maxId) throws MastodonException {
		String endpoint = ENDPOINT_USER_TIMELINE + id + "/statuses";
		return getStatuses(endpoint, new ArrayList<>(), minId, maxId);
	}


	@Override
	public Statuses getUserFavorits(long id, long minId, long maxId) throws MastodonException {
		if (id == settings.getLogin().getId()) // mastodon only returns favorits of the authenticating user
			return getStatuses(ENDPOINT_USER_FAVORITS, new ArrayList<>(), maxId, minId); // min_id and max_id swapped by Mastodon
		return new Statuses();
	}


	@Override
	public Statuses getUserBookmarks(long minId, long maxId) throws MastodonException {
		return getStatuses(ENDPOINT_BOOKMARKS, new ArrayList<>(), maxId, minId); // min_id and max_id swapped by Mastodon
	}


	@Override
	public Statuses getUserlistStatuses(long id, long minId, long maxId) throws MastodonException {
		return getStatuses(ENDPOINT_LIST_TIMELINE + id, new ArrayList<>(0), minId, maxId);
	}


	@Override
	public Statuses getStatusReplies(long id, long minId, long maxId, String... extras) throws MastodonException {
		Statuses statusThreads = getStatuses(ENDPOINT_STATUS + id + "/context", new ArrayList<>(0), minId, maxId);
		Statuses result = new Statuses();
		for (Status status : statusThreads) {
			// Mastodon doesn't support min/max ID.
			if (status.getRepliedStatusId() == id && (minId == 0 || status.getId() > minId) && (maxId == 0 || status.getId() < maxId)) {
				result.add(status);
			}
		}
		return result;
	}


	@Override
	public Status showStatus(long id) throws MastodonException {
		return getStatus(ENDPOINT_STATUS + id, new ArrayList<>());
	}


	@Override
	public Status favoriteStatus(long id) throws MastodonException {
		MastodonStatus status = postStatus(ENDPOINT_STATUS + id + "/favourite", new ArrayList<>());
		status.setFavorite(true);
		return status;
	}


	@Override
	public Status unfavoriteStatus(long id) throws MastodonException {
		MastodonStatus status = postStatus(ENDPOINT_STATUS + id + "/unfavourite", new ArrayList<>());
		status.setFavorite(false);
		return status;
	}


	@Override
	public Status repostStatus(long id) throws MastodonException {
		MastodonStatus status = postStatus(ENDPOINT_STATUS + id + "/reblog", new ArrayList<>());
		status.setRepost(true);
		return status;
	}


	@Override
	public Status removeRepost(long id) throws MastodonException {
		MastodonStatus status = postStatus(ENDPOINT_STATUS + id + "/unreblog", new ArrayList<>());
		status.setRepost(false);
		return status;
	}


	@Override
	public Status bookmarkStatus(long id) throws ConnectionException {
		MastodonStatus status = postStatus(ENDPOINT_STATUS + id + "/bookmark", new ArrayList<>());
		status.setBookmark(true);
		return status;
	}


	@Override
	public Status removeBookmark(long id) throws ConnectionException {
		MastodonStatus status = postStatus(ENDPOINT_STATUS + id + "/unbookmark", new ArrayList<>());
		status.setBookmark(false);
		return status;
	}


	@Override
	public void muteConversation(long id) throws MastodonException {
		createPost(ENDPOINT_MUTES + id + "/mute", new ArrayList<>());
	}


	@Override
	public void unmuteConversation(long id) throws MastodonException {
		createPost(ENDPOINT_STATUS + id + "/unmute", new ArrayList<>());
	}


	@Override
	public void deleteStatus(long id) throws MastodonException {
		try {
			Response response = delete(ENDPOINT_STATUS + id, new ArrayList<>());
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Status uploadStatus(StatusUpdate update, List<Long> mediaIds) throws MastodonException {
		List<String> params = new ArrayList<>();
		// add identifier to prevent duplicate posts
		params.add("Idempotency-Key=" + System.currentTimeMillis() / 5000);
		params.add("visibility=public");
		if (update.isSensitive())
			params.add("sensitive=true");
		if (update.isSpoiler())
			params.add("spoiler_text=true");
		if (update.getText() != null)
			params.add("status=" + StringUtils.encode(update.getText()));
		if (update.getReplyId() != 0)
			params.add("in_reply_to_id=" + update.getReplyId());
		if (update.getVisibility() == Status.VISIBLE_DIRECT)
			params.add("visibility=direct");
		else if (update.getVisibility() == Status.VISIBLE_PRIVATE)
			params.add("visibility=private");
		else if (update.getVisibility() == Status.VISIBLE_UNLISTED)
			params.add("visibility=unlisted");
		else
			params.add("visibility=public");
		for (long mediaId : mediaIds) {
			params.add("media_ids[]=" + mediaId);
		}
		// add media keys of a previous status
		for (String mediaKey : update.getMediaKeys()) {
			params.add("media_ids[]=" + mediaKey);
		}
		if (update.getPoll() != null) {
			PollUpdate poll = update.getPoll();
			for (String option : poll.getOptions())
				params.add("poll[options][]=" + StringUtils.encode(option));
			params.add("poll[expires_in]=" + poll.getDuration());
			params.add("poll[multiple]=" + poll.multipleChoiceEnabled());
			params.add("poll[hide_totals]=" + poll.hideTotalVotes());
		}
		try {
			Response response;
			if (update.getStatusId() != 0L)
				response = put(ENDPOINT_STATUS + update.getStatusId(), params);
			else
				response = post(ENDPOINT_STATUS, params);
			if (response.code() == 200) {
				return createStatus(response);
			}
			throw new MastodonException(response);
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList createUserlist(UserListUpdate update) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("title=" + StringUtils.encode(update.getTitle()));
		try {
			return createUserlist(post(ENDPOINT_USERLIST, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList updateUserlist(UserListUpdate update) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("title=" + StringUtils.encode(update.getTitle()));
		try {
			return createUserlist(put(ENDPOINT_USERLIST + update.getId(), params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList getUserlist(long id) throws MastodonException {
		try {
			return createUserlist(get(ENDPOINT_USERLIST + id, new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList followUserlist(long id) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public UserList unfollowUserlist(long id) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public void deleteUserlist(long id) throws MastodonException {
		try {
			Response response = delete(ENDPOINT_USERLIST + id, new ArrayList<>());
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserLists getUserlistOwnerships(long id, long cursor) throws MastodonException {
		return getUserLists(ENDPOINT_USERLIST, new ArrayList<>(), cursor);
	}


	@Override
	public UserLists getUserlistMemberships(long id, long cursor) throws MastodonException {
		return getUserLists(ENDPOINT_ACCOUNTS + id + "/lists", new ArrayList<>(), cursor);
	}


	@Override
	public void addUserToList(long id, String name) throws MastodonException {
		List<String> params = new ArrayList<>();
		User user = showUser(name);
		params.add("account_ids[]=" + user.getId());
		try {
			Response response = post(ENDPOINT_USERLIST + id + "/accounts", params);
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void removeUserFromList(long id, String name) throws MastodonException {
		List<String> params = new ArrayList<>();
		User user = showUser(name);
		params.add("account_ids[]=" + user.getId());
		try {
			Response response = delete(ENDPOINT_USERLIST + id + "/accounts", params);
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void sendDirectmessage(long id, String message, long mediaId) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public void deleteDirectmessage(long id) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public Messages getDirectmessages(String cursor) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public List<Emoji> getEmojis() throws MastodonException {
		try {
			Response response = get(ENDPOINT_CUSTOM_EMOJIS, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray json = new JSONArray(body.string());
				List<Emoji> result = new ArrayList<>(json.length());
				for (int i = 0; i < json.length(); i++) {
					MastodonEmoji item = new MastodonEmoji(json.getJSONObject(i));
					if (item.visible()) {
						result.add(item);
					}
				}
				Collections.sort(result);
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Poll getPoll(long id) throws ConnectionException {
		try {
			Response response = get(ENDPOINT_POLL + id, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonPoll(json);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Poll votePoll(Poll poll, int[] selection) throws ConnectionException {
		List<String> params = new ArrayList<>();
		for (int choice : selection) {
			params.add("choices[]=" + choice);
		}
		try {
			Response response = post(ENDPOINT_POLL + poll.getId() + "/votes", params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonPoll(json);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public List<Long> getIdBlocklist() throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public MediaStatus downloadImage(String link) throws MastodonException {
		try {
			Request request = new Request.Builder().url(link).get().build();
			Response response = client.newCall(request).execute();
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				MediaType type = body.contentType();
				if (type != null) {
					String mime = type.toString();
					InputStream stream = body.byteStream();
					return new MediaStatus(stream, mime);
				}
			}
			throw new MastodonException(response);
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public User updateProfile(ProfileUpdate update) throws MastodonException {
		List<String> params = new ArrayList<>();
		List<InputStream> streams = new ArrayList<>();
		List<String> keys = new ArrayList<>();

		params.add("display_name=" + StringUtils.encode(update.getName()));
		params.add("note=" + StringUtils.encode(update.getDescription()));
		if (update.getProfileImageStream() != null) {
			streams.add(update.getProfileImageStream());
			keys.add("avatar");
		}
		if (update.getBannerImageStream() != null) {
			streams.add(update.getBannerImageStream());
			keys.add("header");
		}
		try {
			Response response = patch(ENDPOINT_UPDATE_CREDENTIALS, params, streams, keys);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonUser(json, settings.getLogin().getId());
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public long uploadMedia(MediaStatus mediaUpdate) throws MastodonException {
		try {
			Response response = post(ENDPOINT_UPLOAD_MEDIA, new ArrayList<>(), mediaUpdate.getStream(), "file");
			ResponseBody body = response.body();
			if (body != null) {
				if (response.code() == 200) {
					JSONObject json = new JSONObject(body.string());
					return Long.parseLong(json.getString("id"));
				}
				// wait until processed
				else if (response.code() == 202) {
					int retryCount = 0;
					JSONObject json = new JSONObject(body.string());
					long id = Long.parseLong(json.getString("id"));
					while (retryCount++ < 10) {
						response = get(ENDPOINT_MEDIA_STATUS + id, new ArrayList<>());
						if (response.code() == 200)
							return id;
						Thread.sleep(2000L);
					}
				}
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException | NumberFormatException | InterruptedException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public List<Notification> getNotifications(long minId, long maxId) throws ConnectionException {
		List<String> params = new ArrayList<>();
		if (minId != 0L)
			params.add("since_id=" + minId);
		if (maxId != 0L)
			params.add("max_id=" + maxId);
		params.add("limit=" + settings.getListSize());
		try {
			return createNotifications(get(ENDPOINT_NOTIFICATION, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Notification getNotification(long id) throws ConnectionException {
		try {
			return createNotification(get(ENDPOINT_NOTIFICATION + '/' + id, new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void dismissNotification(long id) throws ConnectionException {
		try {
			Response response = post(ENDPOINT_NOTIFICATION + '/' + id + "/dismiss", new ArrayList<>());
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Translation getStatusTranslation(long id) throws ConnectionException {
		try {
			List<String> param = new ArrayList<>();
			param.add("lang=" + Locale.getDefault().getLanguage()); // set system language as destiny for translation
			Response response = post(ENDPOINT_STATUS + id + "/translate", param);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonTranslation(json);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * get information about the current user
	 *
	 * @param host   Mastodon hostname
	 * @param bearer bearer token to use
	 * @return current user information
	 */
	private User getCredentials(String host, @NonNull String bearer) throws MastodonException {
		try {
			Response response = get(host, ENDPOINT_VERIFY_CREDENTIALS, bearer, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonUser(json);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * get a status from endpoint
	 *
	 * @param endpoint endpoint to use
	 * @param params   additional parameters
	 * @return status
	 */
	private Status getStatus(String endpoint, List<String> params) throws MastodonException {
		try {
			return createStatus(get(endpoint, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * post a status from endpoint
	 *
	 * @param endpoint endpoint to use
	 * @param params   additional parameters
	 * @return status
	 */
	private MastodonStatus postStatus(String endpoint, List<String> params) throws MastodonException {
		try {
			return createStatus(post(endpoint, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * get a status timeline
	 *
	 * @param endpoint Endpoint to use
	 * @param params   additional parameters
	 * @param minId    minimum status ID
	 * @param maxId    maximum status ID
	 * @return status  timeline
	 */
	private Statuses getStatuses(String endpoint, List<String> params, long minId, long maxId) throws MastodonException {
		if (minId != 0L)
			params.add("min_id=" + minId);
		if (maxId != 0L)
			params.add("max_id=" + maxId);
		params.add("limit=" + settings.getListSize());
		try {
			Statuses result = createStatuses(get(endpoint, params));
			if (result.size() > 1)
				Collections.sort(result);
			return result;
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * get a list of users from an endpoint
	 *
	 * @param endpoint Ednpoint to use
	 * @param params   additional parameters
	 * @return list of users
	 */
	private Users getUsers(String endpoint, long cursor, List<String> params) throws MastodonException {
		try {
			if (cursor != -1L)
				params.add("max_id=" + cursor);
			params.add("limit=" + settings.getListSize());
			return createUsers(get(endpoint, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create userlists from GET endpoint
	 *
	 * @param endpoint userlist endpoint
	 * @param params   additional parameters
	 * @return userlists
	 */
	private UserLists getUserLists(String endpoint, List<String> params, long cursor) throws MastodonException {
		params.add("limit=" + settings.getListSize());
		if (cursor != -1L)
			params.add("max_id=" + cursor);
		try {
			Response response = get(endpoint, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				long[] cursors = getCursors(response);
				JSONArray array = new JSONArray(body.string());
				UserLists result = new UserLists(cursors[0], cursors[1]);
				for (int i = 0; i < array.length(); i++) {
					result.add(new MastodonList(array.getJSONObject(i)));
				}
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create user from response
	 *
	 * @param response endpoint response
	 * @return user
	 */
	private User createUser(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonUser(json, settings.getLogin().getId());
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create a list of users from response
	 *
	 * @param response endpoint response
	 * @return list of users
	 */
	private Users createUsers(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				long currentId = settings.getLogin().getId();
				long[] cursors = getCursors(response);
				JSONArray array = new JSONArray(body.string());
				Users result = new Users(cursors[0], cursors[1]);
				for (int i = 0; i < array.length(); i++) {
					User item = new MastodonUser(array.getJSONObject(i), currentId);
					result.add(item);
				}
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create status from endpoint
	 *
	 * @param response endpoint response
	 * @return status
	 */
	private MastodonStatus createStatus(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonStatus(json, settings.getLogin().getId());
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create userlist from endpoint
	 *
	 * @param response endpoint response
	 * @return status
	 */
	private UserList createUserlist(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonList(json);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create a list of statuses from a resposne
	 *
	 * @param response endpoint response
	 * @return list of statuses
	 */
	private Statuses createStatuses(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray statuses;
				String jsonStr = body.string();
				if (jsonStr.startsWith("{")) {
					JSONObject json = new JSONObject(jsonStr);
					if (json.has("descendants"))
						statuses = json.getJSONArray("descendants");
					else
						statuses = json.getJSONArray("statuses");
				} else {
					statuses = new JSONArray(jsonStr);
				}
				long[] cursors = getCursors(response);
				Statuses result = new Statuses(cursors[0], cursors[1]);
				long currentId = settings.getLogin().getId();
				for (int i = 0; i < statuses.length(); i++) {
					result.add(new MastodonStatus(statuses.getJSONObject(i), currentId));
				}
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create notification from response
	 *
	 * @return a list of notification
	 */
	private List<Notification> createNotifications(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				long currentId = settings.getLogin().getId();
				JSONArray json = new JSONArray(body.string());
				List<Notification> result = new ArrayList<>(json.length());
				for (int i = 0; i < json.length(); i++)
					result.add(new MastodonNotification(json.getJSONObject(i), currentId));
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create a single notification from response
	 *
	 * @return notification
	 */
	private Notification createNotification(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				long currentId = settings.getLogin().getId();
				JSONObject json = new JSONObject(body.string());
				return new MastodonNotification(json, currentId);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * send post request without return
	 *
	 * @param endpoint endpoint to use
	 * @param params   additional parameters
	 */
	private void createPost(String endpoint, List<String> params) throws MastodonException {
		try {
			Response response = post(endpoint, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null)
				return;
			throw new MastodonException(response);
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create get response with user bearer token
	 *
	 * @param endpoint endpoint to use
	 * @param params   additional parameters
	 * @return GET response
	 */
	private Response get(String endpoint, List<String> params) throws IOException {
		Account currentLogin = settings.getLogin();
		return get(currentLogin.getHostname(), endpoint, currentLogin.getBearerToken(), params);
	}

	/**
	 * create a GET response
	 *
	 * @param endpoint endpoint url
	 * @param bearer   bearer token or null
	 * @param params   additional parameters
	 * @return GET response
	 */
	private Response get(String hostname, String endpoint, @Nullable String bearer, List<String> params) throws IOException {
		Request.Builder request = new Request.Builder().url(buildUrl(hostname, endpoint, params)).get();
		if (bearer != null) {
			request.addHeader("Authorization", "Bearer " + bearer);
		}
		return client.newCall(request.build()).execute();
	}

	/**
	 * create post response with user bearer token
	 *
	 * @param endpoint endpoint to use
	 * @param params   additional parameters
	 * @return POST response
	 */
	private Response post(String endpoint, List<String> params) throws IOException {
		Account login = settings.getLogin();
		return post(login.getHostname(), endpoint, login.getBearerToken(), params);
	}

	/**
	 * create a POST response
	 *
	 * @param endpoint endpoint url
	 * @param bearer   bearer token or null
	 * @param params   additional parameters
	 * @return POST response
	 */
	private Response post(String hostname, String endpoint, @Nullable String bearer, List<String> params) throws IOException {
		return post(hostname, endpoint, bearer, params, RequestBody.create("", TYPE_TEXT));
	}

	/**
	 * send POST request with file and create response
	 *
	 * @param endpoint endpoint url
	 * @param params   additional http parameters
	 * @return http response
	 */
	private Response post(String endpoint, List<String> params, InputStream inputStream, String addToKey) throws IOException {
		if (inputStream == null)
			throw new IOException("InputStream null!");
		Account login = settings.getLogin();
		RequestBody body = createUploadRequest(inputStream, addToKey);
		return post(login.getHostname(), endpoint, login.getBearerToken(), params, body);
	}

	/**
	 * send POST request
	 *
	 * @param hostname hostname of a Mastodon instance
	 * @param endpoint POST endpoint to use
	 * @param bearer   bearer token to authenticate
	 * @param params   additional parameters
	 * @return POST response
	 */
	private Response post(String hostname, String endpoint, @Nullable String bearer, List<String> params, RequestBody body) throws IOException {
		Request.Builder request = new Request.Builder().url(buildUrl(hostname, endpoint, params)).post(body);
		if (bearer != null) {
			request.addHeader("Authorization", "Bearer " + bearer);
		}
		return client.newCall(request.build()).execute();
	}

	/**
	 * create a PUT response
	 *
	 * @param endpoint endpoint url
	 * @param params   additional parameters
	 * @return PUT response
	 */
	private Response put(String endpoint, List<String> params) throws IOException {
		Account login = settings.getLogin();
		RequestBody body = RequestBody.create("", TYPE_TEXT);
		Request.Builder request = new Request.Builder().url(buildUrl(login.getHostname(), endpoint, params)).put(body);
		request.addHeader("Authorization", "Bearer " + login.getBearerToken());
		return client.newCall(request.build()).execute();
	}

	/**
	 * create a DELETE response
	 *
	 * @param endpoint endpoint url
	 * @param params   additional parameters
	 * @return DELETE response
	 */
	private Response delete(String endpoint, List<String> params) throws IOException {
		Account login = settings.getLogin();
		RequestBody body = RequestBody.create("", TYPE_TEXT);
		Request.Builder request = new Request.Builder().url(buildUrl(login.getHostname(), endpoint, params)).delete(body);
		request.addHeader("Authorization", "Bearer " + login.getBearerToken());
		return client.newCall(request.build()).execute();
	}

	/**
	 * create a DELETE response
	 *
	 * @param endpoint endpoint url
	 * @param params   additional parameters
	 * @return DELETE response
	 */
	private Response patch(String endpoint, List<String> params, List<InputStream> streams, List<String> keys) throws IOException {
		Account login = settings.getLogin();
		Request.Builder builder = new Request.Builder().url(buildUrl(login.getHostname(), endpoint, params));
		if (streams.isEmpty() || keys.isEmpty()) {
			builder.patch(RequestBody.create("", TYPE_TEXT));
		} else {
			for (int i = 0; i < streams.size() && i < keys.size(); i++) {
				builder.patch(createUploadRequest(streams.get(i), keys.get(i)));
			}
		}
		Request request = builder.addHeader("Authorization", "Bearer " + login.getBearerToken()).build();
		return client.newCall(request).execute();
	}

	/**
	 * create requestbody with upload stream
	 *
	 * @param inputStream  input stream to upload a file
	 * @param addToKey     upload stream key
	 * @return request body
	 */
	private RequestBody createUploadRequest(final InputStream inputStream, String addToKey) throws IOException {
		if (inputStream == null)
			throw new IOException("InputStream null!");
		RequestBody data = new RequestBody() {
			@Override
			public MediaType contentType() {
				return TYPE_STREAM;
			}

			@Override
			public void writeTo(@NonNull BufferedSink sink) throws IOException {
				sink.writeAll(Okio.buffer(Okio.source(inputStream)));
			}
		};
		return new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(addToKey, StringUtils.getRandomString(), data).build();
	}

	/**
	 * get cursors from header
	 *
	 * @param response response to get the header
	 * @return array of cursors [min_id, max_id]
	 */
	private long[] getCursors(Response response) {
		String headerStr = response.header("Link", "");
		long[] cursors = {0L, 0L};
		if (headerStr != null && !headerStr.trim().isEmpty()) {
			try {
				Matcher m = PATTERN_MIN_ID.matcher(headerStr);
				if (m.find()) {
					String min_id_str = headerStr.substring(m.start() + 7, m.end());
					cursors[0] = Long.parseLong(min_id_str);
				}
				m = PATTERN_MAX_ID.matcher(headerStr);
				if (m.find()) {
					String max_id_str = headerStr.substring(m.start() + 7, m.end());
					cursors[1] = Long.parseLong(max_id_str);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return cursors;
	}

	/**
	 * append query parameters to an url
	 *
	 * @param hostname hostname of the url
	 * @param params   additional parameters
	 * @return url with hostname and query parameters
	 */
	private String buildUrl(String hostname, String endpoint, List<String> params) {
		if (!params.isEmpty()) {
			StringBuilder result = new StringBuilder(hostname);
			result.append(endpoint);
			result.append('?');
			for (String param : params) {
				result.append(param).append('&');
			}
			result.deleteCharAt(result.length() - 1);
			return result.toString();
		}
		return hostname + endpoint;
	}
}