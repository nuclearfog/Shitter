package org.nuclearfog.twidda.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.TweetDetail;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.adapter.TweetAdapter.TweetClickListener;
import org.nuclearfog.twidda.backend.TweetListLoader;
import org.nuclearfog.twidda.backend.TweetListLoader.Mode;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_NAME;


public class TweetFragment extends Fragment implements OnRefreshListener, TweetClickListener, FragmentChangeObserver {

    public static final String KEY_FRAG_TWEET_MODE = "tweet_mode";
    public static final String KEY_FRAG_TWEET_SEARCH = "tweet_search";
    public static final String KEY_FRAG_TWEET_ID = "tweet_id";
    public static final String KEY_FRAG_TWEET_FIX_LAYOUT = "tweet_fix_layout";

    public static final int TWEET_FRAG_HOME = 0;
    public static final int TWEET_FRAG_MENT = 1;
    public static final int TWEET_FRAG_TWEETS = 2;
    public static final int TWEET_FRAG_FAVORS = 3;
    public static final int TWEET_FRAG_ANSWER = 4;
    public static final int TWEET_FRAG_SEARCH = 5;
    public static final int TWEET_FRAG_LIST = 6;

    private static final int REQUEST_TWEET_CHANGED = 3;
    public static final int RETURN_TWEET_CHANGED = 4;

    private TweetListLoader tweetTask;
    private GlobalSettings settings;
    private SwipeRefreshLayout reload;
    private RecyclerView list;
    private TweetAdapter adapter;

    private String search;
    private long id, tweetId;
    private int mode;

    private boolean notifyChange;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        boolean fixSize = false;
        Bundle b = getArguments();
        Context context = inflater.getContext();

        if (b != null) {
            mode = b.getInt(KEY_FRAG_TWEET_MODE);
            id = b.getLong(KEY_FRAG_TWEET_ID, -1);
            search = b.getString(KEY_FRAG_TWEET_SEARCH, "");
            fixSize = b.getBoolean(KEY_FRAG_TWEET_FIX_LAYOUT, false);
        }
        settings = GlobalSettings.getInstance(context);
        adapter = new TweetAdapter(this, settings);
        list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setHasFixedSize(fixSize);
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
        if (notifyChange) {
            list.setAdapter(adapter); // re-initialize List
            notifyChange = false;
        }
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
    }


    @Override
    public void onDestroy() {
        if (tweetTask != null && tweetTask.getStatus() == RUNNING)
            tweetTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, Intent i) {
        if (reqCode == REQUEST_TWEET_CHANGED && returnCode == RETURN_TWEET_CHANGED)
            adapter.remove(tweetId);
        super.onActivityResult(reqCode, returnCode, i);
    }


    @Override
    public void onRefresh() {
        if (tweetTask != null && tweetTask.getStatus() != RUNNING)
            load();
    }


    @Override
    public void onTweetClick(Tweet tweet) {
        if (reload != null && !reload.isRefreshing()) {
            tweetId = tweet.getId(); // Mark tweet
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
        notifyChange = true;
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
     *
     * @return ID of the first tweet or zero if list is empty
     */
    public long getTopId() {
        if (adapter != null && !adapter.isEmpty())
            return adapter.getItemId(0);
        return 0;
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
     * @param err Twitter exception
     */
    public void onError(EngineException err) {
        if (err.isErrorDefined()) {
            if (err.isRateLimitExceeded()) {
                String errorMsg = getString(R.string.error_limit_exceeded);
                errorMsg += err.getRetryAfter();
                Toast.makeText(getContext(), errorMsg, LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), err.getMessageResource(), LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), err.getMessage(), LENGTH_SHORT).show();
        }
    }


    private void load() {
        switch (mode) {
            case TWEET_FRAG_HOME:
                tweetTask = new TweetListLoader(this, Mode.TL_HOME);
                tweetTask.execute(1);
                break;

            case TWEET_FRAG_MENT:
                tweetTask = new TweetListLoader(this, Mode.TL_MENT);
                tweetTask.execute(1);
                break;

            case TWEET_FRAG_TWEETS:
                tweetTask = new TweetListLoader(this, Mode.USR_TWEETS);
                tweetTask.execute(id, 1);
                break;

            case TWEET_FRAG_FAVORS:
                tweetTask = new TweetListLoader(this, Mode.USR_FAVORS);
                tweetTask.execute(id, 1);
                break;

            case TWEET_FRAG_ANSWER:
                boolean loadAnswer = settings.getAnswerLoad();
                if (tweetTask != null || loadAnswer)
                    tweetTask = new TweetListLoader(this, Mode.TWEET_ANS);
                else
                    tweetTask = new TweetListLoader(this, Mode.DB_ANS);
                tweetTask.execute(id, search);
                break;

            case TWEET_FRAG_SEARCH:
                tweetTask = new TweetListLoader(this, Mode.TWEET_SEARCH);
                tweetTask.execute(search);
                break;

            case TWEET_FRAG_LIST:
                tweetTask = new TweetListLoader(this, Mode.LIST);
                tweetTask.execute(id, 1);
                break;
        }
    }
}