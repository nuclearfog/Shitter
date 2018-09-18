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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.TwitterSearch;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter.OnItemClicked;
import org.nuclearfog.twidda.viewadapter.UserAdapter;

import static android.os.AsyncTask.Status.RUNNING;

/**
 * Search Page
 *
 * @see TwitterSearch
 */
public class SearchPage extends AppCompatActivity implements UserAdapter.OnItemClicked,
        OnRefreshListener, OnTabChangeListener, OnItemClicked {

    private RecyclerView tweetSearch, userSearch;
    private SwipeRefreshLayout tweetReload;
    private TwitterSearch mSearch;
    private TabHost tabhost;
    private View lastView;
    private String search = "";
    private int tabIndex = 0;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_search);

        b = getIntent().getExtras();
        if (b != null)
            search = b.getString("search");

        GlobalSettings settings = GlobalSettings.getInstance(this);

        View root = findViewById(R.id.search_layout);
        tweetSearch = findViewById(R.id.tweet_result);
        userSearch = findViewById(R.id.user_result);
        tweetReload = findViewById(R.id.searchtweets);
        tabhost = findViewById(R.id.search_tab);
        Toolbar tool = findViewById(R.id.search_toolbar);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        tabhost.setup();
        TabHost.TabSpec tab1 = tabhost.newTabSpec("search_result");
        tab1.setContent(R.id.searchtweets);
        tab1.setIndicator("", getDrawable(R.drawable.search));
        tabhost.addTab(tab1);

        TabHost.TabSpec tab2 = tabhost.newTabSpec("user_result");
        tab2.setContent(R.id.user_result);
        tab2.setIndicator("", getDrawable(R.drawable.user));
        tabhost.addTab(tab2);
        lastView = tabhost.getCurrentView();

        root.setBackgroundColor(settings.getBackgroundColor());

        tweetSearch.setLayoutManager(new LinearLayoutManager(this));
        userSearch.setLayoutManager(new LinearLayoutManager(this));

        tabhost.setOnTabChangedListener(this);
        tweetReload.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mSearch == null) {
            mSearch = new TwitterSearch(this);
            tweetReload.setRefreshing(true);
            mSearch.execute(search);
        }
    }


    @Override
    protected void onStop() {
        if (mSearch != null && mSearch.getStatus() == RUNNING) {
            mSearch.cancel(true);
            tweetReload.setRefreshing(false);
        }
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
        if (item.getItemId() == R.id.search_tweet && mSearch.getStatus() != RUNNING) {
            Intent intent = new Intent(this, TweetPopup.class);
            intent.putExtra("TweetID", -1);
            if (search.startsWith("#"))
                intent.putExtra("Addition", search);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(ViewGroup parent, int position) {
        if (!tweetReload.isRefreshing())
            if (parent.getId() == R.id.tweet_result) {
                TimelineAdapter tweetAdapter = (TimelineAdapter) tweetSearch.getAdapter();
                if (tweetAdapter != null) {
                    Tweet tweet = tweetAdapter.getData().get(position);
                    Intent intent = new Intent(this, TweetDetail.class);
                    intent.putExtra("tweetID", tweet.tweetID);
                    intent.putExtra("userID", tweet.user.userID);
                    intent.putExtra("username", tweet.user.screenname);
                    startActivity(intent);
                }
            } else {
                UserAdapter userAdapter = (UserAdapter) userSearch.getAdapter();
                if (userAdapter != null) {
                    TwitterUser user = userAdapter.getData().get(position);
                    Intent intent = new Intent(this, UserProfile.class);
                    intent.putExtra("userID", user.userID);
                    intent.putExtra("username", user.screenname);
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
    }


    private void animate() {
        final int ANIM_DUR = 300;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;

        Animation leftIn = new TranslateAnimation(DIMENS, -1.0f, DIMENS, 0.0f, DIMENS, 0.0f, DIMENS, 0.0f);
        Animation rightIn = new TranslateAnimation(DIMENS, 1.0f, DIMENS, 0.0f, DIMENS, 0.0f, DIMENS, 0.0f);
        Animation leftOut = new TranslateAnimation(DIMENS, 0.0f, DIMENS, -1.0f, DIMENS, 0.0f, DIMENS, 0.0f);
        Animation rightOut = new TranslateAnimation(DIMENS, 0.0f, DIMENS, 1.0f, DIMENS, 0.0f, DIMENS, 0.0f);
        leftIn.setDuration(ANIM_DUR);
        rightIn.setDuration(ANIM_DUR);
        leftOut.setDuration(ANIM_DUR);
        rightOut.setDuration(ANIM_DUR);

        View currentView = tabhost.getCurrentView();

        if (tabhost.getCurrentTab() > tabIndex) {
            lastView.setAnimation(leftOut);
            currentView.setAnimation(rightIn);
        } else {
            lastView.setAnimation(rightOut);
            currentView.setAnimation(leftIn);
        }
        lastView = tabhost.getCurrentView();
    }
}