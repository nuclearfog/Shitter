package org.nuclearfog.twidda.backend.api;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
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
public class TwitterImpl {

    public static final String HASH = "HMAC-SHA256";
    private static final String OAUTH = "1.0";

    private static final String API = "https://api.twitter.com/";
    private static final String REQUEST_TOKEN = API + "oauth/request_token";
    private static final String AUTHENTICATE = API + "oauth/authenticate";
    private static final String OAUTH_VERIFIER = API + "oauth/access_token";
    private static final String CREDENTIALS = API + "1.1/account/verify_credentials.json";

    private static TwitterImpl instance;

    private GlobalSettings settings;
    private Tokens tokens;

    private OkHttpClient client;


    private TwitterImpl(Context context) {
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
    public static TwitterImpl get(Context context) {
        if (instance == null) {
            instance = new TwitterImpl(context);
        }
        return instance;
    }

    /**
     * request login page url and generate forst token
     *
     * @return url to the login page
     */
    public String getRequestURL() throws IOException {
        Response response = post(REQUEST_TOKEN, "oauth_callback=oob");
        if (response.code() == 200 && response.body() != null) {
            String res = response.body().string();
            Uri uri = Uri.parse(AUTHENTICATE + "?" + res);
            String token = uri.getQueryParameter("oauth_token");
            tokens.setTokens(token);
            return AUTHENTICATE + "?oauth_token=" + token;
        } else {
            // todo add exception
        }
        return "";
    }

    /**
     * login to twitter using pin and add store access tokens
     *
     * @param pin pin from the login website
     */
    public void login(String pin) throws IOException, JSONException {
        Response response = post(OAUTH_VERIFIER, "oauth_verifier=" + pin, "oauth_token=" + tokens.getToken());
        if (response.code() == 200 && response.body() != null) {
            String res = response.body().string();
            Uri uri = Uri.parse(OAUTH_VERIFIER + "?" + res);
            String token = uri.getQueryParameter("oauth_token");
            String tokenSec = uri.getQueryParameter("oauth_token_secret");
            tokens.setTokens(token, tokenSec);
            settings.setUserId(getCredentials().getId());
        } else {
            // todo add exception
        }
    }

    /**
     * get credentials of the current user
     *
     * @return current user
     */
    public User getCredentials() throws IOException, JSONException {
        Response response = get(CREDENTIALS);
        if (response.code() == 200 && response.body() != null) {
            JSONObject json = new JSONObject(response.body().string());
            return new UserV1(json, -1);
        } else {
            throw new IOException("");
        }
    }

    /**
     * create and call POST endpoint
     *
     * @param endpoint endpoint url
     * @param add additional parameters
     * @return http resonse
     */
    private Response post(String endpoint, String... add) throws IOException {
        String oauth_sec = "";
        if (settings.isLoggedIn()) {
            oauth_sec = tokens.getTokenSec();
        }
        String param = buildParamString(add);
        param += "&oauth_signature=" + StringTools.signPost(endpoint, param, tokens.getConsumerSec() + "&" +oauth_sec);
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "");
        Request request = new Request.Builder().url(endpoint + "?" + param).post(body).build();
        return client.newCall(request).execute();
    }

    /**
     * create and call GET endpoint
     *
     * @param endpoint endpoint url
     * @param add additional parameters
     * @return http response
     */
    private Response get(String endpoint, String... add) throws IOException {
        String oauth_sec = "";
        if (settings.isLoggedIn()) {
            oauth_sec = tokens.getTokenSec();
        }
        String param = buildParamString(add);
        param += "&oauth_signature=" + StringTools.signGet(endpoint, param, tokens.getConsumerSec() + "&" + oauth_sec);
        Request request = new Request.Builder().url(endpoint + "?" + param).get().build();
        return client.newCall(request).execute();
    }

    /**
     * build twitter API parameters
     *
     * @param add additional parameters
     * @return parameter string
     */
    private String buildParamString(String... add) {
        // sort parameters
        TreeSet<String> params = new TreeSet<>();
        params.add("oauth_consumer_key=" + tokens.getConsumerKey());
        params.add("oauth_nonce=" + StringTools.getRandomString());
        params.add("oauth_signature_method=" + HASH);
        params.add("oauth_timestamp=" + StringTools.getTimestamp());
        params.add("oauth_version=" + OAUTH);
        params.addAll(Arrays.asList(add));
        if (settings.isLoggedIn()) {
            params.add("oauth_token=" + tokens.getToken());
        }
        // append sorted parameters to string
        StringBuilder param = new StringBuilder();
        for (String e : params)
            param.append(e).append('&');
        return param.deleteCharAt(param.length() - 1).toString();
    }
}
