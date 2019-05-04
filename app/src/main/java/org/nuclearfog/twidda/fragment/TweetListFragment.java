package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask.Status;
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
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.TweetLoader;
import org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode;
import org.nuclearfog.twidda.window.TweetDetail;


public class TweetListFragment extends Fragment implements OnRefreshListener, OnItemClickListener {

    public static final int HOME = 0;
    public static final int MENT = 1;
    public static final int USER_TWEET = 2;
    public static final int USER_FAVOR = 3;
    public static final int TWEET_ANSR = 4;
    public static final int SEARCH = 5;

    private TweetLoader tweetTask;
    private SwipeRefreshLayout reload;
    private TweetAdapter adapter;
    private View root;

    private int mode;
    private String search;
    private long id;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        super.onCreateView(inflater, parent, param);
        boolean fixSize;
        Bundle b = getArguments();
        if(b != null && b.containsKey("mode")) {
            mode = b.getInt("mode");
            id = b.getLong("id", -1);
            search = b.getString("search", "");
            fixSize = b.getBoolean("fix", false);
        } else {
            throw new AssertionError("Bundle error!");
        }

        View v = inflater.inflate(R.layout.fragment_list, parent, false);
        GlobalSettings settings = GlobalSettings.getInstance(getContext());

        reload = v.findViewById(R.id.fragment_reload);
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reload.setOnRefreshListener(this);

        adapter = new TweetAdapter(this);
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
        if(tweetTask == null) {
            switch(mode) {
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
                    tweetTask.execute(id);
                    break;
                case SEARCH:
                    tweetTask = new TweetLoader(root, Mode.TWEET_SEARCH);
                    tweetTask.execute(search);
                    break;
                default:
                    if(BuildConfig.DEBUG)
                        throw new AssertionError("mode failure");
                    break;
            }
        }
    }


    @Override
    public void onStop() {
        if(tweetTask != null && tweetTask.getStatus() == Status.RUNNING)
            tweetTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        switch(mode) {
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
                tweetTask.execute(id);
                break;
            case SEARCH:
                tweetTask = new TweetLoader(root, Mode.TWEET_SEARCH);
                tweetTask.execute(search);
                break;
            default:
                if(BuildConfig.DEBUG)
                    throw new AssertionError("mode failure");
                break;
        }
    }


    @Override
    public void onItemClick(RecyclerView rv, int pos) {
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
}