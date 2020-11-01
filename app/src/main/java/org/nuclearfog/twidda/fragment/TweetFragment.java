package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.TweetActivity;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.adapter.TweetAdapter.TweetClickListener;
import org.nuclearfog.twidda.backend.TweetListLoader;
import org.nuclearfog.twidda.backend.TweetListLoader.Action;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_NAME;

/**
 * #Fragment class for a list of tweets
 */
public class TweetFragment extends ListFragment implements TweetClickListener {

    /**
     * Key to define what type of tweets should be loaded
     * {@link #TWEET_FRAG_HOME}, {@link #TWEET_FRAG_MENT}, {@link #TWEET_FRAG_TWEETS}, {@link #TWEET_FRAG_FAVORS}
     * {@link #TWEET_FRAG_ANSWER}, {@link #TWEET_FRAG_SEARCH}, {@link #TWEET_FRAG_LIST}
     */
    public static final String KEY_FRAG_TWEET_MODE = "tweet_mode";

    /**
     * Key to define a search string such as username or text
     */
    public static final String KEY_FRAG_TWEET_SEARCH = "tweet_search";

    /**
     * Key to define a tweet ID to get replies
     */
    public static final String KEY_FRAG_TWEET_ID = "tweet_id";

    /**
     * Key to return an ID of a removed tweet
     */
    public static final String INTENT_TWEET_REMOVED_ID = "tweet_removed_id";

    public static final int TWEET_FRAG_HOME = 1;
    public static final int TWEET_FRAG_MENT = 2;
    public static final int TWEET_FRAG_TWEETS = 3;
    public static final int TWEET_FRAG_FAVORS = 4;
    public static final int TWEET_FRAG_ANSWER = 5;
    public static final int TWEET_FRAG_SEARCH = 6;
    public static final int TWEET_FRAG_LIST = 7;

    public static final int CLEAR_LIST = -1;
    public static final int RETURN_TWEET_CHANGED = 1;
    private static final int REQUEST_TWEET_CHANGED = 2;

    private TweetListLoader tweetTask;
    private TweetAdapter adapter;
    private GlobalSettings settings;


    @Override
    protected void onCreate() {
        settings = GlobalSettings.getInstance(requireContext());
    }


    @Override
    public void onStart() {
        super.onStart();
        if (tweetTask == null) {
            load(0, 0, CLEAR_LIST);
            setRefresh(true);
        }
    }


    @Override
    protected void onReset() {
        load(0, 0, CLEAR_LIST);
        setRefresh(true);
    }


    @Override
    public void onDestroy() {
        if (tweetTask != null && tweetTask.getStatus() == RUNNING) {
            tweetTask.cancel(true);
        }
        super.onDestroy();
    }


    @Override
    protected TweetAdapter initAdapter() {
        adapter = new TweetAdapter(this, settings);
        return adapter;
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
        if (intent != null && reqCode == REQUEST_TWEET_CHANGED && returnCode == RETURN_TWEET_CHANGED) {
            long removedTweetId = intent.getLongExtra(INTENT_TWEET_REMOVED_ID, 0);
            adapter.remove(removedTweetId);
        }
        super.onActivityResult(reqCode, returnCode, intent);
    }


    @Override
    protected void onReload() {
        if (tweetTask != null && tweetTask.getStatus() != RUNNING) {
            long sinceId = 0;
            if (!adapter.isEmpty())
                sinceId = adapter.getItemId(0);
            load(sinceId, 0, 0);
        }
    }


    @Override
    public void onTweetClick(Tweet tweet) {
        if (!isRefreshing()) {
            if (tweet.getEmbeddedTweet() != null)
                tweet = tweet.getEmbeddedTweet();
            Intent tweetIntent = new Intent(requireContext(), TweetActivity.class);
            tweetIntent.putExtra(KEY_TWEET_ID, tweet.getId());
            tweetIntent.putExtra(KEY_TWEET_NAME, tweet.getUser().getScreenname());
            startActivityForResult(tweetIntent, REQUEST_TWEET_CHANGED);
        }
    }


    @Override
    public void onHolderClick(long sinceId, long maxId, int pos) {
        if (tweetTask != null && tweetTask.getStatus() != RUNNING) {
            load(sinceId, maxId, pos);
        }
    }


    /**
     * Set Tweet data to list
     *
     * @param tweets List of tweets
     * @param pos    position where tweets should be added
     */
    public void setData(List<Tweet> tweets, int pos) {
        if (pos == CLEAR_LIST) {
            adapter.replaceAll(tweets);
        } else {
            adapter.insertAt(tweets, pos);
        }
        setRefresh(false);
    }


    /**
     * called from {@link TweetListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(@Nullable EngineException error) {
        if (error != null)
            ErrorHandler.handleFailure(requireContext(), error);
        adapter.disableLoading();
        setRefresh(false);
    }


    /**
     * load content into the list
     *
     * @param sinceId ID where to start at
     * @param maxId   ID where to stop
     * @param index   index where tweet list should be added
     */
    private void load(long sinceId, long maxId, int index) {
        Bundle param = getArguments();
        if (param != null) {
            int mode = param.getInt(KEY_FRAG_TWEET_MODE, 0);
            long id = param.getLong(KEY_FRAG_TWEET_ID, 0);
            String search = param.getString(KEY_FRAG_TWEET_SEARCH, "");
            Action action = Action.NONE;

            switch (mode) {
                case TWEET_FRAG_HOME:
                    action = Action.TL_HOME;
                    break;

                case TWEET_FRAG_MENT:
                    action = Action.TL_MENT;
                    break;

                case TWEET_FRAG_TWEETS:
                    action = Action.USR_TWEETS;
                    break;

                case TWEET_FRAG_FAVORS:
                    action = Action.USR_FAVORS;
                    break;

                case TWEET_FRAG_ANSWER:
                    if (tweetTask != null || settings.getAnswerLoad())
                        action = Action.TWEET_ANS;
                    else
                        action = Action.DB_ANS;
                    break;

                case TWEET_FRAG_SEARCH:
                    action = Action.TWEET_SEARCH;
                    break;

                case TWEET_FRAG_LIST:
                    action = Action.LIST;
                    break;
            }
            tweetTask = new TweetListLoader(this, action, id, search, index);
            tweetTask.execute(sinceId, maxId);
        }
    }
}