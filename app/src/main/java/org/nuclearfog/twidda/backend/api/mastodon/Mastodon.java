package org.nuclearfog.twidda.backend.api.mastodon;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonAccount;
import org.nuclearfog.twidda.backend.api.mastodon.impl.MastodonUser;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Implementation of the Mastodon API
 *
 * @author nuclearfog
 */
public class Mastodon implements Connection {

	private static final String DEFAULT_HOST = "https://mastodon.social";

	private static final String AUTH_SCOPES = "read%20write%20follow";
	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	private static final String AUTH_WEBSITE = "https://github.com/nuclearfog/Shitter";
	private static final String AUTH_NAME = "SH1TT3R";

	private static final String REGISTER_APP = "/api/v1/apps";
	private static final String AUTHORIZE_APP = "/oauth/authorize";
	private static final String LOGIN_APP = "/oauth/token";
	private static final String VERIFY_CREDENTIALS = "/api/v1/accounts/verify_credentials";

	MediaType TYPE_TEXT = MediaType.parse("text/plain");


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
			Response response = post(hostname + REGISTER_APP, null, params);
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
		String client_secret = link.getQueryParameter("client_secret");

		params.add("client_id=" + client_id);
		params.add("client_secret=" + client_secret);
		params.add("grant_type=authorization_code");
		params.add("code=" + paramsStr[1]);
		params.add("redirect_uri=" + REDIRECT_URI);
		params.add("scope=" + AUTH_SCOPES);

		try {
			Response response = post(host + LOGIN_APP, null, params);
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				String bearer = json.getString("access_token");
				User user = getCredentials(host, bearer);
				Account account = new MastodonAccount(user, host, bearer, client_id, client_secret);
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

	/**
	 * get information about the current user
	 *
	 * @param host   Mastodon hostname
	 * @param bearer bearer token to use
	 * @return current user information
	 */
	private User getCredentials(String host, @NonNull String bearer) throws MastodonException {
		try {
			Response response = get(host + VERIFY_CREDENTIALS, bearer, new ArrayList<>());
			ResponseBody body = response.body();
			if (response.code() == 200 && body != null) {
				JSONObject json = new JSONObject(body.string());
				return new MastodonUser(json, true);
			}
			throw new MastodonException(response);
		} catch (IOException | JSONException e) {
			throw new MastodonException(e);
		}
	}

	/**
	 * create a GET response
	 *
	 * @param endpoint endpoint url
	 * @param bearer   bearer token or null
	 * @param params   additional parameters
	 * @return GET response
	 */
	private Response get(String endpoint, @Nullable String bearer, List<String> params) throws IOException {
		Request.Builder request = new Request.Builder().url(buildUrl(endpoint, params)).get();
		if (bearer != null) {
			request.addHeader("Authorization", "Bearer " + bearer);
		}
		return client.newCall(request.build()).execute();
	}

	/**
	 * create a POST response
	 *
	 * @param endpoint endpoint url
	 * @param bearer   bearer token or null
	 * @param params   additional parameters
	 * @return POST response
	 */
	private Response post(String endpoint, @Nullable String bearer, List<String> params) throws IOException {
		RequestBody body = RequestBody.create("", TYPE_TEXT);
		Request.Builder request = new Request.Builder().url(buildUrl(endpoint, params)).post(body);
		if (bearer != null) {
			request.addHeader("Authorization", "Bearer " + bearer);
		}
		return client.newCall(request.build()).execute();
	}

	/**
	 * append query parameters to an url
	 *
	 * @param hostname hostname of the url
	 * @param params   additional parameters
	 * @return url with hostname and query parameters
	 */
	private String buildUrl(String hostname, List<String> params) {
		if (!params.isEmpty()) {
			StringBuilder result = new StringBuilder(hostname);
			result.append('?');
			for (String param : params) {
				result.append(param).append('&');
			}
			result.deleteCharAt(result.length() - 1);
			return result.toString();
		}
		return hostname;
	}
}