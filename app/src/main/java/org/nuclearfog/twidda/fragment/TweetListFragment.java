package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.fragment.backend.TweetLoader;
import org.nuclearfog.twidda.window.TweetDetail;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.DB_ANS;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.DB_FAVORS;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.DB_HOME;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.DB_MENT;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.DB_TWEETS;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.TL_HOME;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.TL_MENT;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.TWEET_ANS;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.TWEET_SEARCH;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.USR_FAVORS;
import static org.nuclearfog.twidda.fragment.backend.TweetLoader.Mode.USR_TWEETS;


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
    private ViewGroup root;

    private long id;
    private int mode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        super.onCreateView(inflater,parent,param);
        reload = new SwipeRefreshLayout(inflater.getContext());
        RecyclerView list = new RecyclerView(inflater.getContext());
        adapter = new TweetAdapter(this);
        list.setAdapter(adapter);
        reload.setOnRefreshListener(this);
        reload.addView(list);
        return reload;
    }


    @Override
    public void onViewCreated(@NonNull View v, Bundle param) {
        Bundle b = getArguments();
        if(b != null) {
            mode = b.getInt("mode");
            id = b.getInt("id");
        }
       root = (ViewGroup) v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(tweetTask == null) {
            switch(mode) {
                case HOME:
                    tweetTask = new TweetLoader(root, DB_HOME);
                    tweetTask.execute();
                    break;

                case MENT:
                    tweetTask = new TweetLoader(root, DB_MENT);
                    tweetTask.execute();
                    break;

                case USER_TWEET:
                    tweetTask = new TweetLoader(root, DB_TWEETS);
                    tweetTask.execute();
                    break;

                case USER_FAVOR:
                    tweetTask = new TweetLoader(root, DB_FAVORS);
                    tweetTask.execute(id);
                    break;

                case TWEET_ANSR:
                    tweetTask = new TweetLoader(root, DB_ANS);
                    tweetTask.execute(id);
                    break;

                case SEARCH:
                    tweetTask = new TweetLoader(root, TWEET_SEARCH);
                    tweetTask.execute(id);
                    break;
            }
        }
    }


    @Override
    public void onStop() {
        if(tweetTask != null && tweetTask.getStatus() == RUNNING)
            tweetTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        switch(mode) {
            case HOME:
                tweetTask = new TweetLoader(root, TL_HOME);
                tweetTask.execute();
                break;

            case MENT:
                tweetTask = new TweetLoader(root, TL_MENT);
                tweetTask.execute();
                break;

            case USER_TWEET:
                tweetTask = new TweetLoader(root, USR_TWEETS);
                tweetTask.execute();
                break;

            case USER_FAVOR:
                tweetTask = new TweetLoader(root, USR_FAVORS);
                tweetTask.execute();
                break;

            case TWEET_ANSR:
                tweetTask = new TweetLoader(root, TWEET_ANS);
                tweetTask.execute();
                break;

            case SEARCH:
                tweetTask = new TweetLoader(root, TWEET_SEARCH);
                tweetTask.execute();
                break;
        }
    }


    @Override
    public void onItemClick(RecyclerView rv, int pos) {
        if (!reload.isRefreshing()) {
            Tweet tweet = adapter.getData(pos);
            if (tweet.getEmbeddedTweet() != null)
                tweet = tweet.getEmbeddedTweet();
            Intent intent = new Intent(getContext(), TweetDetail.class);//,
            intent.putExtra("tweetID", tweet.getId());
            intent.putExtra("username", tweet.getUser().getScreenname());
            startActivity(intent);
        }
    }
}