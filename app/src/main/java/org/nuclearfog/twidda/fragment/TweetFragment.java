package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.TweetActivity;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.adapter.TweetAdapter.TweetClickListener;
import org.nuclearfog.twidda.backend.TweetLoader;
import org.nuclearfog.twidda.backend.TweetLoader.ListType;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.model.Tweet;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.util.List;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_DATA;

/**
 * #Fragment class for a list of tweets
 *
 * @author nuclearfog
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

    /**
     * Key to return an ID of a removed tweet
     */
    public static final String INTENT_TWEET_UPDATE_DATA = "tweet_update_data";

    /**
     * setup list for home timeline
     */
    public static final int TWEET_FRAG_HOME = 0xE7028B60;

    /**
     * setup list for mention timeline
     */
    public static final int TWEET_FRAG_MENT = 0x9EC8274D;

    /**
     * setup list for user tweets
     */
    public static final int TWEET_FRAG_TWEETS = 0x4DBEF6CD;

    /**
     * setup list for user favorites
     */
    public static final int TWEET_FRAG_FAVORS = 0x8DE749EC;

    /**
     * setup list for tweet replies
     */
    public static final int TWEET_FRAG_ANSWER = 0xAFB5F1C0;

    /**
     * setup list for search
     */
    public static final int TWEET_FRAG_SEARCH = 0x91A71117;

    /**
     * setup list for user list tweets
     */
    public static final int TWEET_FRAG_LIST = 0x43F518F7;

    /**
     * replace all items from list
     */
    public static final int CLEAR_LIST = -1;

    /**
     * return code if a tweet was not found
     */
    public static final int RETURN_TWEET_NOT_FOUND = 0x8B03DB84;

    /**
     * return code if a tweet was not found
     */
    public static final int RETURN_TWEET_UPDATE = 0x789CD38B;

    /**
     * request code to check for tweet changes
     */
    private static final int REQUEST_TWEET_CHANGED = 0xB90D;

    private TweetLoader tweetTask;
    private TweetAdapter adapter;

    private String search = "";
    private int mode = 0;
    private long id = 0;


    @Override
    protected void onCreate() {
        Bundle param = getArguments();
        if (param != null) {
            mode = param.getInt(KEY_FRAG_TWEET_MODE, 0);
            id = param.getLong(KEY_FRAG_TWEET_ID, 0);
            search = param.getString(KEY_FRAG_TWEET_SEARCH, "");
        }
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
        adapter.clear();
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
        adapter = new TweetAdapter(settings, this);
        return adapter;
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
        if (intent != null && reqCode == REQUEST_TWEET_CHANGED) {
            if (returnCode == RETURN_TWEET_UPDATE) {
                Object data = intent.getSerializableExtra(INTENT_TWEET_UPDATE_DATA);
                if (data instanceof Tweet) {
                    Tweet updateTweet = (Tweet) data;
                    adapter.updateItem(updateTweet);
                }
            } else if (returnCode == RETURN_TWEET_NOT_FOUND) {
                long removedTweetId = intent.getLongExtra(INTENT_TWEET_REMOVED_ID, 0);
                adapter.remove(removedTweetId);
            }
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
            Intent tweetIntent = new Intent(requireContext(), TweetActivity.class);
            tweetIntent.putExtra(KEY_TWEET_DATA, tweet);
            startActivityForResult(tweetIntent, REQUEST_TWEET_CHANGED);
        }
    }


    @Override
    public boolean onHolderClick(long sinceId, long maxId, int pos) {
        if (tweetTask != null && tweetTask.getStatus() != RUNNING) {
            load(sinceId, maxId, pos);
            return true;
        }
        return false;
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
     * called from {@link TweetLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(@Nullable EngineException error) {
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
        ListType listType = ListType.NONE;

        switch (mode) {
            case TWEET_FRAG_HOME:
                listType = ListType.TL_HOME;
                break;

            case TWEET_FRAG_MENT:
                listType = ListType.TL_MENT;
                break;

            case TWEET_FRAG_TWEETS:
                listType = ListType.USR_TWEETS;
                break;

            case TWEET_FRAG_FAVORS:
                listType = ListType.USR_FAVORS;
                break;

            case TWEET_FRAG_ANSWER:
                if (tweetTask != null || settings.replyLoadingEnabled())
                    listType = ListType.REPLIES;
                else
                    listType = ListType.DB_ANS;
                break;

            case TWEET_FRAG_SEARCH:
                listType = ListType.TWEET_SEARCH;
                break;

            case TWEET_FRAG_LIST:
                listType = ListType.USERLIST;
                break;
        }
        tweetTask = new TweetLoader(this, listType, id, search, index);
        tweetTask.execute(sinceId, maxId);
    }
}