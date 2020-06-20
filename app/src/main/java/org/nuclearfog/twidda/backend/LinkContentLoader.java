package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.DirectMessage;
import org.nuclearfog.twidda.activity.MainActivity;
import org.nuclearfog.twidda.activity.SearchPage;
import org.nuclearfog.twidda.activity.TweetDetail;
import org.nuclearfog.twidda.activity.TweetPopup;
import org.nuclearfog.twidda.activity.TwitterList;
import org.nuclearfog.twidda.activity.UserProfile;

import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.activity.TweetPopup.KEY_TWEETPOPUP_TEXT;
import static org.nuclearfog.twidda.activity.TwitterList.KEY_USERLIST_OWNER_NAME;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_NAME;

/**
 * This class handles deep links and starts activities to show the content
 *
 * @see MainActivity
 */
public class LinkContentLoader extends AsyncTask<Uri, Integer, LinkContentLoader.DataHolder> {

    private static final Pattern TWEET_PATH = Pattern.compile("[\\w]+/status/\\d+");
    private static final Pattern USER_PATH = Pattern.compile("[\\w]+/?(\\bwith_replies\\b|\\bmedia\\b|\\blikes\\b)?");
    private static final Pattern LIST_PATH = Pattern.compile("[\\w]+/lists");

    private WeakReference<MainActivity> callback;

    public LinkContentLoader(MainActivity callback) {
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setLoading(true);
        }
    }

    @Override
    protected DataHolder doInBackground(Uri[] links) {
        DataHolder dataHolder = null;
        try {
            Uri link = links[0];
            Bundle data = new Bundle();
            String path = link.getPath();
            if (path != null && path.length() > 1) {
                path = path.substring(1);
                if (path.equals("home")) {
                    publishProgress(0);
                } else if (path.equals("i/trends") || path.equals("trends") || path.equals("explore")) {
                    publishProgress(1);
                } else if (path.equals("notifications")) {
                    publishProgress(2);
                } else if (path.equals("messages")) {
                    dataHolder = new DataHolder(null, DirectMessage.class);
                } else if (path.equals("search")) {
                    if (link.isHierarchical()) {
                        String search = link.getQueryParameter("q");
                        if (search != null) {
                            data.putString(KEY_SEARCH_QUERY, search);
                            dataHolder = new DataHolder(data, SearchPage.class);
                        }
                    }
                } else if (path.equals("intent/tweet")) {
                    if (link.isHierarchical()) {
                        String tweet = link.getQueryParameter("text");
                        if (tweet != null) {
                            data.putString(KEY_TWEETPOPUP_TEXT, tweet);
                            dataHolder = new DataHolder(data, TweetPopup.class);
                        }
                    }
                } else if (path.startsWith("hashtag/")) {
                    String search = '#' + path.substring(8);
                    data.putString(KEY_SEARCH_QUERY, search);
                    dataHolder = new DataHolder(data, SearchPage.class);
                } else if (USER_PATH.matcher(path).matches()) {
                    int end = path.indexOf('/');
                    if (end > 0)
                        path = path.substring(0, end);
                    data.putString(KEY_PROFILE_NAME, path);
                    dataHolder = new DataHolder(data, UserProfile.class);
                } else if (TWEET_PATH.matcher(path).matches()) {
                    String username = '@' + path.substring(0, path.indexOf('/'));
                    long tweetId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
                    data.putLong(KEY_TWEET_ID, tweetId);
                    data.putString(KEY_TWEET_NAME, username);
                    dataHolder = new DataHolder(data, TweetDetail.class);
                } else if (LIST_PATH.matcher(path).matches()) {
                    String username = '@' + path.substring(0, path.indexOf('/'));
                    data.putString(KEY_USERLIST_OWNER_NAME, username);
                    dataHolder = new DataHolder(data, TwitterList.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataHolder;
    }


    @Override
    protected void onProgressUpdate(Integer[] pageNo) {
        int page = pageNo[0];
        if (callback.get() != null) {
            callback.get().setTab(page);
        }
    }


    @Override
    protected void onPostExecute(DataHolder result) {
        if (callback.get() != null) {
            callback.get().setLoading(false);
            if (result != null) {
                Intent intent = new Intent(callback.get(), result.activity);
                if (result.data != null)
                    intent.putExtras(result.data);
                callback.get().startActivity(intent);
            }
        }
    }


    /**
     * Holder class for information to start an activity
     */
    class DataHolder {
        @Nullable
        final Bundle data;
        final Class<? extends Activity> activity;

        DataHolder(@Nullable Bundle data, Class<? extends Activity> activity) {
            this.data = data;
            this.activity = activity;
        }
    }
}