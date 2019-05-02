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

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TweetAdapter;
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
    private TweetAdapter searchAdapter;
    private UserAdapter userAdapter;
    private SwipeRefreshLayout tweetReload;
    private GlobalSettings settings;
    private TwitterSearch searchAsync;
    private View lastView, twUnderline, usUnderline;
    private TabHost tabhost;
    private String search = "";
    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_search);

        Toolbar tool = findViewById(R.id.search_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            if (BuildConfig.DEBUG && param.size() != 1)
                throw new AssertionError();
            search = param.getString("search");
        }

        View root = findViewById(R.id.search_layout);
        tweetSearch = findViewById(R.id.tweet_result);
        userSearch = findViewById(R.id.user_result);
        tweetReload = findViewById(R.id.searchtweets);
        tabhost = findViewById(R.id.search_tab);

        settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());

        LayoutInflater inflater = LayoutInflater.from(this);
        View twIndicator = inflater.inflate(R.layout.tab_ts, null);
        View usIndicator = inflater.inflate(R.layout.tab_us, null);
        twUnderline = twIndicator.findViewById(R.id.ts_divider);
        usUnderline = usIndicator.findViewById(R.id.us_divider);
        tweetReload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());

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
        setIndicator();

        tweetSearch.setLayoutManager(new LinearLayoutManager(this));
        userSearch.setLayoutManager(new LinearLayoutManager(this));

        tabhost.setOnTabChangedListener(this);
        tweetReload.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (searchAsync == null) {
            searchAdapter = new TweetAdapter(this);
            searchAdapter.setColor(settings.getHighlightColor(), settings.getFontColor());
            searchAdapter.toggleImage(settings.getImageLoad());
            tweetSearch.setAdapter(searchAdapter);

            userAdapter = new UserAdapter(this);
            userAdapter.toggleImage(settings.getImageLoad());
            userAdapter.setColor(settings.getFontColor());
            userSearch.setAdapter(userAdapter);

            searchAsync = new TwitterSearch(this);
            searchAsync.execute(search);
        }
    }


    @Override
    protected void onStop() {
        if (searchAsync != null && searchAsync.getStatus() == RUNNING)
            searchAsync.cancel(true);
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
            if (search.startsWith("#"))
                intent.putExtra("Addition", search);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(RecyclerView rv, int position) {
        if (!tweetReload.isRefreshing()) {
            switch (rv.getId()) {
                case R.id.tweet_result:
                    Tweet tweet = searchAdapter.getData(position);
                    Intent tweetdetail = new Intent(this, TweetDetail.class);
                    tweetdetail.putExtra("tweetID", tweet.getId());
                    tweetdetail.putExtra("username", tweet.getUser().getScreenname());
                    startActivity(tweetdetail);
                    break;

                case R.id.user_result:
                    TwitterUser user = userAdapter.getData(position);
                    Intent userprofile = new Intent(this, UserProfile.class);
                    userprofile.putExtra("userID", user.getId());
                    userprofile.putExtra("username", user.getScreenname());
                    startActivity(userprofile);
                    break;
            }
        }
    }


    @Override
    public void onRefresh() {
        if (searchAsync != null && searchAsync.getStatus() == RUNNING)
            searchAsync.cancel(true);
        searchAsync = new TwitterSearch(this);
        searchAsync.execute(search);
    }


    @Override
    public void onTabChanged(String tabId) {
        animate();
        tabIndex = tabhost.getCurrentTab();
        setIndicator();
    }


    private void setIndicator() {
        switch (tabIndex) {
            case 0:
                twUnderline.setBackgroundColor(settings.getHighlightColor());
                usUnderline.setBackgroundColor(0);
                break;

            case 1:
                usUnderline.setBackgroundColor(settings.getHighlightColor());
                twUnderline.setBackgroundColor(0);
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