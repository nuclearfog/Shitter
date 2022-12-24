package org.nuclearfog.twidda.backend.api.mastodon;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonAccount;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonList;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonNotification;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonRelation;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonStatus;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonTrend;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonUser;
import org.nuclearfog.twidda.backend.lists.Messages;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.backend.update.MediaStatus;
import org.nuclearfog.twidda.backend.update.ProfileUpdate;
import org.nuclearfog.twidda.backend.update.StatusUpdate;
import org.nuclearfog.twidda.backend.update.UserListUpdate;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private static final String AUTH_SCOPES = "read%20write%20follow";
	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	private static final String AUTH_WEBSITE = "https://github.com/nuclearfog/Shitter";
	private static final String AUTH_NAME = "SH1TT3R";

	// Mastodon endpoints see https://docs.joinmastodon.org/methods/
	private static final String REGISTER_APP = "/api/v1/apps";
	private static final String AUTHORIZE_APP = "/oauth/authorize";
	private static final String LOGIN_APP = "/oauth/token";
	private static final String VERIFY_CREDENTIALS = "/api/v1/accounts/verify_credentials";
	private static final String HOME_TIMELINE = "/api/v1/timelines/home";
	private static final String LIST_TIMELINE = "/api/v1/timelines/list/";
	private static final String SEARCH_TIMELINE = "/api/v2/search";
	private static final String ENDPOINT_USER_TIMELINE = "/api/v1/accounts/";
	private static final String ENDPOINT_USER_FAVORITS = "/api/v1/favourites";
	private static final String ENDPOINT_TRENDS = "/api/v1/trends/tags";
	private static final String ENDPOINT_GET_USER = "/api/v1/accounts/";
	private static final String ENDPOINT_RELATIONSHIP = "/api/v1/accounts/relationships";
	private static final String ENDPOINT_STATUS = "/api/v1/statuses/";
	private static final String ENDPOINT_ACCOUNTS = "/api/v1/accounts/";
	private static final String ENDPOINT_SEARCH_ACCOUNTS = "/api/v1/accounts/search";
	private static final String ENDPOINT_LIST = "/api/v1/lists/";
	private static final String ENDPOINT_BLOCKS = "/api/v1/blocks";
	private static final String ENDPOINT_MUTES = "/api/v1/mutes";
	private static final String ENDPOINT_INCOMIN_REQUESTS = "/api/v1/follow_requests";
	private static final String ENDPOINT_LOOKUP_USER = "/api/v1/accounts/lookup";
	private static final String ENDPOINT_USERLIST = "/api/v1/lists";
	private static final String ENDPOINT_NOTIFICATION = "/api/v1/notifications";
	private static final String ENDPOINT_UPLOAD_MEDIA = "/api/v2/media";
	private static final String ENDPOINT_MEDIA_STATUS = "/api/v1/media/";
	private static final String ENDPOINT_UPDATE_CREDENTIALS = "/api/v1/accounts/update_credentials";

	private static final MediaType TYPE_TEXT = MediaType.parse("text/plain");
	private static final MediaType TYPE_STREAM = MediaType.parse("application/octet-stream");


	private GlobalSettings settings;
	private OkHttpClient client;

	/**
	 *
	 */
	public Mastodon(Context context) {
		settings = GlobalSettings.getInstance(context);
		client = ConnectionBuilder.create(context, 0);
	}


	@Override
	public String getAuthorisationLink(String... paramsStr) throws MastodonException {
		String hostname;
		List<String> params = new ArrayList<>();
		params.add("scopes=" + AUTH_SCOPES);
		params.add("redirect_uris=" + REDIRECT_URI);
		params.add("client_name=" + AUTH_NAME);
		params.add("website=" + AUTH_WEBSITE);
		if (paramsStr.length == 0)
			hostname = DEFAULT_HOST;
		else
			hostname = paramsStr[0];
		try {
			Response response = post(hostname, REGISTER_APP, null, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				String client_id = json.getString("client_id");
				String client_secret = json.getString("client_secret");
				return hostname + AUTHORIZE_APP + "?scope=read%20write%20follow&response_type=code&redirect_uri=" + REDIRECT_URI
						+ "&client_id=" + client_id + "&client_secret=" + client_secret;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Account loginApp(String... paramsStr) throws MastodonException {
		Uri link = Uri.parse(paramsStr[0]);
		List<String> params = new ArrayList<>();
		String host = link.getScheme() + "://" + link.getHost();
		String client_id = link.getQueryParameter("client_id");
		String clientSecret = link.getQueryParameter("client_secret");
		params.add("client_id=" + client_id);
		params.add("client_secret=" + clientSecret);
		params.add("grant_type=authorization_code");
		params.add("code=" + paramsStr[1]);
		params.add("redirect_uri=" + REDIRECT_URI);
		params.add("scope=" + AUTH_SCOPES);
		try {
			Response response = post(host, LOGIN_APP, null, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				String bearer = json.getString("access_token");
				User user = getCredentials(host, bearer);
				Account account = new MastodonAccount(user, host, bearer, client_id, clientSecret);
				settings.setLogin(account, false);
				return account;
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
		params.add("q=" + StringTools.encode(search));
		params.add("limit=" + settings.getListSize());
		return getUsers(ENDPOINT_SEARCH_ACCOUNTS, params);
	}


	@Override
	public Users getRepostingUsers(long id) throws MastodonException {
		return getUsers(ENDPOINT_STATUS + id + "/reblogged_by", new ArrayList<>());
	}


	@Override
	public Users getFavoritingUsers(long id) throws MastodonException {
		return getUsers(ENDPOINT_STATUS + id + "/favourited_by", new ArrayList<>());
	}


	@Override
	public Users getFollowing(long id, long cursor) throws MastodonException {
		List<String> params = new ArrayList<>();
		if (cursor > 0)
			params.add("since_id=" + cursor);
		return getUsers(ENDPOINT_ACCOUNTS + id + "/following", params);
	}


	@Override
	public Users getFollower(long id, long cursor) throws MastodonException {
		List<String> params = new ArrayList<>();
		if (cursor > 0)
			params.add("since_id=" + cursor);
		return getUsers(ENDPOINT_ACCOUNTS + id + "/followers", params);
	}


	@Override
	public Users getListMember(long id, long cursor) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("limit=" + settings.getListSize());
		return getUsers(ENDPOINT_LIST + id + "/accounts", params);
	}


	@Override
	public Users getListSubscriber(long id, long cursor) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public Users getBlockedUsers(long cursor) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("limit=" + settings.getListSize());
		return getUsers(ENDPOINT_BLOCKS, params);
	}


	@Override
	public Users getMutedUsers(long cursor) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("limit=" + settings.getListSize());
		return getUsers(ENDPOINT_MUTES, params);
	}


	@Override
	public Users getIncomingFollowRequests(long cursor) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("limit=" + settings.getListSize());
		return getUsers(ENDPOINT_INCOMIN_REQUESTS, params);
	}


	@Override
	public Users getOutgoingFollowRequests(long cursor) {
		return new Users(0L, 0L); // not yet implemented in the mastodon API
	}


	@Override
	public Relation getUserRelationship(long id) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("id[]=" + id);
		try {
			Response response = get(ENDPOINT_RELATIONSHIP, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONArray(body.string());
				return new MastodonRelation(array.getJSONObject(0), settings.getLogin().getId());
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void followUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/follow", new ArrayList<>());
	}


	@Override
	public void unfollowUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/unfollow", new ArrayList<>());
	}


	@Override
	public void blockUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/block", new ArrayList<>());
	}


	@Override
	public void blockUser(String name) throws MastodonException {
		User user = showUser(name);
		blockUser(user.getId());
	}


	@Override
	public void unblockUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/unblock", new ArrayList<>());
	}


	@Override
	public void muteUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/mute", new ArrayList<>());
	}


	@Override
	public void muteUser(String name) throws MastodonException {
		User user = showUser(name);
		muteUser(user.getId());
	}


	@Override
	public void unmuteUser(long id) throws MastodonException {
		createPost(ENDPOINT_ACCOUNTS + id + "/unmute", new ArrayList<>());
	}


	@Override
	public List<Status> searchStatuses(String search, long minId, long maxId) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("q=" + StringTools.encode(search));
		params.add("type=statuses");
		List<Status> result = getStatuses(SEARCH_TIMELINE, params, minId, maxId);
		if (result.size() > 1)
			Collections.sort(result);
		return result;
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
					result.add(new MastodonTrend(array.getJSONObject(i), i));
				}
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
	public List<Status> getHomeTimeline(long minId, long maxId) throws MastodonException {
		return getStatuses(HOME_TIMELINE, new ArrayList<>(0), minId, maxId);
	}


	@Override
	public List<Status> getUserTimeline(long id, long minId, long maxId) throws MastodonException {
		String endpoint = ENDPOINT_USER_TIMELINE + id + "/statuses";
		return getStatuses(endpoint, new ArrayList<>(), minId, maxId);
	}


	@Override
	public List<Status> getUserTimeline(String name, long minId, long maxId) throws MastodonException {
		User user = showUser(name);
		return getUserTimeline(user.getId(), minId, maxId);
	}


	@Override
	public List<Status> getUserFavorits(long id, long minId, long maxId) throws MastodonException {
		if (id == settings.getLogin().getId()) // mastodon only returns favorits of the authenticating user
			return getStatuses(ENDPOINT_USER_FAVORITS, new ArrayList<>(), minId, maxId);
		return new ArrayList<>(0);
	}


	@Override
	public List<Status> getUserFavorits(String name, long minId, long maxId) throws MastodonException {
		User user = showUser(name);
		return getUserFavorits(user.getId(), minId, maxId);
	}


	@Override
	public List<Status> getUserlistStatuses(long id, long minId, long maxId) throws MastodonException {
		return getStatuses(LIST_TIMELINE + id, new ArrayList<>(0), minId, maxId);
	}


	@Override
	public List<Status> getStatusReplies(long id, long minId, long maxId) {
		return new ArrayList<>(0); // todo add implementation
	}


	@Override
	public Status showStatus(long id) throws MastodonException {
		return getStatus(ENDPOINT_STATUS + id, new ArrayList<>());
	}


	@Override
	public Status favoriteStatus(long id) throws MastodonException {
		return postStatus(ENDPOINT_STATUS + id + "/favourite", new ArrayList<>());
	}


	@Override
	public Status unfavoriteStatus(long id) throws MastodonException {
		return postStatus(ENDPOINT_STATUS + id + "/unfavourite", new ArrayList<>());
	}


	@Override
	public Status repostStatus(long id) throws MastodonException {
		return postStatus(ENDPOINT_STATUS + id + "/reblog", new ArrayList<>());
	}


	@Override
	public Status removeRepost(long id) throws MastodonException {
		return postStatus(ENDPOINT_STATUS + id + "/unreblog", new ArrayList<>());
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
	public void uploadStatus(StatusUpdate update, long[] mediaIds) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("status=" + StringTools.encode(update.getText()));
		// add identifier to prevent duplicate posts
		params.add("Idempotency-Key=" + System.currentTimeMillis() / 5000);
		params.add("visibility=public");
		if (update.getReplyId() > 0)
			params.add("in_reply_to_id=" + update.getReplyId());
		for (long mediaId : mediaIds)
			params.add("media_ids[]=" + mediaId);
		try {
			Response response = post(ENDPOINT_STATUS, params);
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList createUserlist(UserListUpdate update) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("title=" + update.getTitle());
		try {
			return createUserlist(post(ENDPOINT_USERLIST, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList updateUserlist(UserListUpdate update) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("id=" + update.getId());
		params.add("title=" + update.getTitle());
		try {
			return createUserlist(put(ENDPOINT_USERLIST, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList getUserlist(long id) throws MastodonException {
		try {
			return createUserlist(get(ENDPOINT_USERLIST + '/' + id, new ArrayList<>()));
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
	public UserList deleteUserlist(long id) throws MastodonException {
		try {
			return createUserlist(delete(ENDPOINT_USERLIST + '/' + id, new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserLists getUserlistOwnerships(long id, String name, long cursor) {
		return new UserLists(0L, 0L); // not implemented yet in the official API
	}


	@Override
	public UserLists getUserlistMemberships(long id, String name, long cursor) throws MastodonException {
		List<String> params = new ArrayList<>();
		if (cursor > 0)
			params.add("since_id=" + cursor);
		return getUserLists(ENDPOINT_ACCOUNTS + '/' + id + "/lists", params);
	}


	@Override
	public void addUserToList(long id, String name) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("account_ids[]=\"" + name + "\"");
		try {
			Response response = post(ENDPOINT_USERLIST + '/' + id + "/accounts", params);
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
		params.add("account_ids[]=\"" + name + "\"");
		try {
			Response response = delete(ENDPOINT_USERLIST + '/' + id + "/accounts", params);
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void sendDirectmessage(long id, String message, long mediaId) throws MastodonException {
		throw new MastodonException("not implemented!"); // todo add implementation
	}


	@Override
	public void deleteDirectmessage(long id) throws MastodonException {
		throw new MastodonException("not implemented!"); // todo add implementation
	}


	@Override
	public Messages getDirectmessages(String cursor) throws MastodonException {
		throw new MastodonException("not implemented!"); // todo add implementation
	}


	@Override
	public List<Long> getIdBlocklist() throws MastodonException {
		throw new MastodonException("not implemented!"); // todo add implementation
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

		params.add("display_name=" + update.getName());
		params.add("note=" + update.getDescription());
		if (update.getProfileImageStream() != null) {
			streams.add(update.getProfileImageStream());
			keys.add("avatar");
		}
		if (update.getBannerImageStream() != null) {
			streams.add(update.getBannerImageStream());
			keys.add("header"); // fixme: banner upload not working
		}
		try {
			Response response = patch(ENDPOINT_UPDATE_CREDENTIALS, params, streams, keys);
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
		if (minId > 0)
			params.add("since_id=" + minId);
		if (maxId > minId)
			params.add("max_id=" + maxId);
		params.add("limit=" + settings.getListSize());
		try {
			return createNotifications(get(ENDPOINT_NOTIFICATION, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * get information about the current user
	 *
	 * @return current user information
	 */
	private User getCredentials() throws MastodonException {
		Account login = settings.getLogin();
		return getCredentials(login.getHostname(), login.getBearerToken());
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
			return createUser(get(host, VERIFY_CREDENTIALS, bearer, new ArrayList<>()));
		} catch (IOException e) {
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
	private Status postStatus(String endpoint, List<String> params) throws MastodonException {
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
	private List<Status> getStatuses(String endpoint, List<String> params, long minId, long maxId) throws MastodonException {
		if (minId > 0)
			params.add("min_id=" + minId);
		if (maxId > minId)
			params.add("max_id=" + maxId);
		params.add("limit=" + settings.getListSize());
		try {
			return createStatuses(get(endpoint, params));
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
	private Users getUsers(String endpoint, List<String> params) throws MastodonException {
		try {
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
	private UserLists getUserLists(String endpoint, List<String> params) throws MastodonException {
		params.add("limit=" + settings.getListSize());
		try {
			Response response = get(endpoint, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONArray(body.string());
				UserLists result = new UserLists(0L, 0L);// todo add pagination
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
				JSONArray array = new JSONArray(body.string());
				Users result = new Users(0L, 0L);
				for (int i = 0; i < array.length(); i++) {
					User item = new MastodonUser(array.getJSONObject(i));
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
	private Status createStatus(Response response) throws MastodonException {
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
				MastodonList result = new MastodonList(json);
				result.setOwner(getCredentials());
				return result;
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
	private List<Status> createStatuses(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray statuses;
				String jsonStr = body.string();
				if (jsonStr.startsWith("{"))
					statuses = new JSONObject(jsonStr).getJSONArray("statuses");
				else
					statuses = new JSONArray(jsonStr);
				List<Status> result = new ArrayList<>(statuses.length());
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
	 * @return notification
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
	private Response post(String endpoint, List<String> params, InputStream is, String addToKey) throws IOException {
		Account login = settings.getLogin();
		RequestBody body = createUploadRequest(is, addToKey);
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
	 * @param is       input stream to upload a file
	 * @param addToKey upload stream key
	 * @return request body
	 */
	private RequestBody createUploadRequest(final InputStream is, String addToKey) {
		RequestBody data = new RequestBody() {
			@Override
			public MediaType contentType() {
				return TYPE_STREAM;
			}

			@Override
			public void writeTo(@NonNull BufferedSink sink) throws IOException {
				sink.writeAll(Okio.buffer(Okio.source(is)));
			}
		};
		return new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(addToKey, StringTools.getRandomString(), data).build();
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