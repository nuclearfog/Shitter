package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter.OnSettingsChanged;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.TweetLoader;
import org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode;
import org.nuclearfog.twidda.window.TweetDetail;


public class TweetListFragment extends Fragment implements OnRefreshListener, OnItemClickListener, OnSettingsChanged {

    public enum TweetType {
        HOME,
        MENT,
        USER_TWEET,
        USER_FAVOR,
        TWEET_ANSR,
        SEARCH,
    }

    private GlobalSettings settings;
    private TweetLoader tweetTask;
    private SwipeRefreshLayout reload;
    private TweetAdapter adapter;
    private View root;

    private TweetType mode;
    private String search;
    private long id;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        super.onCreateView(inflater, parent, param);
        boolean fixSize;
        Bundle b = getArguments();
        if (b != null && b.containsKey("mode")) {
            mode = (TweetType) b.getSerializable("mode");
            id = b.getLong("id", -1);
            search = b.getString("search", "");
            fixSize = b.getBoolean("fix", false);
        } else {
            throw new AssertionError();
        }

        View v = inflater.inflate(R.layout.fragment_list, parent, false);

        reload = v.findViewById(R.id.fragment_reload);
        reload.setOnRefreshListener(this);
        adapter = new TweetAdapter(this);

        settings = GlobalSettings.getInstance(getContext());
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        adapter.setColor(settings.getHighlightColor(), settings.getFontColor());
        adapter.toggleImage(settings.getImageLoad());

        RecyclerView list = v.findViewById(R.id.fragment_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(fixSize);
        list.setAdapter(adapter);

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
            switch (mode) {
                case HOME:
                    tweetTask = new TweetLoader(root, Mode.DB_HOME);
                    tweetTask.execute();
                    break;
                case MENT:
                    tweetTask = new TweetLoader(root, Mode.DB_MENT);
                    tweetTask.execute();
                    break;
                case USER_TWEET:
                    tweetTask = new TweetLoader(root, Mode.DB_TWEETS);
                    tweetTask.execute(id);
                    break;
                case USER_FAVOR:
                    tweetTask = new TweetLoader(root, Mode.DB_FAVORS);
                    tweetTask.execute(id);
                    break;
                case TWEET_ANSR:
                    tweetTask = new TweetLoader(root, Mode.DB_ANS);
                    tweetTask.execute(id, search);
                    break;
                case SEARCH:
                    tweetTask = new TweetLoader(root, Mode.TWEET_SEARCH);
                    tweetTask.execute(search);
                    break;
                default:
                    if (BuildConfig.DEBUG)
                        throw new AssertionError("mode failure");
                    break;
            }
        }
    }


    @Override
    public void onStop() {
        if (tweetTask != null && tweetTask.getStatus() == Status.RUNNING)
            tweetTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
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
                tweetTask = new TweetLoader(root, Mode.TWEET_ANS);
                tweetTask.execute(id, search);
                break;
            case SEARCH:
                tweetTask = new TweetLoader(root, Mode.TWEET_SEARCH);
                tweetTask.execute(search);
                break;
            default:
                if (BuildConfig.DEBUG)
                    throw new AssertionError("mode failure");
                break;
        }
    }


    @Override
    public void onItemClick(int pos) {
        if (!reload.isRefreshing()) {
            Tweet tweet = adapter.getData(pos);
            if (tweet.getEmbeddedTweet() != null)
                tweet = tweet.getEmbeddedTweet();
            Intent intent = new Intent(getContext(), TweetDetail.class);
            intent.putExtra("tweetID", tweet.getId());
            intent.putExtra("username", tweet.getUser().getScreenname());
            startActivity(intent);
        }
    }


    @Override
    public void settingsChanged() {
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        adapter.setColor(settings.getHighlightColor(), settings.getFontColor());
        adapter.toggleImage(settings.getImageLoad());
        adapter.notifyDataSetChanged();
    }
}