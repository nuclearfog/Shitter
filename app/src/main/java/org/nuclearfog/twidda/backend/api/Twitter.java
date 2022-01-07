package org.nuclearfog.twidda.backend.api;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.backend.utils.TLSSocketFactory;
import org.nuclearfog.twidda.backend.utils.Tokens;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.TreeSet;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * new API implementation to replace twitter4j and add version 2.0 support
 *
 * @author nuclearfog
 */
public class Twitter {

    private static final String OAUTH = "1.0";
    private static final String API = "https://api.twitter.com/";
    private static final String AUTHENTICATE = API + "oauth/authenticate";
    private static final String REQUEST_TOKEN = API + "oauth/request_token";
    private static final String OAUTH_VERIFIER = API + "oauth/access_token";
    private static final String CREDENTIALS = API + "1.1/account/verify_credentials.json";
    private static final String USER_LOOKUP = API + "1.1/users/show.json";
    public static final String REQUEST_URL = AUTHENTICATE + "?oauth_token=";
    public static final String SIGNATURE_ALG = "HMAC-SHA256";

    private static Twitter instance;

    private OkHttpClient client;
    private GlobalSettings settings;
    private Tokens tokens;


    private Twitter(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init((KeyStore) null);
                X509TrustManager manager = (X509TrustManager) factory.getTrustManagers()[0];
                client = new OkHttpClient().newBuilder().sslSocketFactory(new TLSSocketFactory(), manager).build();
            } catch (Exception e) {
                client = new OkHttpClient().newBuilder().build();
            }
        } else {
            client = new OkHttpClient().newBuilder().build();
        }
        settings = GlobalSettings.getInstance(context);
        tokens = Tokens.getInstance(context);
    }

    /**
     * get singleton instance
     *
     * @return instance of this class
     */
    public static Twitter get(Context context) {
        if (instance == null) {
            instance = new Twitter(context);
        }
        return instance;
    }

    /**
     * request temporary access token to pass it to the Twitter login page
     *
     * @return a temporary access token created by Twitter
     */
    public String getRequestToken() throws TwitterException {
        try {
            Response response = post(REQUEST_TOKEN);
            if (response.code() == 200 && response.body() != null) {
                String res = response.body().string();
                Uri uri = Uri.parse(AUTHENTICATE + "?" + res);
                return uri.getQueryParameter("oauth_token");
            } else {
                throw new TwitterException(response.code());
            }
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
            if (oauth_token == null)
                throw new TwitterException(TwitterException.TOKEN_NOT_SET);
            String paramPin = "oauth_verifier=" + pin;
            String paramToken = "oauth_token=" + oauth_token;
            Response response = post(OAUTH_VERIFIER, paramPin, paramToken);
            if (response.code() == 200 && response.body() != null) {
                String res = response.body().string();
                // extrect tokens from link
                Uri uri = Uri.parse(OAUTH_VERIFIER + "?" + res);
                settings.setAccessToken(uri.getQueryParameter("oauth_token"));
                settings.setTokenSecret(uri.getQueryParameter("oauth_token_secret"));
                settings.setUserId(Long.parseLong(uri.getQueryParameter("user_id")));
                settings.setogin(true);
                return getCredentials();
            } else {
                throw new TwitterException(response.code());
            }
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    /**
     * lookup single user
     *
     * @param id ID of the user
     * @return user information
     */
    public User showUser(long id) throws TwitterException {
        try {
            String param = "user_id=" + id;
            String extra = "include_entities=true";
            Response response = get(USER_LOOKUP, param, extra);
            if (response.code() == 200 && response.body() != null) {
                JSONObject json = new JSONObject(response.body().string());
                return new UserV1(json, settings.getCurrentUserId());
            } else {
                throw new TwitterException(response.code());
            }
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * get credentials of the current user
     *
     * @return current user
     */
    public User getCredentials() throws TwitterException {
        try {
            Response response = get(CREDENTIALS);
            if (response.code() == 200 && response.body() != null) {
                JSONObject json = new JSONObject(response.body().string());
                return new UserV1(json);
            } else {
                throw new TwitterException(response.code());
            }
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * get users retweeting a tweet
     *
     * @param tweetId ID of the tweet
     * @return user list
     */
    public Users getRetweetingUsers(long tweetId) throws TwitterException {
        String endpoint = API + "2/tweets/" + tweetId + "/retweeted_by";
        return getUsers(endpoint);
    }

    /**
     * get users liking a tweet
     *
     * @param tweetId ID of the tweet
     * @return user list
     */
    public Users getLikingUsers(long tweetId) throws TwitterException {
        String endpoint = API + "2/tweets/" + tweetId + "/liking_users";
        return getUsers(endpoint);
    }

    /**
     * get a list of twitter users
     *
     * @param endpoint endpoint url to get the user data from
     * @return user list
     */
    private Users getUsers(String endpoint) throws TwitterException {
        try {
            Response response = get(endpoint, UserV2.PARAMS);
            if (response.code() == 200 && response.body() != null) {
                JSONObject json = new JSONObject(response.body().string());
                JSONArray array = json.getJSONArray("data");
                Users users = new Users();
                long homeId = settings.getCurrentUserId();
                for (int i = 0 ; i < array.length() ; i++) {
                    users.add(new UserV2(array.getJSONObject(i), homeId));
                }
                return users;
            } else {
                throw new TwitterException(response.code());
            }
        } catch (IOException err) {
            throw new TwitterException(err);
        } catch (JSONException err) {
            throw new TwitterException(err);
        }
    }

    /**
     * create and call POST endpoint
     *
     * @param endpoint endpoint url
     * @return http resonse
     */
    private Response post(String endpoint, String... params) throws IOException {
        String authHeader = buildHeader("POST", endpoint, params);
        String url = appendParams(endpoint, params);
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "");
        Request request = new Request.Builder().url(url).addHeader("Authorization", authHeader).post(body).build();
        return client.newCall(request).execute();
    }

    /**
     * create and call GET endpoint
     *
     * @param endpoint endpoint url
     * @return http response
     */
    private Response get(String endpoint, String... params) throws IOException {
        String authHeader = buildHeader("GET", endpoint, params);
        String url = appendParams(endpoint, params);
        Request request = new Request.Builder().url(url).addHeader("Authorization", authHeader).get().build();
        return client.newCall(request).execute();
    }

    /**
     * create http header with credentials and signature
     *
     * @param method endpoint method to call
     * @param endpoint endpoint url
     * @param params parameter to add to signature
     * @return header string
     */
    private String buildHeader(String method, String endpoint, String... params) {
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
        sortedParams.addAll(Arrays.asList(params));

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
                ", oauth_nonce=\""+ random + "\"" +
                ", oauth_signature=\"" + signature + "\"" +
                ", oauth_signature_method=\""+ SIGNATURE_ALG + "\"" +
                ", oauth_timestamp=\"" + timeStamp + "\""
                + oauth_token_param +
                ", oauth_version=\"" + OAUTH + "\"";
    }

    /**
     * build url with param
     *
     * @param url url without parameters
     * @param params parameters
     * @return url with parameters
     */
    private String appendParams(String url, String[] params) {
        if (params.length > 0) {
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