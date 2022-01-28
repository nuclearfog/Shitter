package org.nuclearfog.twidda.backend.api;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.api.holder.MediaStream;
import org.nuclearfog.twidda.backend.api.holder.ProfileUpdate;
import org.nuclearfog.twidda.backend.api.holder.TweetUpdate;
import org.nuclearfog.twidda.backend.api.holder.UserlistUpdate;
import org.nuclearfog.twidda.backend.lists.Directmessages;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.backend.proxy.ProxyAuthenticator;
import org.nuclearfog.twidda.backend.proxy.UserProxy;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.backend.utils.TLSSocketFactory;
import org.nuclearfog.twidda.backend.utils.Tokens;
import org.nuclearfog.twidda.database.ExcludeDatabase;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * Twitter API 1.1/2.0 implementation
 *
 * @author nuclearfog
 */
public class Twitter implements GlobalSettings.SettingsListener {

    public static final String SIGNATURE_ALG = "HMAC-SHA256";
    private static final String OAUTH = "1.0";
    private static final String API = "https://api.twitter.com/";
    private static final String UPLOAD = "https://upload.twitter.com/";
    private static final String DOWNLOAD = "https://ton.twitter.com/";
    private static final String AUTHENTICATE = API + "oauth/authenticate";
    public static final String REQUEST_URL = AUTHENTICATE + "?oauth_token=";
    private static final String REQUEST_TOKEN = API + "oauth/request_token";
    private static final String OAUTH_VERIFIER = API + "oauth/access_token";
    private static final String CREDENTIALS = API + "1.1/account/verify_credentials.json";
    private static final String USER_LOOKUP = API + "1.1/users/show.json";
    private static final String USER_FOLLOWING = API + "1.1/friends/list.json";
    private static final String USER_FOLLOWER = API + "1.1/followers/list.json";
    private static final String USER_SEARCH = API + "1.1/users/search.json";
    private static final String USER_LIST_MEMBER = API + "1.1/lists/members.json";
    private static final String USER_LIST_SUBSCRIBER = API + "1.1/lists/subscribers.json";
    private static final String BLOCK_LIST = API + "1.1/blocks/list.json";
    private static final String BLOCK_ID_LIST = API + "1.1/blocks/ids.json";
    private static final String MUTES_LIST = API + "1.1/mutes/users/list.json";
    private static final String SHOW_TWEET = API + "1.1/statuses/show.json";
    private static final String SHOW_HOME = API + "1.1/statuses/home_timeline.json";
    private static final String SHOW_MENTIONS = API + "1.1/statuses/mentions_timeline.json";
    private static final String USER_TIMELINE = API + "1.1/statuses/user_timeline.json";
    private static final String USER_FAVORITS = API + "1.1/favorites/list.json";
    private static final String USER_FOLLOW = API + "1.1/friendships/create.json";
    private static final String USER_UNFOLLOW = API + "1.1/friendships/destroy.json";
    private static final String USER_BLOCK = API + "1.1/blocks/create.json";
    private static final String USER_UNBLOCK = API + "1.1/blocks/destroy.json";
    private static final String USER_MUTE = API + "1.1/mutes/users/create.json";
    private static final String USER_UNMUTE = API + "1.1/mutes/users/destroy.json";
    private static final String TWEETS_LIST = API + "1.1/lists/statuses.json";
    private static final String TWEET_SEARCH = API + "1.1/search/tweets.json";
    private static final String TWEET_FAVORITE = API + "1.1/favorites/create.json";
    private static final String TWEET_UNFAVORITE = API + "1.1/favorites/destroy.json";
    private static final String TWEET_RETWEET = API + "1.1/statuses/retweet/";
    private static final String TWEET_UNRETWEET = API + "1.1/statuses/unretweet/";
    private static final String TWEET_UPLOAD = API + "1.1/statuses/update.json";
    private static final String TWEET_DELETE = API + "1.1/statuses/destroy/";
    private static final String TRENDS = API + "1.1/trends/place.json";
    private static final String LOCATIONS = API + "1.1/trends/available.json";
    private static final String USERLIST_SHOW = API + "1.1/lists/show.json";
    private static final String USERLIST_FOLLOW = API + "1.1/lists/subscribers/create.json";
    private static final String USERLIST_UNFOLLOW = API + "1.1/lists/subscribers/destroy.json";
    private static final String USERLIST_CREATE = API + "1.1/lists/create.json";
    private static final String USERLIST_UPDATE = API + "1.1/lists/update.json";
    private static final String USERLIST_DESTROY = API + "1.1/lists/destroy.json";
    private static final String USERLIST_OWNERSHIP = API + "1.1/lists/list.json";
    private static final String USERLIST_MEMBERSHIP = API + "1.1/lists/memberships.json";
    private static final String USERLIST_ADD_USER = API + "1.1/lists/members/create.json";
    private static final String USERLIST_DEL_USER = API + "1.1/lists/members/destroy.json";
    private static final String RELATION = API + "1.1/friendships/show.json";
    private static final String DIRECTMESSAGE = API + "1.1/direct_messages/events/list.json";
    private static final String DIRECTMESSAGE_CREATE = API + "1.1/direct_messages/events/new.json";
    private static final String DIRECTMESSAGE_DELETE = API + "1.1/direct_messages/events/destroy.json";
    private static final String MEDIA_UPLOAD = UPLOAD + "1.1/media/upload.json";
    private static final String PROFILE_UPDATE = API + "1.1/account/update_profile.json";
    private static final String PROFILE_UPDATE_IMAGE = API + "1.1/account/update_profile_image.json";
    private static final String PROFILE_UPDATE_BANNER = API + "1.1/account/update_profile_banner.json";

    /**
     * type of the media upload used by okHttp
     */
    private static final MediaType TYPE_STREAM = MediaType.parse("application/octet-stream");

    /**
     * To upload big files like videos, files must be chunked in segments.
     * Twitter can handle up to 1000 segments with max 5MB.
     */
    private static final int CHUNK_MAX_BYTES = 1024 * 2048;

    private static Twitter instance;
    private static boolean notifySettingsChange = false;

