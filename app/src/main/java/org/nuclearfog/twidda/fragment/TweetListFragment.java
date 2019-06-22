package org.nuclearfog.twidda.fragment;

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

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.TweetLoader;
import org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode;
import org.nuclearfog.twidda.window.TweetDetail;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.window.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.window.TweetDetail.KEY_TWEET_NAME;


public class TweetListFragment extends Fragment implements OnRefreshListener, OnItemClickListener, FragmentChangeObserver {

    public static final String KEY_FRAG_TWEET_MODE = "mode";
    public static final String KEY_FRAG_TWEET_SEARCH = "search";
    public static final String KEY_FRAG_TWEET_ID = "ID";
    public static final String KEY_FRAG_TWEET_FIX = "fix";

    public static final int REQUEST_TWEET_CHANGED = 3;
    public static final int RETURN_TWEET_CHANGED = 4;

    public enum TweetType {
        HOME,
        MENT,
        USER_TWEET,
        USER_FAVOR,
        TWEET_ANSR,
        SEARCH,
    }

    private TweetLoader tweetTask;
    private GlobalSettings settings;
    private SwipeRefreshLayout reload;
    private RecyclerView list;
    private TweetAdapter adapter;
    private View root;

    private TweetType mode;
    private String search;
    private long id, tweetId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        super.onCreateView(inflater, parent, param);
        boolean fixSize;
        Bundle b = getArguments();
        if (b != null && b.containsKey(KEY_FRAG_TWEET_MODE)) {
            mode = (TweetType) b.getSerializable(KEY_FRAG_TWEET_MODE);
            id = b.getLong(KEY_FRAG_TWEET_ID, -1);
            search = b.getString(KEY_FRAG_TWEET_SEARCH, "");
            fixSize = b.getBoolean(KEY_FRAG_TWEET_FIX, false);
        } else {
            throw new AssertionError();
        }

        View v = inflater.inflate(R.layout.fragment_list, parent, false);
        reload = v.findViewById(R.id.fragment_reload);
        list = v.findViewById(R.id.fragment_list);

        settings = GlobalSettings.getInstance(getContext());
        reload.setOnRefreshListener(this);
        adapter = new TweetAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(fixSize);
        list.setAdapter(adapter);

        onSettingsChange();
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View v, Bundle param) {
        root = v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (tweetTask == null) {
            load();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (tweetTask == null) {
            load();
        }
    }


    @Override
    public void onStop() {
        if (tweetTask != null && tweetTask.getStatus() == RUNNING)
            tweetTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, Intent i) {
        if (reqCode == REQUEST_TWEET_CHANGED && returnCode == RETURN_TWEET_CHANGED)
            adapter.removeItem(tweetId);
        super.onActivityResult(reqCode, returnCode, i);
    }


    @Override
    public void onRefresh() {
        load();
    }


    @Override
    public void onItemClick(int pos) {
        if (reload != null && !reload.isRefreshing()) {
            Tweet tweet = adapter.getData(pos);
            tweetId = tweet.getId();
            if (tweet.getEmbeddedTweet() != null)
                tweet = tweet.getEmbeddedTweet();
            Intent tweetIntent = new Intent(getContext(), TweetDetail.class);
            tweetIntent.putExtra(KEY_TWEET_ID, tweetId);
            tweetIntent.putExtra(KEY_TWEET_NAME, tweet.getUser().getScreenname());
            startActivityForResult(tweetIntent, REQUEST_TWEET_CHANGED);
        }
    }


    @Override
    public void onSettingsChange() {
        if (reload != null)
            reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        adapter.setColor(settings.getHighlightColor(), settings.getFontColor());
        adapter.toggleImage(settings.getImageLoad());
        adapter.clear();
        tweetTask = null;
    }


    @Override
    public void onTabChange() {
        if (list != null)
            list.smoothScrollToPosition(0);
    }


    @Override
    public void onDataClear() {
        adapter.clear();
        tweetTask = null;
    }


    private void load() {
        switch (mode) {
            case HOME:
                tweetTask = new TweetLoader(root, Mode.TL_HOME);
                tweetTask.execute();
                break;

            case MENT:
                tweetTask = new TweetLoader(root, Mode.TL_MENT);
                tweetTask.execute();
                break;

            case USER_TWEET:
                tweetTask = new TweetLoader(root, Mode.USR_TWEETS);
                tweetTask.execute(id);
                break;

            case USER_FAVOR:
                tweetTask = new TweetLoader(root, Mode.USR_FAVORS);
                tweetTask.execute(id);
                break;

            case TWEET_ANSR:
                boolean loadAnswer = settings.getAnswerLoad();
                if (tweetTask != null || loadAnswer)
                    tweetTask = new TweetLoader(root, Mode.TWEET_ANS);
                else
                    tweetTask = new TweetLoader(root, Mode.DB_ANS);
                tweetTask.execute(id, search);
                break;

            case SEARCH:
                tweetTask = new TweetLoader(root, Mode.TWEET_SEARCH);
                tweetTask.execute(search);
                break;
        }
    }
}