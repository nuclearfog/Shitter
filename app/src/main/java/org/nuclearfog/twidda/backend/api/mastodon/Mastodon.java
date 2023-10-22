package org.nuclearfog.twidda.backend.api.mastodon;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.mastodon.impl.EditedMastodonStatus;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonAccount;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonCredentials;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonEmoji;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonFilter;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonHashtag;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonInstance;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonList;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonNotification;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonPoll;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonPush;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonRelation;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonRule;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonStatus;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonTranslation;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonUser;
import org.nuclearfog.twidda.backend.api.mastodon.impl.ScheduledMastodonStatus;
import org.nuclearfog.twidda.backend.helper.ConnectionResult;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.update.ConnectionUpdate;
import org.nuclearfog.twidda.backend.helper.update.FilterUpdate;
import org.nuclearfog.twidda.backend.helper.update.PollUpdate;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.backend.helper.update.ReportUpdate;
import org.nuclearfog.twidda.backend.helper.update.StatusUpdate;
import org.nuclearfog.twidda.backend.helper.update.UserListUpdate;
import org.nuclearfog.twidda.backend.helper.update.UserUpdate;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Credentials;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Filter;
import org.nuclearfog.twidda.model.Hashtag;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.ScheduledStatus;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Translation;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.model.WebPush;
import org.nuclearfog.twidda.model.lists.Domains;
import org.nuclearfog.twidda.model.lists.Filters;
import org.nuclearfog.twidda.model.lists.Hashtags;
import org.nuclearfog.twidda.model.lists.Notifications;
import org.nuclearfog.twidda.model.lists.Rules;
import org.nuclearfog.twidda.model.lists.ScheduledStatuses;
import org.nuclearfog.twidda.model.lists.StatusEditHistoy;
import org.nuclearfog.twidda.model.lists.Statuses;
import org.nuclearfog.twidda.model.lists.UserLists;
import org.nuclearfog.twidda.model.lists.Users;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.ArrayList;
import java.util.Arrays;
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
	private static final String AUTH_SCOPES = "read%20write%20push";

	/**
	 * oauth no redirect (oob)
	 */
	private static final String REDIRECT_URI = "urn%3aietf%3awg%3aoauth%3a2.0%3aoob";

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
	private static final String ENDPOINT_HASHTAG_FOLLOWING = "/api/v1/followed_tags";
	private static final String ENDPOINT_HASHTAG_FEATURE = "/api/v1/featured_tags";
	private static final String ENDPOINT_HASHTAG = "/api/v1/tags/";
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
	private static final String ENDPOINT_FOLLOW_REQUESTS = "/api/v1/follow_requests";
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
	private static final String ENDPOINT_DOMAIN_BLOCK = "/api/v1/domain_blocks";
	private static final String ENDPOINT_PUSH_UPDATE = "/api/v1/push/subscription";
	private static final String ENDPOINT_FILTER = "/api/v2/filters";
	private static final String ENDPOINT_REPORT = "/api/v1/reports";
	private static final String ENDPOINT_SCHEDULED_STATUS = "/api/v1/scheduled_statuses";
	private static final String ENDPOINT_GET_RULES = "/api/v1/instance/rules";

	private static final MediaType TYPE_TEXT = MediaType.parse("text/plain");
	private static final MediaType TYPE_STREAM = MediaType.parse("application/octet-stream");

	private static final Pattern PATTERN_SINCE_ID = Pattern.compile("since_id=\\d+");
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
		settings = GlobalSettings.get(context);
		client = ConnectionBuilder.create(context);
		app_name = context.getString(R.string.app_name_api);
		app_website = context.getString(R.string.app_website);
	}


	@Override
	public ConnectionResult getAuthorisationLink(ConnectionUpdate connection) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("scopes=" + AUTH_SCOPES);
		params.add("redirect_uris=" + REDIRECT_URI);
		params.add("website=" + StringUtils.encode(app_website));
		if (!connection.getAppName().isEmpty())
			params.add("client_name=" + StringUtils.encode(connection.getAppName()));
		else
			params.add("client_name=" + StringUtils.encode(app_name));
		String hostname = connection.useHost() ? connection.getHostname() : DEFAULT_HOST;
		try {
			Response response = post(hostname, ENDPOINT_REGISTER_APP, null, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				String client_id = json.getString("client_id");
				String client_secret = json.getString("client_secret");
				String authLink = hostname + ENDPOINT_AUTHORIZE_APP + "?scope=" + AUTH_SCOPES + "&response_type=code&redirect_uri=" + REDIRECT_URI + "&client_id=" + client_id;
				return new ConnectionResult(authLink, client_id, client_secret);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Account loginApp(ConnectionUpdate connection, String pin) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("client_id=" + connection.getOauthConsumerToken());
		params.add("client_secret=" + connection.getOauthTokenSecret());
		params.add("grant_type=authorization_code");
		params.add("code=" + StringUtils.encode(pin));
		params.add("redirect_uri=" + REDIRECT_URI);
		params.add("scope=" + AUTH_SCOPES);
		String hostname = connection.useHost() ? connection.getHostname() : DEFAULT_HOST;
		try {
			Response response = post(hostname, ENDPOINT_LOGIN_APP, null, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				String bearer = json.getString("access_token");
				Credentials credentials = getCredentials(hostname, bearer);
				MastodonAccount account = new MastodonAccount(credentials.getId(), hostname, bearer, connection.getOauthConsumerToken(), connection.getOauthTokenSecret());
				settings.setLogin(account, false);
				User user = showUser(credentials.getId());
				account.setUser(user);
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
		params.add("acct=" + StringUtils.encode(name));
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
		return getUsers(ENDPOINT_FOLLOW_REQUESTS, cursor, new ArrayList<>());
	}


	@Override
	public Users getOutgoingFollowRequests(long cursor) throws MastodonException {
		throw new MastodonException("not supported!");
	}


	@Override
	public void acceptFollowRequest(long id) throws ConnectionException {
		try {
			Response response = get(ENDPOINT_FOLLOW_REQUESTS + "/" + id + "/authorize", new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void rejectFollowRequest(long id) throws ConnectionException {
		try {
			Response response = get(ENDPOINT_FOLLOW_REQUESTS + "/" + id + "/reject", new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
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
		if (settings.getPublicTimeline().equals(GlobalSettings.TIMELINE_LOCAL))
			params.add("local=true");
		else if (settings.getPublicTimeline().equals(GlobalSettings.TIMELINE_REMOTE))
			params.add("remote=true");
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
		if (settings.getPublicTimeline().equals(GlobalSettings.TIMELINE_LOCAL))
			params.add("local=true");
		else if (settings.getPublicTimeline().equals(GlobalSettings.TIMELINE_REMOTE))
			params.add("remote=true");
		return getStatuses(ENDPOINT_PUBLIC_TIMELINE, params, minId, maxId);
	}


	@Override
	public StatusEditHistoy getStatusEditHistory(long id) throws ConnectionException {
		try {
			StatusEditHistoy result = new StatusEditHistoy();
			Response response = get(ENDPOINT_STATUS + id + "/history", new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONArray(body.string());
				long currentId = settings.getLogin().getId();
				for (int i = 0; i < array.length(); i++) {
					result.add(new EditedMastodonStatus(array.getJSONObject(i), currentId));
				}
			}
			return result;
		} catch (IOException | JSONException exception) {
			throw new MastodonException(exception);
		}
	}


	@Override
	public Hashtags getHashtags() throws MastodonException {
		Hashtags result = getHashtags(ENDPOINT_TRENDS, new ArrayList<>());
		Collections.sort(result);
		return result;
	}


	@Override
	public Hashtags searchHashtags(String search) throws MastodonException {
		List<String> params = new ArrayList<>();
		if (search.startsWith("#"))
			params.add("q=" + StringUtils.encode(search.substring(1)));
		else
			params.add("q=" + StringUtils.encode(search));
		params.add("type=hashtags");
		Hashtags result = getHashtags(ENDPOINT_SEARCH_TIMELINE, params);
		Collections.sort(result);
		return result;
	}


	@Override
	public Hashtags showHashtagFollowing(long cursor) throws ConnectionException {
		List<String> params = new ArrayList<>();
		if (cursor != 0L)
			params.add("max_id=" + cursor);
		return getHashtags(ENDPOINT_HASHTAG_FOLLOWING, params);
	}


	@Override
	public Hashtags showHashtagFeaturing() throws ConnectionException {
		return getHashtags(ENDPOINT_HASHTAG_FEATURE, new ArrayList<>());
	}


	@Override
	public Hashtags showHashtagSuggestions() throws ConnectionException {
		return getHashtags(ENDPOINT_HASHTAG_FEATURE + "/suggestions", new ArrayList<>());
	}


	@Override
	public Hashtag showHashtag(String name) throws ConnectionException {
		try {
			if (name.startsWith("#"))
				name = name.substring(1);
			return createTag(get(ENDPOINT_HASHTAG + StringUtils.encode(name), new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Hashtag followHashtag(String name) throws ConnectionException {
		try {
			if (name.startsWith("#"))
				name = name.substring(1);
			return createTag(post(ENDPOINT_HASHTAG + StringUtils.encode(name) + "/follow", new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Hashtag unfollowHashtag(String name) throws ConnectionException {
		try {
			if (name.startsWith("#"))
				name = name.substring(1);
			return createTag(post(ENDPOINT_HASHTAG + StringUtils.encode(name) + "/unfollow", new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Hashtag featureHashtag(String name) throws ConnectionException {
		try {
			if (name.startsWith("#"))
				name = name.substring(1);
			List<String> params = new ArrayList<>();
			params.add("name=" + StringUtils.encode(name));
			return createTag(post(ENDPOINT_HASHTAG_FEATURE, params));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Hashtag unfeatureHashtag(long id) throws ConnectionException {
		try {
			return createTag(delete(ENDPOINT_HASHTAG_FEATURE + "/" + id, new ArrayList<>()));
		} catch (IOException e) {
			throw new MastodonException(e);
		}
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
	public Statuses getStatusReplies(long id, long minId, long maxId) throws MastodonException {
		Statuses statusThreads = getStatuses(ENDPOINT_STATUS + id + "/context", new ArrayList<>(0), minId, maxId);
		Statuses result = new Statuses();
		for (Status status : statusThreads) {
			// Mastodon doesn't support min/max ID.
			if (status.getRepliedStatusId() == id && (minId == 0L || status.getId() > minId) && (maxId == 0L || status.getId() < maxId)) {
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
	@Nullable
	public Status updateStatus(StatusUpdate update, List<Long> mediaIds) throws MastodonException {
		List<String> params = new ArrayList<>();
		// add identifier to prevent duplicate posts
		params.add("Idempotency-Key=" + System.currentTimeMillis() / 5000L);
		if (update.isSensitive())
			params.add("sensitive=true");
		if (update.isSpoiler())
			params.add("spoiler_text=true");
		if (update.getText() != null)
			params.add("status=" + StringUtils.encode(update.getText()));
		if (!update.getLanguageCode().isEmpty())
			params.add("language=" + update.getLanguageCode());
		if (update.getReplyId() != 0L)
			params.add("in_reply_to_id=" + update.getReplyId());
		if (update.getVisibility() == Status.VISIBLE_DIRECT)
			params.add("visibility=direct");
		else if (update.getVisibility() == Status.VISIBLE_PRIVATE)
			params.add("visibility=private");
		else if (update.getVisibility() == Status.VISIBLE_UNLISTED)
			params.add("visibility=unlisted");
		else if (update.getVisibility() == Status.VISIBLE_PUBLIC)
			params.add("visibility=public");
		if (update.getScheduleTime() != 0L) {
			String dateFormat = ISODateTimeFormat.dateTimeNoMillis().print(update.getScheduleTime());
			params.add("scheduled_at=" + StringUtils.encode(dateFormat));
		}
		// add media IDs of previously uploaded media files (status create first)
		if (!mediaIds.isEmpty()) {
			for (long mediaId : mediaIds) {
				params.add("media_ids[]=" + mediaId);
			}
		}
		// add media keys of existing online media files (status edit)
		else {
			for (String mediaKey : update.getMediaKeys()) {
				params.add("media_ids[]=" + mediaKey);
			}
		}
		if (update.getPoll() != null) {
			PollUpdate poll = update.getPoll();
			for (String option : poll.getOptions())
				params.add("poll[options][]=" + StringUtils.encode(option));
			params.add("poll[expires_in]=" + poll.getDuration());
			if (poll.multipleChoiceEnabled()) {
				params.add("poll[multiple]=true");
			} else {
				params.add("poll[multiple]=false");
			}
			if (poll.hideTotalVotes()) {
				params.add("poll[hide_totals]=true");
			} else {
				params.add("poll[hide_totals]=false");
			}
		}
		try {
			Response response;
			if (update.getStatusId() != 0L)
				response = put(ENDPOINT_STATUS + update.getStatusId(), params);
			else
				response = post(ENDPOINT_STATUS, params);
			if (response.code() == 200) {
				if (update.getScheduleTime() == 0L)
					return createStatus(response);
				return null; // when scheduling, ScheduledStatus will be returned from API instead, which is not compatible to Status
			}
			throw new MastodonException(response);
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public ScheduledStatuses getScheduledStatuses(long minId, long maxId) throws ConnectionException {
		try {
			List<String> params = new ArrayList<>();
			if (minId != 0L)
				params.add("since_id=" + minId);
			if (maxId != 0L)
				params.add("max_id=" + maxId);
			params.add("limit=" + settings.getListSize());
			Response response = get(ENDPOINT_SCHEDULED_STATUS, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray jsonArray = new JSONArray(body.string());
				ScheduledStatuses result = new ScheduledStatuses();
				for (int i = 0; i < jsonArray.length(); i++) {
					result.add(new ScheduledMastodonStatus(jsonArray.getJSONObject(i)));
				}
				return result;
			}
			throw new MastodonException(response);
		} catch (JSONException | IOException exception) {
			throw new MastodonException(exception);
		}
	}


	@Override
	public ScheduledStatus updateScheduledStatus(long id, long schedule) throws ConnectionException {
		try {
			List<String> params = new ArrayList<>();
			String dateFormat = ISODateTimeFormat.dateTimeNoMillis().print(schedule);
			params.add("scheduled_at=" + StringUtils.encode(dateFormat));
			Response response = put(ENDPOINT_SCHEDULED_STATUS + "/" + id, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new ScheduledMastodonStatus(json);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException exception) {
			throw new MastodonException(exception);
		}
	}


	@Override
	public void cancelScheduledStatus(long id) throws ConnectionException {
		try {
			Response response = delete(ENDPOINT_SCHEDULED_STATUS + "/" + id, new ArrayList<>());
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException exception) {
			throw new MastodonException(exception);
		}
	}


	@Override
	public Domains getDomainBlocks(long cursor) throws ConnectionException {
		try {
			List<String> params = new ArrayList<>();
			params.add("limit=" + settings.getListSize());
			if (cursor != 0L)
				params.add("max_id=" + cursor);
			Response response = get(ENDPOINT_DOMAIN_BLOCK, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONArray(body.string());
				long[] cursors = getCursors(response);
				Domains result = new Domains(cursors[0], cursors[1]);
				for (int i = 0; i < array.length(); i++) {
					result.add(array.getString(i));
				}
				return result;
			}
			throw new MastodonException(response);

		} catch (JSONException | IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void blockDomain(String domain) throws ConnectionException {
		try {
			List<String> params = new ArrayList<>();
			params.add("domain=" + StringUtils.encode(domain));
			Response response = post(ENDPOINT_DOMAIN_BLOCK, params);
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public void unblockDomain(String domain) throws ConnectionException {
		try {
			List<String> params = new ArrayList<>();
			params.add("domain=" + StringUtils.encode(domain));
			Response response = delete(ENDPOINT_DOMAIN_BLOCK, params);
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public UserList updateUserlist(UserListUpdate update) throws MastodonException {
		List<String> params = new ArrayList<>();
		params.add("title=" + StringUtils.encode(update.getTitle()));
		if (update.getPolicy() == UserList.LIST)
			params.add("replies_policy=list");
		else if (update.getPolicy() == UserList.FOLLOWED)
			params.add("replies_policy=followed");
		else if (update.getPolicy() == UserList.NONE)
			params.add("replies_policy=none");
		if (update.isExclusive())
			params.add("exclusive=true");
		try {
			if (update.getId() != 0L)
				return createUserlist(put(ENDPOINT_USERLIST + update.getId(), params));
			return createUserlist(post(ENDPOINT_USERLIST, params));
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
	public Filters getFilter() throws ConnectionException {
		try {
			Response response = get(ENDPOINT_FILTER, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray array = new JSONArray(body.string());
				Filters result = new Filters();
				for (int i = 0; i < array.length(); i++) {
					result.add(new MastodonFilter(array.getJSONObject(i)));
				}
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException exception) {
			throw new MastodonException(exception);
		}
	}


	@Override
	public Filter updateFilter(FilterUpdate update) throws ConnectionException {
		try {
			List<String> params = new ArrayList<>();
			params.add("title=" + StringUtils.encode(update.getTitle()));
			if (update.getExpirationTime() > 0)
				params.add("expires_in=" + update.getExpirationTime());
			if (update.filterHomeSet())
				params.add("context[]=home");
			if (update.filterNotificationSet())
				params.add("context[]=notifications");
			if (update.filterPublicSet())
				params.add("context[]=public");
			if (update.filterThreadSet())
				params.add("context[]=thread");
			if (update.filterUserSet())
				params.add("context[]=account");
			if (update.getFilterAction() == Filter.ACTION_WARN)
				params.add("filter_action=warn");
			else if (update.getFilterAction() == Filter.ACTION_HIDE)
				params.add("filter_action=hide");
			// add keywords to filter
			for (int i = 0; i < update.getKeywords().length; i++) {
				String keyword = update.getKeywords()[i];
				if (!keyword.trim().isEmpty()) {
					// add existing keyword IDs to prevent duplicates
					if (i < update.getKeywordIds().length) {
						params.add("keywords_attributes[][id]=" + update.getKeywordIds()[i]);
					}
					if (keyword.startsWith("\"") && keyword.endsWith("\"")) {
						params.add("keywords_attributes[][keyword]=" + StringUtils.encode(keyword.substring(1, keyword.length() - 1)));
						params.add("keywords_attributes[][whole_word]=true");
					} else {
						params.add("keywords_attributes[][keyword]=" + StringUtils.encode(keyword));
						params.add("keywords_attributes[][whole_word]=false");
					}
				}
			}
			// remove unused keyword IDs
			if (update.getKeywordIds().length > update.getKeywords().length) {
				for (int i = update.getKeywords().length; i < update.getKeywordIds().length; i++) {
					params.add("keywords_attributes[][id]=" + update.getKeywordIds()[i]);
					params.add("keywords_attributes[][_destroy]=true");
				}
			}
			Response response;
			if (update.getId() != 0L) {
				response = put(ENDPOINT_FILTER + '/' + update.getId(), params);
			} else {
				response = post(ENDPOINT_FILTER, params);
			}
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonFilter(json);
			}
			throw new MastodonException(response);
		} catch (JSONException | IOException exception) {
			throw new MastodonException(exception);
		}
	}


	@Override
	public void deleteFilter(long id) throws ConnectionException {
		try {
			Response response = delete(ENDPOINT_FILTER + '/' + id, new ArrayList<>());
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException exception) {
			throw new MastodonException(exception);
		}
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
					return new MediaStatus(stream, mime, "");
				}
			}
			throw new MastodonException(response);
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Credentials getCredentials() throws ConnectionException {
		return getCredentials(settings.getLogin().getHostname(), settings.getLogin().getBearerToken());
	}


	@Override
	public User updateCredentials(UserUpdate update) throws MastodonException {
		List<String> params = new ArrayList<>();
		List<InputStream> streams = new ArrayList<>();
		List<String> keys = new ArrayList<>();

		params.add("display_name=" + StringUtils.encode(update.getName()));
		params.add("note=" + StringUtils.encode(update.getDescription()));
		params.add("locked=" + update.privacyEnabled());
		params.add("source[sensitive]=" + update.isSensitive());
		if (update.getProfileImageMedia() != null) {
			streams.add(update.getProfileImageMedia().getStream());
			keys.add("avatar");
		}
		if (update.getBannerImageMedia() != null) {
			streams.add(update.getBannerImageMedia().getStream());
			keys.add("header");
		}
		if (!update.getLanguageCode().isEmpty()) {
			params.add("source[language]=" + update.getLanguageCode());
		}
		if (update.getStatusVisibility() == Status.VISIBLE_PUBLIC) {
			params.add("source[privacy]=public");
		} else if (update.getStatusVisibility() == Status.VISIBLE_PRIVATE) {
			params.add("source[privacy]=private");
		} else if (update.getStatusVisibility() == Status.VISIBLE_UNLISTED) {
			params.add("source[privacy]=unlisted");
		} else if (update.getStatusVisibility() == Status.VISIBLE_DIRECT) {
			params.add("source[privacy]=direct");
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
	public long updateMedia(MediaStatus mediaUpdate) throws MastodonException {
		try {
			List<String> params = new ArrayList<>();
			if (!mediaUpdate.getDescription().isEmpty())
				params.add("description=" + StringUtils.encode(mediaUpdate.getDescription()));
			Response response = post(ENDPOINT_UPLOAD_MEDIA, params, mediaUpdate.getStream(), "file");
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
	public WebPush updatePush(PushUpdate pushUpdate) throws ConnectionException {
		try {
			// initialize encryption as required by Mastodon API (but not used yet)
			KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
			ECGenParameterSpec spec = new ECGenParameterSpec("prime256v1");
			generator.initialize(spec);
			KeyPair keyPair = generator.generateKeyPair();
			byte[] privKeyData = keyPair.getPrivate().getEncoded();
			byte[] pubKeyData = keyPair.getPublic().getEncoded();
			byte[] serializedPubKey = serializeRawPublicKey((ECPublicKey) keyPair.getPublic());
			String encodedPublicKey = Base64.encodeToString(serializedPubKey, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
			String pushPrivateKey = Base64.encodeToString(privKeyData, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
			String pushPublicKey = Base64.encodeToString(pubKeyData, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
			String randomString = StringUtils.getRandomString();
			//
			List<String> params = new ArrayList<>();
			params.add("subscription[endpoint]=" + StringUtils.encode(pushUpdate.getHost()));
			params.add("subscription[keys][p256dh]=" + encodedPublicKey);
			params.add("subscription[keys][auth]=" + randomString);
			params.add("data[alerts][mention]=" + pushUpdate.mentionsEnabled());
			params.add("data[alerts][favourite]=" + pushUpdate.favoriteEnabled());
			params.add("data[alerts][reblog]=" + pushUpdate.repostEnabled());
			params.add("data[alerts][follow]=" + pushUpdate.followEnabled());
			params.add("data[alerts][follow_request]=" + pushUpdate.followRequestEnabled());
			params.add("data[alerts][poll]=" + pushUpdate.pollEnabled());
			params.add("data[alerts][status]=" + pushUpdate.statusPostEnabled());
			params.add("data[alerts][update]=" + pushUpdate.statusEditEnabled());
			if (pushUpdate.getPolicy() == WebPush.POLICY_ALL)
				params.add("data[policy]=all");
			else if (pushUpdate.getPolicy() == WebPush.POLICY_FOLLOWER)
				params.add("data[policy]=follower");
			else if (pushUpdate.getPolicy() == WebPush.POLICY_FOLLOWING)
				params.add("data[policy]=followed");
			else if (pushUpdate.getPolicy() == WebPush.POLICY_NONE)
				params.add("data[policy]=none");
			Response response = post(ENDPOINT_PUSH_UPDATE, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				MastodonPush result = new MastodonPush(json);
				result.setKeys(pushPublicKey, pushPrivateKey);
				result.setAuthSecret(randomString);
				result.setPolicy(pushUpdate.getPolicy());
				return result;
			}
			throw new MastodonException(response);
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | JSONException | IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Notifications getNotifications(long minId, long maxId) throws ConnectionException {
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
			List<String> params = new ArrayList<>();
			params.add("lang=" + Locale.getDefault().getLanguage()); // set system language as destiny for translation
			Response response = post(ENDPOINT_STATUS + id + "/translate", params);
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


	@Override
	public void createReport(ReportUpdate update) throws ConnectionException {
		try {
			List<String> params = new ArrayList<>();
			params.add("account_id=" + update.getUserId());
			for (long statusId : update.getStatusIds())
				params.add("status_ids[]=" + statusId);
			for (long ruleId : update.getRuleIds())
				params.add("rule_ids[]=" + ruleId);
			if (!update.getComment().trim().isEmpty())
				params.add("comment=" + StringUtils.encode(update.getComment()));
			if (update.getCategory() == ReportUpdate.CATEGORY_OTHER)
				params.add("category=other");
			else if (update.getCategory() == ReportUpdate.CATEGORY_SPAM)
				params.add("category=spam");
			else if (update.getCategory() == ReportUpdate.CATEGORY_VIOLATION)
				params.add("category=violation");
			if (update.getForward())
				params.add("forward=true");
			Response response = post(ENDPOINT_REPORT, params);
			if (response.code() != 200) {
				throw new MastodonException(response);
			}
		} catch (IOException e) {
			throw new MastodonException(e);
		}
	}


	@Override
	public Rules getRules() throws ConnectionException {
		try {
			Response response = get(ENDPOINT_GET_RULES, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONArray jsonArray = new JSONArray(body.string());
				Rules rules = new Rules(jsonArray.length());
				for (int i = 0; i < jsonArray.length(); i++) {
					try {
						rules.add(new MastodonRule(jsonArray.getJSONObject(i)));
					} catch (JSONException e) {
						if (BuildConfig.DEBUG) {
							e.printStackTrace();
						}
					}
				}
				return rules;
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
	private Credentials getCredentials(String host, @NonNull String bearer) throws MastodonException {
		try {
			Response response = get(host, ENDPOINT_VERIFY_CREDENTIALS, bearer, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonCredentials(json);
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
	 * @param endpoint Endpoint to use
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
	 * call Trend/Hashtag endpoint and create trend result
	 *
	 * @param endpoint Endpoint to use
	 * @param params   additional parameters
	 * @return trend list
	 */
	private Hashtags getHashtags(String endpoint, List<String> params) throws MastodonException {
		try {
			params.add("limit=" + settings.getListSize());
			Response response = get(endpoint, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				String jsonStr = body.string();
				JSONArray jsonArray;
				if (jsonStr.startsWith("[")) {
					jsonArray = new JSONArray(jsonStr);
				} else {
					jsonArray = new JSONObject(jsonStr).getJSONArray("hashtags");
				}
				long[] cursors = getCursors(response);
				Hashtags result = new Hashtags(cursors[0], cursors[1]);
				for (int i = 0; i < jsonArray.length(); i++) {
					MastodonHashtag item = new MastodonHashtag(jsonArray.getJSONObject(i));
					item.setRank(i + 1);
					result.add(item);
				}
				Collections.sort(result);
				return result;
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
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
	private Notifications createNotifications(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				long currentId = settings.getLogin().getId();
				JSONArray json = new JSONArray(body.string());
				Notifications result = new Notifications();
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
	 * create trend from response
	 *
	 * @param response response from a trend endpoint
	 * @return trend information
	 */
	private Hashtag createTag(Response response) throws MastodonException {
		try {
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonHashtag(json);
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
	 * @param inputStream input stream to upload a file
	 * @param addToKey    upload stream key
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
				} else {
					m = PATTERN_SINCE_ID.matcher(headerStr);
					if (m.find()) {
						String min_id_str = headerStr.substring(m.start() + 9, m.end());
						cursors[0] = Long.parseLong(min_id_str);
					}
				}
				m = PATTERN_MAX_ID.matcher(headerStr);
				if (m.find()) {
					String max_id_str = headerStr.substring(m.start() + 7, m.end());
					cursors[1] = Long.parseLong(max_id_str);
				}
			} catch (NumberFormatException exception) {
				if (BuildConfig.DEBUG) {
					exception.printStackTrace();
				}
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

	/**
	 *
	 */
	private byte[] serializeRawPublicKey(ECPublicKey key) {
		ECPoint point = key.getW();
		byte[] x = point.getAffineX().toByteArray();
		byte[] y = point.getAffineY().toByteArray();
		if (x.length > 32)
			x = Arrays.copyOfRange(x, x.length - 32, x.length);
		if (y.length > 32)
			y = Arrays.copyOfRange(y, y.length - 32, y.length);
		byte[] result = new byte[65];
		result[0] = 4;
		System.arraycopy(x, 0, result, 1 + (32 - x.length), x.length);
		System.arraycopy(y, 0, result, result.length - y.length, y.length);
		return result;
	}
}