    private OkHttpClient client;
    private GlobalSettings settings;
    private ExcludeDatabase filterList;
    private Tokens tokens;

    /**
     * initiate singleton instance
     *
     * @param context context used to initiate databases
     */
    private Twitter(Context context) {
        settings = GlobalSettings.getInstance(context);
        tokens = Tokens.getInstance(context);
        filterList = new ExcludeDatabase(context);
        settings.addSettingsChangeListener(this);
        // init okhttp
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS);
        // setup proxy
        builder.proxy(UserProxy.get(settings));
        builder.proxyAuthenticator(new ProxyAuthenticator(settings));
        // enable experimental TLS 1.2 support for old android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init((KeyStore) null);
                X509TrustManager manager = (X509TrustManager) factory.getTrustManagers()[0];
                builder.sslSocketFactory(new TLSSocketFactory(), manager);
            } catch (Exception e) {
                // ignore, try with default setting
            }
        }
        client = builder.build();
        notifySettingsChange = false;
    }

    /**
     * get singleton instance
     *
     * @return instance of this class
     */
    public static Twitter get(Context context) {
        if (notifySettingsChange || instance == null) {
            instance = new Twitter(context);
        }
        return instance;
    }


    @Override
    public void onSettingsChange() {
        notifySettingsChange = true;
    }

    /**
     * request temporary access token to pass it to the Twitter login page
     *
     * @return a temporary access token created by Twitter
     */
    public String getRequestToken() throws TwitterException {
        try {
            Response response = post(REQUEST_TOKEN, new ArrayList<>(1));
            if (response.code() == 200 && response.body() != null) {
                String res = response.body().string();
                // extrect oauth_token from url
                Uri uri = Uri.parse(AUTHENTICATE + "?" + res);
                return uri.getQueryParameter("oauth_token");
            }
            throw new TwitterException(response);
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    /**
     * login to twitter using pin and add store access tokens
     *
     * @param pin pin from the login website
     */
    public User login(String oauth_token, String pin) throws TwitterException {
        try {
            List<String> params = new ArrayList<>(3);
            params.add("oauth_verifier=" + pin);
            params.add("oauth_token=" + oauth_token);
            Response response = post(OAUTH_VERIFIER, params);
            if (response.code() == 200 && response.body() != null) {
                String res = response.body().string();
                // extrect tokens from link
                Uri uri = Uri.parse(OAUTH_VERIFIER + "?" + res);
                settings.setAccessToken(uri.getQueryParameter("oauth_token"));
                settings.setTokenSecret(uri.getQueryParameter("oauth_token_secret"));
                settings.setUserId(Long.parseLong(uri.getQueryParameter("user_id")));
                settings.setogin(true);
                return getCredentials();
            }
            throw new TwitterException(response);
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    /**
     * get credentials of the current user
     *
     * @return current user
     */
    public User getCredentials() throws TwitterException {
        try {
            Response response = get(CREDENTIALS, new ArrayList<>(1));
            if (response.body() != null && response.code() == 200) {
                JSONObject json = new JSONObject(response.body().string());
                return new UserV1(json);
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * lookup user and return user information
     *
     * @param id ID of the user
     * @return user information
     */
    public User showUser(long id) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        params.add("user_id=" + id);
        return getUser1(USER_LOOKUP, params);
    }

    /**
     * lookup user and return user information
     *
     * @param screen_name screen name of the user
     * @return user information
     */
    public User showUser(String screen_name) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        if (screen_name.startsWith("@"))
            screen_name = screen_name.substring(1);
        params.add("screen_name=" + StringTools.encode(screen_name));
        return getUser1(USER_LOOKUP, params);
    }

    /**
     * create a list of users a specified user is following
     *
     * @param userId ID of the user
     * @param cursor cursor value used to parse the list
     * @return list of users
     */
    public Users getFollowing(long userId, long cursor) throws TwitterException {
        List<String> params = new ArrayList<>(5);
        params.add("user_id=" + userId);
        params.add("cursor=" + cursor);
        return getUsers1(USER_FOLLOWING, params);
    }

    /**
     * create a list of users following a specified user
     *
     * @param userId ID of the user
     * @param cursor cursor value used to parse the list
     * @return list of users
     */
    public Users getFollower(long userId, long cursor) throws TwitterException {
        List<String> params = new ArrayList<>(5);
        params.add("user_id=" + userId);
        params.add("cursor=" + cursor);
        return getUsers1(USER_FOLLOWER, params);
    }

    /**
     * create a list of user list members
     *
     * @param listId ID of the list
     * @param cursor cursor value used to parse the list
     * @return list of users
     */
    public Users getListMember(long listId, long cursor) throws TwitterException {
        List<String> params = new ArrayList<>(5);
        params.add("list_id=" + listId);
        params.add("cursor=" + cursor);
        return getUsers1(USER_LIST_MEMBER, params);
    }

    /**
     * create a list of user list subscriber
     *
     * @param listId ID of the list
     * @param cursor cursor value used to parse the list
     * @return list of users
     */
    public Users getListSubscriber(long listId, long cursor) throws TwitterException {
        List<String> params = new ArrayList<>(5);
        params.add("list_id=" + listId);
        params.add("cursor=" + cursor);
        return getUsers1(USER_LIST_SUBSCRIBER, params);
    }

    /**
     * get relationship information to an user
     *
     * @param userId ID of the user
     * @return relationship infirmation
     */
    public Relation getRelationToUser(long userId) throws TwitterException {
        List<String> params = new ArrayList<>(5);
        params.add("source_id=" + settings.getCurrentUserId());
        params.add("target_id=" + userId);
        try {
            Response response = get(RELATION, params);
            if (response.body() != null && response.code() == 200) {
                JSONObject json = new JSONObject(response.body().string());
                return new RelationV1(json);
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * follow a specific user
     *
     * @param userId ID of the user
     * @return updated user information
     */
    public User followUser(long userId) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("user_id=" + userId);
        return getUser1(USER_FOLLOW, params);
    }

    /**
     * unfollow a specific user
     *
     * @param userId ID of the user
     * @return updated user information
     */
    public User unfollowUser(long userId) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("user_id=" + userId);
        return getUser1(USER_UNFOLLOW, params);
    }

    /**
     * get block list of the current user
     *
     * @param cursor cursor value used to parse the list
     * @return list of users
     */
    public Users getBlockedUsers(long cursor) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("cursor=" + cursor);
        return getUsers1(BLOCK_LIST, params);
    }

    /**
     * block specific user
     *
     * @param userId ID of the user
     * @return updated user information
     */
    public User blockUser(long userId) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("user_id=" + userId);
        User user = getUser1(USER_BLOCK, params);
        filterList.addUser(userId);
        return user;
    }

    /**
     * block specific user
     *
     * @param screen_name screen name of the user
     * @return updated user information
     */
    public User blockUser(String screen_name) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        if (screen_name.startsWith("@"))
            screen_name = screen_name.substring(1);
        params.add("screen_name=" + StringTools.encode(screen_name));
        User user = getUser1(USER_BLOCK, params);
        filterList.addUser(user.getId());
        return user;
    }

    /**
     * unclock specific user
     *
     * @param userId ID of the user
     * @return updated user information
     */
    public User unblockUser(long userId) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("user_id=" + userId);
        return getUser1(USER_UNBLOCK, params);
    }

    /**
     * get mute list of the current user
     *
     * @param cursor cursor value used to parse the list
     * @return list of users
     */
    public Users getMutedUsers(long cursor) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("cursor=" + cursor);
        return getUsers1(MUTES_LIST, params);
    }

    /**
     * mute specific user
     *
     * @param userId ID of the user
     * @return updated user information
     */
    public User muteUser(long userId) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("user_id=" + userId);
        return getUser1(USER_MUTE, params);
    }

    /**
     * mute specific user
     *
     * @param screen_name screen name of the user
     * @return updated user information
     */
    public User muteUser(String screen_name) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        if (screen_name.startsWith("@"))
            screen_name = screen_name.substring(1);
        params.add("screen_name=" + StringTools.encode(screen_name));
        return getUser1(USER_MUTE, params);
    }

    /**
     * mute specific user
     *
     * @param userId ID of the user
     * @return updated user information
     */
    public User unmuteUser(long userId) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        params.add("user_id=" + userId);
        return getUser1(USER_UNMUTE, params);
    }

    /**
     * get users retweeting a tweet
     *
     * @param tweetId ID of the tweet
     * @return user list
     */
    public Users getRetweetingUsers(long tweetId) throws TwitterException {
        String endpoint = API + "2/tweets/" + tweetId + "/retweeted_by";
        return getUsers2(endpoint);
    }

    /**
     * get users liking a tweet
     *
     * @param tweetId ID of the tweet
     * @return user list
     */
    public Users getLikingUsers(long tweetId) throws TwitterException {
        String endpoint = API + "2/tweets/" + tweetId + "/liking_users";
        return getUsers2(endpoint);
    }

    /**
     * search tweets matching a search string
     *
     * @param search search string
     * @param minId  get tweets with ID above the min ID
     * @param maxId  get tweets with ID under the max ID
     * @return list of tweets matching the search string
     */
    public List<Tweet> searchTweets(String search, long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(7);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        params.add("q=" + StringTools.encode(search + " +exclude:retweets"));
        params.add("result_type=recent");
        List<Tweet> result = getTweets1(TWEET_SEARCH, params);
        if (settings.filterResults())
            filterTweets(result);
        return result;
    }

    /**
     * search for users matching a search string
     *
     * @param search search string
     * @param page   page of the search results
     * @return list of users
     */
    public Users searchUsers(String search, long page) throws TwitterException {
        // search endpoint only supports pages parameter
        long currentPage = page > 0 ? page : 1;
        long nextPage = currentPage + 1;

        List<String> params = new ArrayList<>(4);
        params.add("q=" + StringTools.encode(search));
        params.add("page=" + currentPage);
        Users result = getUsers1(USER_SEARCH, params);
        // notice that there are no more results
        // if result size is less than the requested size
        if (result.size() < settings.getListSize())
            nextPage = 0;
        if (settings.filterResults())
            filterUsers(result);
        result.setCursors(currentPage - 1, nextPage);
        return result;
    }

    /**
     * get location trends
     *
     * @param id world ID
     * @return trend list
     */
    public List<Trend> getTrends(int id) throws TwitterException {
        List<String> params = new ArrayList<>(2);
        params.add("id=" + id);
        try {
            Response response = get(TRENDS, params);
            if (response.body() != null && response.code() == 200) {
                JSONArray json = new JSONArray(response.body().string());
                JSONArray trends = json.getJSONObject(0).getJSONArray("trends");
                List<Trend> result = new ArrayList<>(trends.length() + 1);
                for (int pos = 0; pos < trends.length(); pos++) {
                    JSONObject trend = trends.getJSONObject(pos);
                    result.add(new TrendV1(trend, pos + 1));
                }
                return result;
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * get available locations for trends
     *
     * @return list of locations
     */
    public List<Location> getLocations() throws TwitterException {
        try {
            Response response = get(LOCATIONS, new ArrayList<>(0));
            if (response.body() != null && response.code() == 200) {
                JSONArray locations = new JSONArray(response.body().string());
                List<Location> result = new ArrayList<>(locations.length() + 1);
                for (int pos = 0; pos < locations.length(); pos++) {
                    JSONObject location = locations.getJSONObject(pos);
                    result.add(new LocationV1(location));
                }
                return result;
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * show current user's home timeline
     *
     * @param minId get tweets with ID above the min ID
     * @param maxId get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getHomeTimeline(long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(7);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 0)
            params.add("max_id=" + maxId);
        return getTweets1(SHOW_HOME, params);
    }

    /**
     * show current user's home timeline
     *
     * @param minId get tweets with ID above the min ID
     * @param maxId get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getMentionTimeline(long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(7);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        return getTweets1(SHOW_MENTIONS, params);
    }

    /**
     * show the timeline of an user
     *
     * @param userId ID of the user
     * @param minId  get tweets with ID above the min ID
     * @param maxId  get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getUserTimeline(long userId, long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(8);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        params.add("user_id=" + userId);
        return getTweets1(USER_TIMELINE, params);
    }

    /**
     * show the timeline of an user
     *
     * @param screen_name screen name of the user (without '@')
     * @param minId       get tweets with ID above the min ID
     * @param maxId       get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getUserTimeline(String screen_name, long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(8);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        params.add("screen_name=" + StringTools.encode(screen_name));
        return getTweets1(USER_TIMELINE, params);
    }

    /**
     * show the favorite tweets of an user
     *
     * @param userId ID of the user
     * @param minId  get tweets with ID above the min ID
     * @param maxId  get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getUserFavorits(long userId, long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(8);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        params.add("user_id=" + userId);
        return getTweets1(USER_FAVORITS, params);
    }

    /**
     * show the favorite tweets of an user
     *
     * @param screen_name screen name of the user (without '@')
     * @param minId       get tweets with ID above the min ID
     * @param maxId       get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getUserFavorits(String screen_name, long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(8);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        params.add("screen_name=" + StringTools.encode(screen_name));
        return getTweets1(USER_FAVORITS, params);
    }

    /**
     * return tweets from an user list
     *
     * @param listId ID of the list
     * @param minId  get tweets with ID above the min ID
     * @param maxId  get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getUserlistTweets(long listId, long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(8);
        if (minId > 0)
            params.add("since_id=" + minId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        params.add("list_id=" + listId);
        return getTweets1(TWEETS_LIST, params);
    }

    /**
     * get replies of a tweet
     *
     * @param screen_name screen name of the tweet author
     * @param tweetId     Id of the tweet
     * @param minId       get tweets with ID above the min ID
     * @param maxId       get tweets with ID under the max ID
     * @return list of tweets
     */
    public List<Tweet> getTweetReplies(String screen_name, long tweetId, long minId, long maxId) throws TwitterException {
        List<String> params = new ArrayList<>(7);
        if (minId > 0)
            params.add("since_id=" + minId);
        else
            params.add("since_id=" + tweetId);
        if (maxId > 1)
            params.add("max_id=" + maxId);
        if (screen_name.startsWith("@"))
            screen_name = screen_name.substring(1);
        params.add("result_type=recent");
        params.add("q=" + StringTools.encode("to:" + StringTools.encode(screen_name) + " +exclude:retweets"));
        List<Tweet> result = getTweets1(TWEET_SEARCH, params);
        List<Tweet> replies = new LinkedList<>();
        for (Tweet reply : result) {
            if (reply.getReplyId() == tweetId) {
                replies.add(reply);
            }
        }
        if (settings.filterResults())
            filterTweets(replies);
        return replies;
    }

    /**
     * lookup tweet by ID
     *
     * @param tweetId tweet ID
     * @return tweet information
     */
    public Tweet showTweet(long tweetId) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        params.add("id=" + tweetId);
        return getTweet1(SHOW_TWEET, params);
    }

    /**
     * favorite specific tweet
     *
     * @param tweetId Tweet ID
     * @return updated tweet
     */
    public Tweet favoriteTweet(long tweetId) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        params.add("id=" + tweetId);
        TweetV1 result = getTweet1(TWEET_FAVORITE, params);
        result.setFavorite(true);
        return result;
    }

    /**
     * remove tweet from favorits
     *
     * @param tweetId Tweet ID
     * @return updated tweet
     */
    public Tweet unfavoriteTweet(long tweetId) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        params.add("id=" + tweetId);
        TweetV1 result = getTweet1(TWEET_UNFAVORITE, params);
        result.setFavorite(false);
        return result;
    }

    /**
     * retweet specific tweet
     *
     * @param tweetId Tweet ID
     * @return updated tweet
     */
    public Tweet retweetTweet(long tweetId) throws TwitterException {
        TweetV1 result = getTweet1(TWEET_RETWEET + tweetId + ".json", new ArrayList<>(2));
        result.setRetweet(true);
        return result;
    }

    /**
     * remove retweet
     *
     * @param tweetId ID of the retweeted tweet
     * @return updated tweet
     */
    public Tweet unretweetTweet(long tweetId) throws TwitterException {
        TweetV1 result = getTweet1(TWEET_UNRETWEET + tweetId + ".json", new ArrayList<>(2));
        result.setRetweet(false);
        return result;
    }

    /**
     * remove tweet of the authenticating user
     *
     * @param tweetId tweet ID
     */
    public void deleteTweet(long tweetId) throws TwitterException {
        getTweet1(TWEET_DELETE + tweetId + ".json", new ArrayList<>(2));
    }

    /**
     * upload tweet with additional attachment
     *
     * @param update tweet update information
     */
    public void uploadTweet(TweetUpdate update, long[] mediaIds) throws TwitterException {
        List<String> params = new ArrayList<>(2);
        params.add("status=" + StringTools.encode(update.getText()));
        if (update.getReplyId() > 0)
            params.add("in_reply_to_status_id=" + update.getReplyId());
        if (mediaIds != null && mediaIds.length > 0) {
            StringBuilder buf = new StringBuilder();
            for (long id : mediaIds)
                buf.append(id).append("%2C");
            String idStr = buf.substring(0, buf.length() - 3);
            params.add("media_ids=" + idStr);
        }
        if (update.hasLocation()) {
            String lat = Double.toString(update.getLatitude());
            String lon = Double.toString(update.getLongitude());
            params.add("lat=" + StringTools.encode(lat));
            params.add("long=" + StringTools.encode(lon));
        }
        getTweet1(TWEET_UPLOAD, params);
    }

    /**
     * create userlist
     *
     * @param update Userlist information
     * @return updated user list
     */
    public UserList createUserlist(UserlistUpdate update) throws TwitterException {
        List<String> params = new ArrayList<>(5);
        params.add("name=" + StringTools.encode(update.getTitle()));
        params.add("description=" + StringTools.encode(update.getDescription()));
        if (update.isPublic())
            params.add("mode=public");
        else
            params.add("mode=private");
        return getUserlist1(USERLIST_CREATE, params);
    }

    /**
     * update existing userlist
     *
     * @param update Userlist update
     * @return updated user list
     */
    public UserList updateUserlist(UserlistUpdate update) throws TwitterException {
        List<String> params = new ArrayList<>(6);
        params.add("list_id=" + update.getId());
        params.add("name=" + StringTools.encode(update.getTitle()));
        params.add("description=" + StringTools.encode(update.getDescription()));
        if (update.isPublic())
            params.add("mode=public");
        else
            params.add("mode=private");
        return getUserlist1(USERLIST_UPDATE, params);
    }

    /**
     * return userlist information
     *
     * @param listId ID of the list
     * @return userlist information
     */
    public UserList getUserlist1(long listId) throws TwitterException {
        List<String> params = new ArrayList<>(2);
        params.add("list_id=" + listId);
        return getUserlist1(USERLIST_SHOW, params);
    }

    /**
     * follow an userlist
     *
     * @param listId ID of the list
     * @return userlist information
     */
    public UserList followUserlist(long listId) throws TwitterException {
        List<String> params = new ArrayList<>(2);
        params.add("list_id=" + listId);
        UserListV1 result = getUserlist1(USERLIST_FOLLOW, params);
        result.setFollowing(true);
        return result;
    }

    /**
     * unfollow an userlist
     *
     * @param listId ID of the list
     * @return userlist information
     */
    public UserList unfollowUserlist(long listId) throws TwitterException {
        List<String> params = new ArrayList<>(2);
        params.add("list_id=" + listId);
        UserListV1 result = getUserlist1(USERLIST_UNFOLLOW, params);
        result.setFollowing(false);
        return result;
    }

    /**
     * delete an userlist
     *
     * @param listId ID of the list
     * @return removed userlist
     */
    public UserList deleteUserlist(long listId) throws TwitterException {
        List<String> params = new ArrayList<>(2);
        params.add("list_id=" + listId);
        return getUserlist1(USERLIST_DESTROY, params);
    }

    /**
     * return userlists an user is owning or following
     *
     * @param userId      ID of the user
     * @param screen_name screen name of the user (without '@')
     * @return list of userlists
     */
    public UserLists getUserListOwnerships(long userId, String screen_name) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        if (userId > 0)
            params.add("user_id=" + userId);
        else
            params.add("screen_name=" + StringTools.encode(screen_name));
        UserLists result = getUserlists(USERLIST_OWNERSHIP, params);
        result.setCursors(0, 0); // this endpoint doesn't support cursors
        return result;
    }

    /**
     * return userlists an user is added to
     *
     * @param userId      ID of the user
     * @param screen_name screen name of the user (without '@')
     * @param cursor      list cursor
     * @return list of userlists
     */
    public UserLists getUserListMemberships(long userId, String screen_name, long cursor) throws TwitterException {
        List<String> params = new ArrayList<>(5);
        if (userId > 0)
            params.add("user_id=" + userId);
        else
            params.add("screen_name=" + StringTools.encode(screen_name));
        params.add("count=" + settings.getListSize());
        params.add("cursor=" + cursor);
        return getUserlists(USERLIST_MEMBERSHIP, params);
    }

    /**
     * add user to existing userlist
     *
     * @param listId      ID of the list
     * @param screen_name screen name
     */
    public void addUserToUserlist(long listId, String screen_name) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        if (screen_name.startsWith("@"))
            screen_name = screen_name.substring(1);
        params.add("list_id=" + listId);
        params.add("screen_name=" + StringTools.encode(screen_name));
        sendPost(USERLIST_ADD_USER, params);
    }

    /**
     * remove user from existing userlist
     *
     * @param listId      ID of the list
     * @param screen_name screen name
     */
    public void removeUserFromUserlist(long listId, String screen_name) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        if (screen_name.startsWith("@"))
            screen_name = screen_name.substring(1);
        params.add("list_id=" + listId);
        params.add("screen_name=" + StringTools.encode(screen_name));
        sendPost(USERLIST_DEL_USER, params);
    }

    /**
     * send directmessage to user
     *
     * @param userId  ID of the user
     * @param message message text
     * @param mediaId ID of uploaded media files or -1 if none
     */
    public void sendDirectmessage(long userId, String message, long mediaId) throws TwitterException {
        try {
            // directmessage endpoint uses JSON structure
            JSONObject data = new JSONObject();
            JSONObject root = new JSONObject();
            JSONObject target = new JSONObject();
            JSONObject msg_create = new JSONObject();
            JSONObject event = new JSONObject();
            target.put("recipient_id", Long.toString(userId));
            msg_create.put("target", target);
            msg_create.put("message_data", data);
            event.put("type", "message_create");
            event.put("message_create", msg_create);
            root.put("event", event);
            if (!message.isEmpty())
                data.put("text", message);
            if (mediaId > 0) {
                JSONObject attachment = new JSONObject();
                JSONObject media = new JSONObject();
                attachment.put("type", "media");
                attachment.put("media", media);
                media.put("id", Long.toString(mediaId));
                data.put("attachment", attachment);
            }
            Response response = post(DIRECTMESSAGE_CREATE, new ArrayList<>(0), root);
            if (response.code() != 200) {
                throw new TwitterException(response);
            }
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * delete directmessage
     *
     * @param messageId ID of the message to delete
     */
    public void deleteDirectmessage(long messageId) throws TwitterException {
        List<String> params = new ArrayList<>(2);
        params.add("id=" + messageId);
        try {
            Response response = delete(DIRECTMESSAGE_DELETE, params);
            if (response.code() < 200 || response.code() >= 300) {
                throw new TwitterException(response);
            }
        } catch (IOException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * get current user's direct messages
     *
     * @param cursor list cursor
     * @return list of direct messages
     */
    public Directmessages getDirectmessages(String cursor) throws TwitterException {
        List<String> params = new ArrayList<>(3);
        params.add("count=" + settings.getListSize());
        if (cursor != null && !cursor.isEmpty())
            params.add("cursor=" + cursor);
        try {
            Response response = get(DIRECTMESSAGE, params);
            if (response.body() != null && response.code() == 200) {
                // init user cache to re-use instances
                Map<Long, User> userCache = new TreeMap<>();
                JSONObject json = new JSONObject(response.body().string());
                String nextCursor = json.optString("next_cursor");
                JSONArray array = json.getJSONArray("events");
                Directmessages result = new Directmessages(cursor, nextCursor);
                for (int pos = 0; pos < array.length(); pos++) {
                    JSONObject item = array.getJSONObject(pos);
                    DirectmessageV1 message = new DirectmessageV1(item);
                    long senderId = message.getSenderId();
                    long receiverId = message.getReceiverId();
                    // cache user instances to reduce API calls
                    if (userCache.containsKey(senderId)) {
                        message.addSender(userCache.get(senderId));
                    } else {
                        User user = showUser(senderId);
                        userCache.put(senderId, user);
                        message.addSender(user);
                    }
                    if (userCache.containsKey(receiverId)) {
                        message.addReceiver(userCache.get(receiverId));
                    } else {
                        User user = showUser(receiverId);
                        userCache.put(receiverId, user);
                        message.addReceiver(user);
                    }
                    result.add(message);
                }
                return result;
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * upload medida file to twitter and generate a media ID
     *
     * @param mediaStream inputstream with MIME type of the media
     * @return media ID
     */
    public long uploadMedia(MediaStream mediaStream) throws TwitterException {
        List<String> params = new ArrayList<>(4);
        try {
            // step 1 INIT
            params.add("command=INIT");
            params.add("media_type=" + mediaStream.getMimeType());
            params.add("total_bytes=" + mediaStream.available());
            Response response = post(MEDIA_UPLOAD, params);
            if (response.code() < 200 || response.code() >= 300 || response.body() == null)
                throw new TwitterException(response);
            JSONObject respone = new JSONObject(response.body().string());
            final long mediaId = Long.parseLong(respone.getString("media_id_string"));

            // step 2 APPEND
            int segmentIndex = 0;
            InputStream stream = mediaStream.getStream();
            while (stream.available() > 0) {
                params.clear();
                params.add("command=APPEND");
                params.add("segment_index=" + segmentIndex++);
                params.add("media_id=" + mediaId);
                response = post(MEDIA_UPLOAD, params, stream, "media", true);
                if (response.code() < 200 || response.code() >= 300)
                    throw new TwitterException(response);
            }

            // step 3 FINALIZE
            params.clear();
            params.add("command=FINALIZE");
            params.add("media_id=" + mediaId);
            response = post(MEDIA_UPLOAD, params);
            if (response.code() < 200 || response.code() >= 300)
                throw new TwitterException(response);
            return mediaId;
        } catch (IOException err) {
            err.printStackTrace();
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * download image from twitter
     *
     * @param link link to the image
     * @return image bitmap
     */
    public MediaStream downloadImage(String link) throws TwitterException {
        try {
            // this type of link requires authentication
            if (link.startsWith(DOWNLOAD)) {
                Response response = get(link, new ArrayList<>(0));
                if (response.code() == 200 && response.body() != null) {
                    MediaType type = response.body().contentType();
                    if (type != null) {
                        String mime = type.toString();
                        InputStream stream = response.body().byteStream();
                        return new MediaStream(stream, mime);
                    }
                }
                throw new TwitterException(response);
            }
            // public link, no authentication required
            else {
                Request request = new Request.Builder().url(link).get().build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200 && response.body() != null) {
                    MediaType type = response.body().contentType();
                    if (type != null) {
                        String mime = type.toString();
                        InputStream stream = response.body().byteStream();
                        return new MediaStream(stream, mime);
                    }
                }
                throw new TwitterException(response);
            }
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    /**
     * updates current user's profile
     *
     * @param update profile update information
     * @return updated user information
     */
    public User updateProfile(ProfileUpdate update) throws TwitterException {
        List<String> params = new ArrayList<>(7);
        params.add("name=" + StringTools.encode(update.getName()));
        params.add("url=" + StringTools.encode(update.getUrl()));
        params.add("location=" + StringTools.encode(update.getLocation()));
        params.add("description=" + StringTools.encode(update.getDescription()));
        return getUser1(PROFILE_UPDATE, params);
    }

    /**
     * update current user's profile image
     *
     * @param inputStream inputstream of the local image file
     */
    public void updateProfileImage(InputStream inputStream) throws TwitterException {
        updateImage(PROFILE_UPDATE_IMAGE, inputStream, "image");
    }

    /**
     * update current user's profile banner image
     *
     * @param inputStream inputstream of the local image file
     */
    public void updateProfileBanner(InputStream inputStream) throws TwitterException {
        updateImage(PROFILE_UPDATE_BANNER, inputStream, "banner");
    }

    /**
     * returns a list of blocked user IDs
     *
     * @return list of IDs
     */
    public List<Long> getIdBlocklist() throws TwitterException {
        try {
            long cursor = -1;
            List<Long> result = new ArrayList<>(100);
            // the API returns up to 5000 blocked user IDs
            // but for bigger lists, we have to parse the whole list
            for (int i = 0; i < 10 && cursor != 0; i++) {
                List<String> params = new ArrayList<>(2);
                params.add("cursor=" + cursor);
                Response response = get(BLOCK_ID_LIST, params);
                if (response.body() != null && response.code() == 200) {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray idArray = json.getJSONArray("ids");
                    cursor = json.optLong("next_cursor");
                    for (int pos = 0; pos < idArray.length(); pos++) {
                        result.add(idArray.getLong(pos));
                    }
                } else {
                    throw new TwitterException(response);
                }
            }
            return result;
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * get tweets using an endpoint
     *
     * @param endpoint endpoint url to fetch the tweets
     * @param params   additional parameters
     * @return list of tweets
     */
    private List<Tweet> getTweets1(String endpoint, List<String> params) throws TwitterException {
        try {
            params.add(TweetV1.EXT_MODE);
            params.add(TweetV1.INCL_RT_ID);
            params.add(TweetV1.INCL_ENTITIES);
            params.add("count=" + settings.getListSize());
            Response response = get(endpoint, params);
            if (response.body() != null && response.code() == 200) {
                JSONArray array;
                String body = response.body().string();
                if (endpoint.equals(TWEET_SEARCH)) // twitter search uses another structure
                    array = new JSONObject(body).getJSONArray("statuses");
                else
                    array = new JSONArray(body);
                long homeId = settings.getCurrentUserId();
                List<Tweet> tweets = new ArrayList<>(array.length() + 1);
                for (int i = 0; i < array.length(); i++)
                    tweets.add(new TweetV1(array.getJSONObject(i), homeId));
                return tweets;
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * return tweet from API 1.1 endpoint
     *
     * @param endpoint to use
     * @param params   additional parameter
     */
    private TweetV1 getTweet1(String endpoint, List<String> params) throws TwitterException {
        try {
            params.add(TweetV1.EXT_MODE);
            params.add(TweetV1.INCL_RT_ID);
            params.add(TweetV1.INCL_ENTITIES);
            Response response;
            if (endpoint.equals(SHOW_TWEET)) {
                response = get(endpoint, params);
            } else {
                response = post(endpoint, params);
            }
            if (response.body() != null && response.code() == 200) {
                JSONObject json = new JSONObject(response.body().string());
                long currentId = settings.getCurrentUserId();
                return new TweetV1(json, currentId);
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * create a list of users using API v 1.1
     *
     * @param endpoint endpoint url to get the user data from
     * @param params   additional parameters
     * @return user list
     */
    private Users getUsers1(String endpoint, List<String> params) throws TwitterException {
        try {
            params.add("count=" + settings.getListSize());
            params.add("skip_status=true");
            Response response = get(endpoint, params);
            if (response.body() != null && response.code() == 200) {
                String jsonResult = response.body().string();
                if (endpoint.equals(USER_SEARCH)) // convert to JSON object
                    jsonResult = "{\"users\":" + jsonResult + '}';
                JSONObject json = new JSONObject(jsonResult);
                JSONArray array = json.getJSONArray("users");
                long prevCursor = json.optLong("previous_cursor", -1);
                long nextCursor = json.optLong("next_cursor", -1);
                Users users = new Users(prevCursor, nextCursor);
                long homeId = settings.getCurrentUserId();
                for (int i = 0; i < array.length(); i++) {
                    users.add(new UserV1(array.getJSONObject(i), homeId));
                }
                return users;
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * create a list of users using API v 2
     *
     * @param endpoint endpoint url to get the user data from
     * @return user list
     */
    private Users getUsers2(String endpoint) throws TwitterException {
        try {
            List<String> params = new ArrayList<>(2);
            params.add(UserV2.PARAMS);
            Response response = get(endpoint, params);
            if (response.body() != null && response.code() == 200) {
                JSONObject json = new JSONObject(response.body().string());
                Users users = new Users();
                // check if result is not empty
                if (json.has("data")) {
                    JSONArray array = json.getJSONArray("data");
                    long homeId = settings.getCurrentUserId();
                    for (int i = 0; i < array.length(); i++) {
                        users.add(new UserV2(array.getJSONObject(i), homeId));
                    }
                }
                return users;
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * send POST request and return updated user information
     *
     * @param endpoint POST endpoint
     * @param params   additional parameters
     * @return user information
     */
    private User getUser1(String endpoint, List<String> params) throws TwitterException {
        try {
            params.add("skip_status=true");
            params.add("include_entities=true");
            Response response;
            if (endpoint.equals(USER_LOOKUP))
                response = get(endpoint, params);
            else
                response = post(endpoint, params);
            if (response.body() != null && response.code() == 200) {
                JSONObject json = new JSONObject(response.body().string());
                long currentId = settings.getCurrentUserId();
                return new UserV1(json, currentId);
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * execute userlist action and return userlist information
     *
     * @param endpoint userlist endpoint to use
     * @param params   additional parameters
     * @return userlist information
     */
    private UserListV1 getUserlist1(String endpoint, List<String> params) throws TwitterException {
        try {
            Response response;
            if (endpoint.equals(USERLIST_SHOW))
                response = get(endpoint, params);
            else
                response = post(endpoint, params);
            if (response.body() != null && response.code() == 200) {
                JSONObject json = new JSONObject(response.body().string());
                long currentId = settings.getCurrentUserId();
                return new UserListV1(json, currentId);
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * get a list of userlists
     *
     * @param endpoint endpoint to get the userlists from
     * @param params   additional parameter
     * @return list of userlists
     */
    private UserLists getUserlists(String endpoint, List<String> params) throws TwitterException {
        params.add("include_entities=true");
        try {
            Response response = get(endpoint, params);
            if (response.body() != null && response.code() == 200) {
                JSONArray array;
                UserLists result = new UserLists();
                String body = response.body().string();
                // add cursors if available
                if (body.startsWith("{")) {
                    JSONObject json = new JSONObject(body);
                    array = json.getJSONArray("lists");
                    long prevCursor = json.optLong("previous_cursor");
                    long nextCursor = json.optLong("next_cursor");
                    result.setCursors(prevCursor, nextCursor);
                } else {
                    array = new JSONArray(body);
                }
                long currentId = settings.getCurrentUserId();
                for (int pos = 0; pos < array.length(); pos++) {
                    JSONObject item = array.getJSONObject(pos);
                    result.add(new UserListV1(item, currentId));
                }
                return result;
            }
            throw new TwitterException(response);
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * update profile images
     *
     * @param endpoint endpoint to use
     * @param input    inputstream of the image file
     * @param key      key name used to identify the type of image
     */
    private void updateImage(String endpoint, InputStream input, String key) throws TwitterException {
        try {
            List<String> params = new ArrayList<>(3);
            params.add("skip_status=true");
            params.add("include_entities=false");
            Response response = post(endpoint, params, input, key, false);
            if (response.code() < 200 || response.code() >= 300) {
                throw new TwitterException(response);
            }
        } catch (IOException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * send post without return value
     *
     * @param endpoint endpoint to use
     * @param params   endpoint parameters
     */
    private void sendPost(String endpoint, List<String> params) throws TwitterException {
        try {
            Response response = post(endpoint, params);
            if (response.code() < 200 || response.code() >= 300) {
                throw new TwitterException(response);
            }
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    /**
     * filter tweets from blocked users
     */
    private void filterTweets(List<Tweet> tweets) {
        Set<Long> exclude = filterList.getExcludeSet();
        for (int pos = tweets.size() - 1; pos >= 0; pos--) {
            if (exclude.contains(tweets.get(pos).getAuthor().getId())) {
                tweets.remove(pos);
            }
        }
    }

    /**
     * remove blocked users from list
     */
    private void filterUsers(List<User> users) {
        Set<Long> exclude = filterList.getExcludeSet();
        for (int pos = users.size() - 1; pos >= 0; pos--) {
            if (exclude.contains(users.get(pos).getId())) {
                users.remove(pos);
            }
        }
    }

    /**
     * send POST request with empty body and create response
     *
     * @param endpoint endpoint url
     * @param params   additional http parameters
     * @return http resonse
     */
    private Response post(String endpoint, List<String> params) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "");
        return post(endpoint, params, body);
    }

    /**
     * send POST request with JSON object and create response
     *
     * @param endpoint endpoint url
     * @param params   additional http parameters
     * @return http resonse
     */
    private Response post(String endpoint, List<String> params, JSONObject json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());
        return post(endpoint, params, body);
    }

    /**
     * send POST request with file and create response
     *
     * @param endpoint      endpoint url
     * @param params        additional http parameters
     * @param enableChunk   true to enable file chunk
     * @return http resonse
     */
    private Response post(String endpoint, List<String> params, InputStream is, String addToKey, boolean enableChunk) throws IOException {
        RequestBody data = new RequestBody() {
            @Override
            public MediaType contentType() {
                return TYPE_STREAM;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                if (enableChunk && is.available() > CHUNK_MAX_BYTES) {
                    sink.write(Okio.buffer(Okio.source(is)), CHUNK_MAX_BYTES);
                } else {
                    sink.writeAll(Okio.buffer(Okio.source(is)));
                }
            }
        };
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(addToKey, "", data).build();
        return post(endpoint, params, body);
    }

    /**
     * send POST request with request body
     *
     * @param endpoint endpoint url
     * @param params   additional http parameters
     * @param body     custom body
     * @return http response
     */
    private Response post(String endpoint, List<String> params, RequestBody body) throws IOException {
        String authHeader = buildHeader("POST", endpoint, params);
        String url = appendParams(endpoint, params);
        Request request = new Request.Builder().url(url).addHeader("Authorization", authHeader).post(body).build();
        return client.newCall(request).execute();
    }

    /**
     * create and call GET endpoint
     *
     * @param endpoint endpoint url
     * @return http response
     */
    private Response get(String endpoint, List<String> params) throws IOException {
        String authHeader = buildHeader("GET", endpoint, params);
        String url = appendParams(endpoint, params);
        Request request = new Request.Builder().url(url).addHeader("Authorization", authHeader).get().build();
        return client.newCall(request).execute();
    }

    /**
     * create and call GET endpoint
     *
     * @param endpoint endpoint url
     * @return http response
     */
    private Response delete(String endpoint, List<String> params) throws IOException {
        String authHeader = buildHeader("DELETE", endpoint, params);
        String url = appendParams(endpoint, params);
        Request request = new Request.Builder().url(url).addHeader("Authorization", authHeader).delete().build();
        return client.newCall(request).execute();
    }

    /**
     * create http header with credentials and signature
     *
     * @param method   endpoint method to call
     * @param endpoint endpoint url
     * @param params   parameter to add to signature
     * @return header string
     */
    private String buildHeader(String method, String endpoint, List<String> params) throws IOException {
        String timeStamp = StringTools.getTimestamp();
        String random = StringTools.getRandomString();
        String signkey = tokens.getConsumerSec() + "&";
        String oauth_token_param = "";

        // init default parameters
        TreeSet<String> sortedParams = new TreeSet<>();
        sortedParams.add("oauth_callback=oob");
        sortedParams.add("oauth_consumer_key=" + tokens.getConsumerKey());
        sortedParams.add("oauth_nonce=" + random);
        sortedParams.add("oauth_signature_method=" + SIGNATURE_ALG);
        sortedParams.add("oauth_timestamp=" + timeStamp);
        sortedParams.add("oauth_version=" + OAUTH);
        // add custom parameters
        sortedParams.addAll(params);

        // only add tokens if there is no login process
        if (!REQUEST_TOKEN.equals(endpoint) && !OAUTH_VERIFIER.equals(endpoint)) {
            sortedParams.add("oauth_token=" + settings.getAccessToken());
            oauth_token_param = ", oauth_token=\"" + settings.getAccessToken() + "\"";
            signkey += settings.getTokenSecret();
        }

        // build string with sorted parameters
        StringBuilder paramStr = new StringBuilder();
        for (String param : sortedParams)
            paramStr.append(param).append('&');
        paramStr.deleteCharAt(paramStr.length() - 1);

        // calculate oauth signature
        String signature = StringTools.sign(method, endpoint, paramStr.toString(), signkey);

        // create header string
        return "OAuth oauth_callback=\"oob\"" +
                ", oauth_consumer_key=\"" + tokens.getConsumerKey() + "\"" +
                ", oauth_nonce=\"" + random + "\"" +
                ", oauth_signature=\"" + signature + "\"" +
                ", oauth_signature_method=\"" + SIGNATURE_ALG + "\"" +
                ", oauth_timestamp=\"" + timeStamp + "\""
                + oauth_token_param +
                ", oauth_version=\"" + OAUTH + "\"";
    }

    /**
     * build url with parameters
     *
     * @param url    url without parameters
     * @param params parameters
     * @return url with parameters
     */
    private String appendParams(String url, List<String> params) {
        if (!params.isEmpty()) {
            StringBuilder result = new StringBuilder(url);
            result.append('?');
            for (String param : params)
                result.append(param).append('&');
            result.deleteCharAt(result.length() - 1);
            return result.toString();
        }
        return url;
    }
}