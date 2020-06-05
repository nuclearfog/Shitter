package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.nuclearfog.twidda.activity.MainActivity;
import org.nuclearfog.twidda.activity.SearchPage;
import org.nuclearfog.twidda.activity.TweetDetail;
import org.nuclearfog.twidda.activity.UserProfile;

import java.lang.ref.WeakReference;

import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_NAME;

/**
 * This class handles deep links and starts activities to show the content
 *
 * @see MainActivity
 */
public class LinkContentLoader extends AsyncTask<Uri, Void, LinkContentLoader.DataHolder> {

    private static final String TWEET_PATH = "[\\w]+/status/\\d+";
    private static final String USER_PATH = "[\\w]+/?(\\bwith_replies\\b|\\bmedia\\b|\\blikes\\b)?";

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
        try {
            Uri link = links[0];
            Bundle data = new Bundle();
            String path = link.getPath();
            if (path != null && path.length() > 1) {
                path = path.substring(1);
                if (path.startsWith("search") && link.isHierarchical()) {
                    String search = link.getQueryParameter("q");
                    if (search != null) {
                        data.putString(KEY_SEARCH_QUERY, search);
                        return new DataHolder(data, SearchPage.class);
                    }
                } else if (path.startsWith("hashtag")) {
                    int cut = path.indexOf("/");
                    if (cut > 0) {
                        String search = '#' + path.substring(cut + 1);
                        data.putString(KEY_SEARCH_QUERY, search);
                        return new DataHolder(data, SearchPage.class);
                    }
                } else if (path.matches(USER_PATH)) {
                    int end = path.indexOf("/");
                    if (end > 0)
                        path = path.substring(0, end);
                    data.putString(KEY_PROFILE_NAME, path);
                    return new DataHolder(data, UserProfile.class);
                } else if (path.matches(TWEET_PATH)) {
                    String name = '@' + path.substring(path.indexOf("/"));
                    long id = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
                    data.putLong(KEY_TWEET_ID, id);
                    data.putString(KEY_TWEET_NAME, name);
                    return new DataHolder(data, TweetDetail.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(DataHolder result) {
        if (callback.get() != null) {
            callback.get().setLoading(false);
            if (result != null) {
                Intent intent = new Intent(callback.get(), result.activity);
                intent.putExtras(result.data);
                callback.get().startActivity(intent);
            }
        }
    }

    class DataHolder {
        final Bundle data;
        final Class<? extends Activity> activity;

        DataHolder(Bundle data, Class<? extends Activity> activity) {
            this.data = data;
            this.activity = activity;
        }
    }
}