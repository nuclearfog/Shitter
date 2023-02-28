package org.nuclearfog.twidda.backend.api.twitter.v2;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.TwitterException;
import org.nuclearfog.twidda.backend.api.twitter.v1.TwitterV1;
import org.nuclearfog.twidda.backend.api.twitter.v2.impl.AccountV2;
import org.nuclearfog.twidda.backend.api.twitter.v2.impl.LocationV2;
import org.nuclearfog.twidda.backend.api.twitter.v2.impl.MediaV2;
import org.nuclearfog.twidda.backend.api.twitter.v2.impl.PollV2;
import org.nuclearfog.twidda.backend.api.twitter.v2.impl.TweetV2;
import org.nuclearfog.twidda.backend.api.twitter.v2.impl.UserV2;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.LocationV2Map;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.MediaV2Map;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.PollV2Map;
import org.nuclearfog.twidda.backend.api.twitter.v2.maps.UserV2Map;
import org.nuclearfog.twidda.backend.helper.ConnectionConfig;
import org.nuclearfog.twidda.backend.helper.Users;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Twitter API version 2.0 extension
 *
 * @author nuclearfog
 */
public class TwitterV2 extends TwitterV1 {

	private static final String TWEET2_LOOKUP = API + "/2/tweets/";
	private static final String TWEET_SEARCH_2 = API + "/2/tweets/search/recent";
	private static final String TWEET_UNI = API + "/2/tweets/";

	/**
	 * initiate singleton instance
	 *
	 * @param context context used to initiate databases
	 */
	public TwitterV2(Context context) {
		super(context);
	}


	@Override
	public Account loginApp(ConnectionConfig connection, String pin) throws TwitterException {
		Account account = super.loginApp(connection, pin);
		return new AccountV2(account);
	}


	@Override
	public Status showStatus(long id) throws TwitterException {
		Status status = super.showStatus(id);
		return getTweet(TWEET2_LOOKUP + id, new ArrayList<>(), status);
	}


	@Override
	public Users getRepostingUsers(long tweetId, long cursor) throws TwitterException {
		String endpoint = TWEET_UNI + tweetId + "/retweeted_by";
		return getUsers(endpoint, new ArrayList<>());
	}


	@Override
	public Users getFavoritingUsers(long tweetId, long cursor) throws TwitterException {
		String endpoint = TWEET_UNI + tweetId + "/liking_users";
		return getUsers(endpoint, new ArrayList<>());
	}


	@Override
	public void muteConversation(long id) throws TwitterException {
		muteStatus(id, true);
	}


	@Override
	public void unmuteConversation(long id) throws TwitterException {
		muteStatus(id, false);
	}


	@Override
	public List<Status> getStatusReplies(long id, long minId, long maxId, String... extras) throws TwitterException {
		List<String> params = new ArrayList<>();
		List<Status> replies = new LinkedList<>();
		params.add("query=" + StringTools.encode("conversation_id:" + id));
		// Note: minId disabled! Twitter refuses API request containing minId of a tweet older than one week
		List<Status> result = getTweets(TWEET_SEARCH_2, params, 0, maxId);
		// chose only the first tweet of a conversation
		for (Status reply : result) {
			if (reply.getRepliedStatusId() == id && reply.getId() > minId) {
				replies.add(reply);
			}
		}
		if (settings.filterResults() && !replies.isEmpty()) {
			filterTweets(replies);
		}
		return replies;
	}


	@Override
	public Poll getPoll(long id) throws ConnectionException {
		throw new TwitterException("not implemented!");
	}

	/**
	 * mute a status from conversation
	 *
	 * @param id   ID of the status
	 * @param hide true to hide the status
	 */
	private void muteStatus(long id, boolean hide) throws TwitterException {
		try {
			RequestBody request = RequestBody.create("{\"hidden\":" + hide + "}", TYPE_JSON);
			Response response = put(TWEET_UNI + id + "/hidden", new ArrayList<>(), request);
			if (response.code() != 200) {
				throw new TwitterException(response);
			}
		} catch (IOException e) {
			throw new TwitterException(e);
		}
	}

