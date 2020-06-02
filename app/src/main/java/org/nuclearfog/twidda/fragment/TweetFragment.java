package org.nuclearfog.twidda.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.activity.TweetDetail;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.adapter.TweetAdapter.TweetClickListener;
import org.nuclearfog.twidda.backend.TweetListLoader;
import org.nuclearfog.twidda.backend.TweetListLoader.Action;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_NAME;


public class TweetFragment extends Fragment implements OnRefreshListener, TweetClickListener, FragmentChangeObserver {

    public static final String KEY_FRAG_TWEET_MODE = "tweet_mode";
    public static final String KEY_FRAG_TWEET_SEARCH = "tweet_search";
    public static final String KEY_FRAG_TWEET_ID = "tweet_id";
    public static final String INTENT_TWEET_REMOVED_ID = "tweet_removed_id";

    public static final int TWEET_FRAG_HOME = 0;
    public static final int TWEET_FRAG_MENT = 1;
    public static final int TWEET_FRAG_TWEETS = 2;
    public static final int TWEET_FRAG_FAVORS = 3;
    public static final int TWEET_FRAG_ANSWER = 4;
    public static final int TWEET_FRAG_SEARCH = 5;
    public static final int TWEET_FRAG_LIST = 6;
    public static final int LIST_EMPTY = 1;

    private static final int REQUEST_TWEET_CHANGED = 3;
    public static final int RETURN_TWEET_CHANGED = 4;

    private TweetListLoader tweetTask;
    private GlobalSettings settings;
    private SwipeRefreshLayout reload;
    private RecyclerView list;
    private TweetAdapter adapter;

    private String search;
    private long id;
    private int mode;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle param) {
        Bundle b = getArguments();
        Context context = inflater.getContext();

        if (b != null) {
            mode = b.getInt(KEY_FRAG_TWEET_MODE);
            id = b.getLong(KEY_FRAG_TWEET_ID, -1);
            search = b.getString(KEY_FRAG_TWEET_SEARCH, "");
        }
        settings = GlobalSettings.getInstance(context);
        adapter = new TweetAdapter(this, settings);
        list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setAdapter(adapter);
        reload = new SwipeRefreshLayout(context);
        reload.setOnRefreshListener(this);
        reload.addView(list);
        return reload;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (tweetTask == null)
            load();
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
    }


    @Override
    public void onDestroy() {
        if (tweetTask != null && tweetTask.getStatus() == RUNNING)
            tweetTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
        if (intent != null && reqCode == REQUEST_TWEET_CHANGED && returnCode == RETURN_TWEET_CHANGED) {
            adapter.remove(intent.getLongExtra(INTENT_TWEET_REMOVED_ID, 0));
        }
        super.onActivityResult(reqCode, returnCode, intent);
    }


    @Override
    public void onRefresh() {
        if (tweetTask != null && tweetTask.getStatus() != RUNNING)
            load();
    }


    @Override
    public void onTweetClick(Tweet tweet) {
        if (reload != null && !reload.isRefreshing()) {
            if (tweet.getEmbeddedTweet() != null)
                tweet = tweet.getEmbeddedTweet();
            Intent tweetIntent = new Intent(getContext(), TweetDetail.class);
            tweetIntent.putExtra(KEY_TWEET_ID, tweet.getId());
            tweetIntent.putExtra(KEY_TWEET_NAME, tweet.getUser().getScreenname());
            startActivityForResult(tweetIntent, REQUEST_TWEET_CHANGED);
        }
    }


    @Override
    public void onSettingsChange() {
        list.setAdapter(adapter); // re-initialize List
    }


    @Override
    public void onTabChange() {
        if (list != null)
            list.smoothScrollToPosition(0);
    }


    @Override
    public void onDataClear() {
        if (adapter != null)
            adapter.clear();
        tweetTask = null;
    }

    /**
     * get Id of the first Tweet
     * @return ID of the first tweet or {@link #LIST_EMPTY} if list is empty
     */
    public long getTopId() {
        if (adapter != null && !adapter.isEmpty())
            return adapter.getItemId(0);
        return LIST_EMPTY;
    }

    /**
     * replace all tweets of the list
     *
     * @param tweets list of new tweets
     */
    public void add(List<Tweet> tweets) {
        adapter.add(tweets);
    }

    /**
     * attach new tweets to the top of the list
     *
     * @param tweets list of new tweets
     */
    public void addTop(List<Tweet> tweets) {
        adapter.addFirst(tweets);
    }

    /**
     * called from {@link TweetListLoader} to enable or disable RefreshLayout
     * @param enable true to enable RefreshLayout with delay
     */
    public void setRefresh(boolean enable) {
        if (enable) {
            reload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tweetTask.getStatus() != FINISHED && !reload.isRefreshing())
                        reload.setRefreshing(true);
                }
            }, 500);
        } else {
            reload.setRefreshing(false);
        }
    }

    /**
     * called from {@link TweetListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(EngineException error) {
        if (getContext() != null)
            ErrorHandler.handleFailure(getContext(), error);
    }


    private void load() {
        switch (mode) {
            case TWEET_FRAG_HOME:
                tweetTask = new TweetListLoader(this, TweetListLoader.Action.TL_HOME);
                tweetTask.execute(1);
                break;

            case TWEET_FRAG_MENT:
                tweetTask = new TweetListLoader(this, Action.TL_MENT);
                tweetTask.execute(1);
                break;

            case TWEET_FRAG_TWEETS:
                tweetTask = new TweetListLoader(this, TweetListLoader.Action.USR_TWEETS);
                tweetTask.execute(id, 1);
                break;

            case TWEET_FRAG_FAVORS:
                tweetTask = new TweetListLoader(this, Action.USR_FAVORS);
                tweetTask.execute(id, 1);
                break;

            case TWEET_FRAG_ANSWER:
                boolean loadAnswer = settings.getAnswerLoad();
                if (tweetTask != null || loadAnswer)
                    tweetTask = new TweetListLoader(this, Action.TWEET_ANS);
                else
                    tweetTask = new TweetListLoader(this, Action.DB_ANS);
                tweetTask.execute(id, search);
                break;

            case TWEET_FRAG_SEARCH:
                tweetTask = new TweetListLoader(this, Action.TWEET_SEARCH);
                tweetTask.execute(search);
                break;

            case TWEET_FRAG_LIST:
                tweetTask = new TweetListLoader(this, TweetListLoader.Action.LIST);
                tweetTask.execute(id, 1);
                break;
        }
    }
}