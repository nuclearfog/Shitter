package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.TweetFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;


/**
 * Timeline loader Task
 */
public class TweetLoader extends AsyncTask<Object, Void, List<Tweet>> {

    public enum Mode {
        TL_HOME,
        TL_MENT,
        USR_TWEETS,
        USR_FAVORS,
        TWEET_ANS,
        DB_ANS,
        TWEET_SEARCH,
        LIST
    }

    @Nullable
    private TwitterEngine.EngineException twException;
    private Mode mode;
    private WeakReference<TweetFragment> ui;
    private TweetAdapter adapter;
    private TwitterEngine mTwitter;
    private AppDatabase db;


    public TweetLoader(TweetFragment fragment, Mode mode) {
        ui = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        adapter = fragment.getAdapter();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null) {
            ui.get().setRefresh(true);
        }
    }


    @Override
    protected List<Tweet> doInBackground(Object[] param) {
        List<Tweet> tweets = null;
        long sinceId = 1;
        try {
            switch (mode) {
                case TL_HOME:
                    int page = (int) param[0];
                    if (adapter.isEmpty()) {
                        tweets = db.getHomeTimeline();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getHome(page, sinceId);
                            db.storeHomeTimeline(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getHome(page, sinceId);
                        db.storeHomeTimeline(tweets);
                    }
                    break;

                case TL_MENT:
                    page = (int) param[0];
                    if (adapter.isEmpty()) {
                        tweets = db.getMentions();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getMention(page, sinceId);
                            db.storeMentions(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getMention(page, sinceId);
                        db.storeMentions(tweets);
                    }
                    break;

                case USR_TWEETS:
                    long id = (long) param[0];
                    page = (int) param[1];
                    if (adapter.isEmpty()) {
                        tweets = db.getUserTweets(id);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getUserTweets(id, sinceId, page);
                            db.storeUserTweets(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getUserTweets(id, sinceId, page);
                        db.storeUserTweets(tweets);
                    }
                    break;

                case USR_FAVORS:
                    id = (long) param[0];
                    page = (int) param[1];
                    if (adapter.isEmpty()) {
                        tweets = db.getUserFavs(id);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getUserFavs(id, page);
                            db.storeUserFavs(tweets, id);
                        }
                    } else {
                        tweets = mTwitter.getUserFavs(id, page);
                        db.storeUserFavs(tweets, id);
                    }
                    break;

                case DB_ANS:
                    id = (long) param[0];
                    tweets = db.getAnswers(id);
                    break;

                case TWEET_ANS:
                    id = (long) param[0];
                    String search = (String) param[1];
                    if (adapter.isEmpty()) {
                        tweets = db.getAnswers(id);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getAnswers(search, id, sinceId);
                            if (!tweets.isEmpty() && db.containStatus(id))
                                db.storeReplies(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getAnswers(search, id, sinceId);
                        if (!tweets.isEmpty() && db.containStatus(id))
                            db.storeReplies(tweets);
                    }
                    break;

                case TWEET_SEARCH:
                    search = (String) param[0];
                    if (!adapter.isEmpty())
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.searchTweets(search, sinceId);
                    break;

                case LIST:
                    long listId = (long) param[0];
                    page = (int) param[1];
                    if (!adapter.isEmpty())
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getListTweets(listId, sinceId, page);
                    break;
            }
        } catch (TwitterEngine.EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return tweets;
    }


    @Override
    protected void onPostExecute(@Nullable List<Tweet> tweets) {
        if (ui.get() != null) {
            if (tweets != null) {
                if (mode == Mode.USR_FAVORS)
                    adapter.add(tweets); // replace all items
                else
                    adapter.addFirst(tweets);
            }
            if (twException != null)
                Toast.makeText(ui.get().getContext(), twException.getMessageResource(), LENGTH_SHORT).show();
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null) {
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled(@Nullable List<Tweet> tweets) {
        if (ui.get() != null) {
            if (tweets != null)
                adapter.addFirst(tweets);
            ui.get().setRefresh(false);
        }
    }
}