	/**
	 * return tweet from API 2.0 endpoint
	 *
	 * @param endpoint to use
	 * @param params   additional parameter
	 */
	private Status getTweet(String endpoint, List<String> params, Status statusCompat) throws TwitterException {
		// enable additional tweet fields
		params.add(TweetV2.FIELDS_EXPANSION);
		params.add(UserV2.FIELDS_USER);
		params.add(MediaV2.FIELDS_MEDIA);
		params.add(PollV2.FIELDS_POLL);
		params.add(LocationV2.FIELDS_PLACE);
		// add metrics information if the author is the current user and the tweet is not older than 28 days and not a retweet/quote
		if (statusCompat.getAuthor().isCurrentUser() && System.currentTimeMillis() - statusCompat.getTimestamp() < 2419200000L
				&& (statusCompat.getEmbeddedStatus() == null || statusCompat.getEmbeddedStatus().getRepostId() <= 0L)) {
			params.add(TweetV2.FIELDS_TWEET_PRIVATE);
		} else {
			params.add(TweetV2.FIELDS_TWEET);
		}
		try {
			Response response;
			if (endpoint.startsWith(TWEET2_LOOKUP)) {
				response = get(endpoint, params);
			} else {
				response = post(endpoint, params);
			}
			ResponseBody body = response.body();
			if (body != null && response.code() == 200) {
				JSONObject json = new JSONObject(body.string());
				UserV2Map userMap = new UserV2Map(json, settings.getLogin().getId());
				MediaV2Map mediaMap = new MediaV2Map(json);
				PollV2Map pollMap = new PollV2Map(json);
				LocationV2Map locationMap = new LocationV2Map(json);
				String host = settings.getLogin().getHostname();
				return new TweetV2(json, userMap, mediaMap, pollMap, locationMap, host, statusCompat);
			}
			throw new TwitterException(response);
		} catch (IOException | JSONException err) {
			throw new TwitterException(err);
		}
	}


	/**
	 * get tweets using an endpoint
	 *
	 * @param endpoint endpoint url to fetch the tweets
	 * @param params   additional parameters
	 * @param minId    minimum tweet ID
	 * @param maxId    maximum tweet ID
	 * @return list of tweets
	 */
	private List<Status> getTweets(String endpoint, List<String> params, long minId, long maxId) throws TwitterException {
		// enable additional tweet fields
		params.add(TweetV2.FIELDS_TWEET);
		params.add(TweetV2.FIELDS_EXPANSION);
		params.add(UserV2.FIELDS_USER);
		params.add(MediaV2.FIELDS_MEDIA);
		params.add(PollV2.FIELDS_POLL);
		params.add(LocationV2.FIELDS_PLACE);
		// set tweet range
		if (minId != 0L)
			params.add("since_id=" + minId);
		if (maxId != 0L)
			params.add("until_id=" + maxId);
		params.add("max_results=" + settings.getListSize());
		try {
			Response response = get(endpoint, params);
			ResponseBody body = response.body();
			if (body != null && response.code() == 200) {
				JSONObject json = new JSONObject(body.string());
				JSONArray data = json.optJSONArray("data");
				if (data != null && data.length() > 0) {
					List<Status> tweets = new ArrayList<>(data.length() + 1);
					UserV2Map userMap = new UserV2Map(json, settings.getLogin().getId());
					MediaV2Map mediaMap = new MediaV2Map(json);
					PollV2Map pollMap = new PollV2Map(json);
					LocationV2Map locationMap = new LocationV2Map(json);
					String host = settings.getLogin().getHostname();
					for (int i = 0; i < data.length(); i++) {
						try {
							Status item = new TweetV2(data.getJSONObject(i), userMap, mediaMap, pollMap, locationMap, host, null);
							tweets.add(item);
						} catch (JSONException e) {
							if (BuildConfig.DEBUG) {
								Log.w("tweet-v2", e);
							}
						}
					}
					return tweets;
				}
				return new ArrayList<>(0);
			}
			throw new TwitterException(response);
		} catch (IOException | JSONException err) {
			throw new TwitterException(err);
		}
	}


	/**
	 * create a list of users using API v 2
	 *
	 * @param endpoint endpoint url to get the user data from
	 * @param params   additional parameters
	 * @return user list
	 */
	private Users getUsers(String endpoint, List<String> params) throws TwitterException {
		// enable additional user fields
		params.add("user.fields=" + UserV2.FIELDS_USER);
		params.add("max_results=" + settings.getListSize());
		// todo implement cursor
		try {
			Response response = get(endpoint, params);
			ResponseBody body = response.body();
			if (body != null && response.code() == 200) {
				JSONObject json = new JSONObject(body.string());
				Users users = new Users(0L, 0L);
				// check if result is not empty
				if (json.has("data")) {
					JSONArray array = json.getJSONArray("data");
					long homeId = settings.getLogin().getId();
					for (int i = 0; i < array.length(); i++) {
						try {
							users.add(new UserV2(array.getJSONObject(i), homeId));
						} catch (JSONException err) {
							if (BuildConfig.DEBUG) {
								Log.w("user-v2", err);
							}
						}
					}
				}
				return users;
			}
			throw new TwitterException(response);
		} catch (IOException | JSONException err) {
			throw new TwitterException(err);
		}
	}
}