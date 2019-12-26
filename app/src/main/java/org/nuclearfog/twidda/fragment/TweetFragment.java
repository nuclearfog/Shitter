package org.nuclearfog.twidda.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.activity.TweetDetail;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.TweetLoader;
import org.nuclearfog.twidda.backend.TweetLoader.Mode;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_NAME;


public class TweetFragment extends Fragment implements OnRefreshListener, OnItemClickListener, FragmentChangeObserver {

    public static final String KEY_FRAG_TWEET_MODE = "tweet_mode";
    public static final String KEY_FRAG_TWEET_SEARCH = "tweet_search";
    public static final String KEY_FRAG_TWEET_ID = "tweet_id";
    public static final String KEY_FRAG_TWEET_FIX_LAYOUT = "tweet_fix_layout";

    private static final int REQUEST_TWEET_CHANGED = 3;
    public static final int RETURN_TWEET_CHANGED = 4;

    public enum TweetType {
        HOME,
        MENT,
        USER_TWEET,
        USER_FAVOR,
        TWEET_ANSR,
        SEARCH,
        LIST
    }

    private TweetLoader tweetTask;
    private GlobalSettings settings;
    private SwipeRefreshLayout reload;
    private RecyclerView list;
    private TweetAdapter adapter;

    private TweetType mode;
    private String search;
    private long id, tweetId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        boolean fixSize = false;
        Bundle b = getArguments();
        Context context = inflater.getContext();

        if (b != null) {
            mode = (TweetType) b.getSerializable(KEY_FRAG_TWEET_MODE);
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
    public void onItemClick(int pos) {
        if (reload != null && !reload.isRefreshing()) {
            Tweet tweet = adapter.get(pos);
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
        if (adapter != null)
            adapter.notifyDataSetChanged();
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


    public TweetAdapter getAdapter() {
        return adapter;
    }


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


    private void load() {
        switch (mode) {
            case HOME:
                tweetTask = new TweetLoader(this, Mode.TL_HOME);
                tweetTask.execute(1);
                break;

            case MENT:
                tweetTask = new TweetLoader(this, Mode.TL_MENT);
                tweetTask.execute(1);
                break;

            case USER_TWEET:
                tweetTask = new TweetLoader(this, Mode.USR_TWEETS);
                tweetTask.execute(id, 1);
                break;

            case USER_FAVOR:
                tweetTask = new TweetLoader(this, Mode.USR_FAVORS);
                tweetTask.execute(id, 1);
                break;

            case TWEET_ANSR:
                boolean loadAnswer = settings.getAnswerLoad();
                if (tweetTask != null || loadAnswer)
                    tweetTask = new TweetLoader(this, Mode.TWEET_ANS);
                else
                    tweetTask = new TweetLoader(this, Mode.DB_ANS);
                tweetTask.execute(id, search);
                break;

            case SEARCH:
                tweetTask = new TweetLoader(this, Mode.TWEET_SEARCH);
                tweetTask.execute(search);
                break;

            case LIST:
                tweetTask = new TweetLoader(this, Mode.LIST);
                tweetTask.execute(id, 1);
                break;
        }
    }
}