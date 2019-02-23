package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.TwitterSearch;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;

/**
 * Search Page
 *
 * @see TwitterSearch
 */
public class SearchPage extends AppCompatActivity implements OnRefreshListener,
        OnTabChangeListener, OnItemClickListener {

    private RecyclerView tweetSearch, userSearch;
    private SwipeRefreshLayout tweetReload;
    private GlobalSettings settings;
    private TwitterSearch mSearch;
    private View twIndicator, usIndicator;
    private TabHost tabhost;
    private View lastView;
    private String search = "";
    private int tabIndex = 0;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_search);

        Bundle param = getIntent().getExtras();
        if (param != null)
            search = param.getString("search");

        View root = findViewById(R.id.search_layout);
        tweetSearch = findViewById(R.id.tweet_result);
        userSearch = findViewById(R.id.user_result);
        tweetReload = findViewById(R.id.searchtweets);
        tabhost = findViewById(R.id.search_tab);
        Toolbar tool = findViewById(R.id.search_toolbar);

        settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        twIndicator = inflater.inflate(R.layout.tab_ts, null);
        usIndicator = inflater.inflate(R.layout.tab_us, null);

        tabhost.setup();
        TabHost.TabSpec tab1 = tabhost.newTabSpec("search_result");
        tab1.setContent(R.id.searchtweets);
        tab1.setIndicator(twIndicator);
        tabhost.addTab(tab1);

        TabHost.TabSpec tab2 = tabhost.newTabSpec("user_result");
        tab2.setContent(R.id.user_result);
        tab2.setIndicator(usIndicator);
        tabhost.addTab(tab2);
        lastView = tabhost.getCurrentView();

        twIndicator.findViewById(R.id.ts_divider).setBackgroundColor(settings.getHighlightColor());

        tweetSearch.setLayoutManager(new LinearLayoutManager(this));
        userSearch.setLayoutManager(new LinearLayoutManager(this));

        tabhost.setOnTabChangedListener(this);
        tweetReload.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mSearch == null) {
            TimelineAdapter searchAdapter = new TimelineAdapter(this);
            searchAdapter.setColor(settings.getHighlightColor(), settings.getFontColor());
            searchAdapter.toggleImage(settings.getImageLoad());
            tweetSearch.setAdapter(searchAdapter);

            UserAdapter userAdapter = new UserAdapter(this);
            userAdapter.toggleImage(settings.getImageLoad());
            userAdapter.setColor(settings.getFontColor());
            userSearch.setAdapter(userAdapter);

            mSearch = new TwitterSearch(this);
            tweetReload.setRefreshing(true);
            mSearch.execute(search);
        }
    }


    @Override
    protected void onStop() {
        if (mSearch != null && mSearch.getStatus() == RUNNING)
            mSearch.cancel(true);
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        if (tabIndex == 1) {
            tabhost.setCurrentTab(0);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.search, m);
        MenuItem mSearch = m.findItem(R.id.new_search);
        SearchView searchQuery = (SearchView) mSearch.getActionView();
        searchQuery.setQueryHint(search);
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(SearchPage.this, SearchPage.class);
                intent.putExtra("search", s);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search_tweet) {
            Intent intent = new Intent(this, TweetPopup.class);
            intent.putExtra("TweetID", -1);
            if (search.startsWith("#"))
                intent.putExtra("Addition", search);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(RecyclerView rv, int position) {
        if (!tweetReload.isRefreshing())
            if (rv.getId() == R.id.tweet_result) {
                TimelineAdapter tweetAdapter = (TimelineAdapter) tweetSearch.getAdapter();
                if (tweetAdapter != null) {
                    Tweet tweet = tweetAdapter.getData().get(position);
                    Intent intent = new Intent(this, TweetDetail.class);
                    intent.putExtra("tweetID", tweet.getId());
                    intent.putExtra("userID", tweet.getUser().getId());
                    intent.putExtra("username", tweet.getUser().getScreenname());
                    startActivity(intent);
                }
            } else {
                UserAdapter userAdapter = (UserAdapter) userSearch.getAdapter();
                if (userAdapter != null) {
                    TwitterUser user = userAdapter.getData().get(position);
                    Intent intent = new Intent(this, UserProfile.class);
                    intent.putExtra("userID", user.getId());
                    intent.putExtra("username", user.getScreenname());
                    startActivity(intent);
                }
            }
    }


    @Override
    public void onRefresh() {
        if (mSearch != null && mSearch.getStatus() != RUNNING) {
            mSearch = new TwitterSearch(this);
            mSearch.execute(search);
        }
    }


    @Override
    public void onTabChanged(String tabId) {
        animate();
        tabIndex = tabhost.getCurrentTab();
        switch (tabIndex) {
            case 0:
                twIndicator.findViewById(R.id.ts_divider).setBackgroundColor(settings.getHighlightColor());
                usIndicator.findViewById(R.id.us_divider).setBackgroundColor(0);
                break;

            case 1:
                usIndicator.findViewById(R.id.us_divider).setBackgroundColor(settings.getHighlightColor());
                twIndicator.findViewById(R.id.ts_divider).setBackgroundResource(0);
                break;
        }
    }


    private void animate() {
        final int ANIM_DUR = 300;
        final float LEFT = -1.0f;
        final float RIGHT = 1.0f;
        final float NULL = 0.0f;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;

        View currentView = tabhost.getCurrentView();
        if (tabhost.getCurrentTab() > tabIndex) {
            Animation leftOut = new TranslateAnimation(DIMENS, NULL, DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL);
            Animation rightIn = new TranslateAnimation(DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
            leftOut.setDuration(ANIM_DUR);
            rightIn.setDuration(ANIM_DUR);
            lastView.setAnimation(leftOut);
            currentView.setAnimation(rightIn);
        } else {
            Animation leftIn = new TranslateAnimation(DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
            Animation rightOut = new TranslateAnimation(DIMENS, NULL, DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL);
            leftIn.setDuration(ANIM_DUR);
            rightOut.setDuration(ANIM_DUR);
            lastView.setAnimation(rightOut);
            currentView.setAnimation(leftIn);
        }
        lastView = tabhost.getCurrentView();
    }
}