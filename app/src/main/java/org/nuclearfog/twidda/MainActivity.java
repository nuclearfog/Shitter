package org.nuclearfog.twidda;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.TabHost.TabSpec;

import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.backend.LinkBrowser;
import org.nuclearfog.twidda.backend.MainPage;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.AppSettings;
import org.nuclearfog.twidda.window.LoginPage;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.TweetPopup;
import org.nuclearfog.twidda.window.UserProfile;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.backend.MainPage.Mode.DATA;
import static org.nuclearfog.twidda.backend.MainPage.Mode.HOME;
import static org.nuclearfog.twidda.backend.MainPage.Mode.MENT;
import static org.nuclearfog.twidda.backend.MainPage.Mode.TRND;
import static org.nuclearfog.twidda.window.TweetDetail.TWEET_REMOVED;

/**
 * Main Activity
 *
 * @see MainPage
 */
public class MainActivity extends AppCompatActivity implements OnRefreshListener,
        OnTabChangeListener, OnItemClickListener {

    private static final int LOGIN = 1;
    private static final int SETTING = 2;
    private static final int TWEET = 3;

    private SwipeRefreshLayout timelineReload, trendReload, mentionReload;
    private RecyclerView timelineList, trendList, mentionList;
    private TimelineAdapter timelineAdapter, mentionAdapter;
    private View tlUnderline, trUnderline, mnUnderline;
    private View lastTab, root;
    private TrendAdapter trendsAdapter;
    private GlobalSettings settings;
    private MainPage home;
    private LinkBrowser mBrowser;
    private TabHost tabhost;
    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_main);

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        timelineList = findViewById(R.id.tl_list);
        trendList = findViewById(R.id.tr_list);
        mentionList = findViewById(R.id.m_list);
        timelineReload = findViewById(R.id.timeline);
        trendReload = findViewById(R.id.trends);
        mentionReload = findViewById(R.id.mention);
        tabhost = findViewById(R.id.main_tabhost);
        root = findViewById(R.id.main_layout);

        LayoutInflater inflater = LayoutInflater.from(this);
        View tlIndicator = inflater.inflate(R.layout.tab_tl, null);
        View trIndicator = inflater.inflate(R.layout.tab_tr, null);
        View mnIndicator = inflater.inflate(R.layout.tab_mn, null);
        tlUnderline = tlIndicator.findViewById(R.id.tl_divider);
        trUnderline = trIndicator.findViewById(R.id.tr_divider);
        mnUnderline = mnIndicator.findViewById(R.id.mn_divider);

        tabhost.setup();
        TabSpec tab1 = tabhost.newTabSpec("timeline");
        tab1.setContent(R.id.timeline);
        tab1.setIndicator(tlIndicator);
        tabhost.addTab(tab1);
        TabSpec tab2 = tabhost.newTabSpec("trends");
        tab2.setContent(R.id.trends);
        tab2.setIndicator(trIndicator);
        tabhost.addTab(tab2);
        TabSpec tab3 = tabhost.newTabSpec("mention");
        tab3.setContent(R.id.mention);
        tab3.setIndicator(mnIndicator);
        tabhost.addTab(tab3);

        timelineList.setLayoutManager(new LinearLayoutManager(this));
        trendList.setLayoutManager(new LinearLayoutManager(this));
        mentionList.setLayoutManager(new LinearLayoutManager(this));
        timelineList.setHasFixedSize(true);
        trendList.setHasFixedSize(true);
        mentionList.setHasFixedSize(true);

        lastTab = tabhost.getCurrentView();
        tabhost.setOnTabChangedListener(this);
        timelineReload.setOnRefreshListener(this);
        trendReload.setOnRefreshListener(this);
        mentionReload.setOnRefreshListener(this);
        settings = GlobalSettings.getInstance(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!settings.getLogin()) {
            Intent i = new Intent(this, LoginPage.class);
            startActivityForResult(i, LOGIN);
        } else if (home == null) {
            timelineAdapter = new TimelineAdapter(this);
            trendsAdapter = new TrendAdapter(this);
            mentionAdapter = new TimelineAdapter(this);

            root.setBackgroundColor(settings.getBackgroundColor());
            timelineAdapter.setColor(settings.getHighlightColor(), settings.getFontColor());
            timelineAdapter.toggleImage(settings.getImageLoad());
            trendsAdapter.setColor(settings.getFontColor());
            mentionAdapter.setColor(settings.getHighlightColor(), settings.getFontColor());
            mentionAdapter.toggleImage(settings.getImageLoad());

            timelineReload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
            trendReload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
            mentionReload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());

            timelineList.setAdapter(timelineAdapter);
            trendList.setAdapter(trendsAdapter);
            mentionList.setAdapter(mentionAdapter);

            home = new MainPage(this, DATA);
            home.execute(1);

            setIndicator();

            Uri link = getIntent().getData();
            if (link != null) {
                mBrowser = new LinkBrowser(this);
                mBrowser.execute(link);
            }
        }
    }


    @Override
    protected void onStop() {
        if (home != null && home.getStatus() == RUNNING) {
            home.cancel(true);
        }
        if (mBrowser != null && mBrowser.getStatus() == RUNNING) {
            mBrowser.cancel(true);
        }
        super.onStop();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        switch (reqCode) {
            case LOGIN:
                if (returnCode == RESULT_CANCELED) {
                    overridePendingTransition(0, 0);
                    finish();
                }
                break;

            case TWEET:
                if (returnCode == TWEET_REMOVED)
                    home = null;
                break;
        }
        super.onActivityResult(reqCode, returnCode, i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.home, m);
        MenuItem search = m.findItem(R.id.action_search);
        SearchView searchQuery = (SearchView) search.getActionView();
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent search = new Intent(MainActivity.this, SearchPage.class);
                search.putExtra("search", s);
                startActivity(search);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        MenuItem profile = m.findItem(R.id.action_profile);
        MenuItem tweet = m.findItem(R.id.action_tweet);
        MenuItem search = m.findItem(R.id.action_search);
        MenuItem setting = m.findItem(R.id.action_settings);
        SearchView searchQuery = (SearchView) search.getActionView();

        switch (tabIndex) {
            case 0:
                profile.setVisible(true);
                search.setVisible(false);
                tweet.setVisible(true);
                setting.setVisible(false);
                search.collapseActionView();
                break;

            case 1:
                profile.setVisible(false);
                search.setVisible(true);
                tweet.setVisible(false);
                setting.setVisible(true);
                break;

            case 2:
                searchQuery.onActionViewCollapsed();
                profile.setVisible(false);
                search.setVisible(false);
                tweet.setVisible(false);
                setting.setVisible(true);
                search.collapseActionView();
                break;
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                long homeId = settings.getUserId();
                Intent user = new Intent(this, UserProfile.class);
                user.putExtra("userID", homeId);
                user.putExtra("username", "");
                startActivity(user);
                break;

            case R.id.action_tweet:
                Intent tweet = new Intent(this, TweetPopup.class);
                startActivityForResult(tweet, TWEET);
                break;

            case R.id.action_settings:
                if (home != null && home.getStatus() == RUNNING)
                    home.cancel(true);
                home = null;
                Intent settings = new Intent(this, AppSettings.class);
                startActivityForResult(settings, SETTING);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tabIndex == 0) {
            super.onBackPressed();
        } else {
            tabhost.setCurrentTab(0);
        }
    }


    @Override
    public void onRefresh() {
        if (home != null && home.getStatus() == RUNNING)
            home.cancel(true);

        switch (tabIndex) {
            default:
            case 0:
                home = new MainPage(this, HOME);
                break;
            case 1:
                home = new MainPage(this, TRND);
                break;
            case 2:
                home = new MainPage(this, MENT);
                break;
        }
        home.execute(1);
    }


    @Override
    public void onTabChanged(String tabId) {
        animate();
        tabIndex = tabhost.getCurrentTab();
        invalidateOptionsMenu();
        setIndicator();
    }


    @Override
    public void onItemClick(RecyclerView parent, int position) {
        switch (parent.getId()) {
            case R.id.tl_list:
                if (!timelineReload.isRefreshing()) {
                    Tweet tweet = timelineAdapter.getData(position);
                    if (tweet.getEmbeddedTweet() != null)
                        tweet = tweet.getEmbeddedTweet();
                    openTweet(tweet.getId(), tweet.getUser().getId(), tweet.getUser().getScreenname());
                }
                break;

            case R.id.tr_list:
                if (!trendReload.isRefreshing()) {
                    String search = trendsAdapter.getData(position).getName();
                    Intent intent = new Intent(this, SearchPage.class);
                    if (!search.startsWith("#"))
                        search = '\"' + search + '\"';
                    intent.putExtra("search", search);
                    startActivity(intent);
                }
                break;

            case R.id.m_list:
                if (!mentionReload.isRefreshing()) {
                    Tweet tweet = mentionAdapter.getData(position);
                    if (tweet.getEmbeddedTweet() != null)
                        tweet = tweet.getEmbeddedTweet();
                    openTweet(tweet.getId(), tweet.getUser().getId(), tweet.getUser().getScreenname());
                }
                break;
        }
    }


    private void setIndicator() {
        switch (tabIndex) {
            case 0:
                tlUnderline.setBackgroundColor(settings.getHighlightColor());
                trUnderline.setBackgroundColor(0);
                mnUnderline.setBackgroundColor(0);
                break;

            case 1:
                trUnderline.setBackgroundColor(settings.getHighlightColor());
                tlUnderline.setBackgroundColor(0);
                mnUnderline.setBackgroundColor(0);
                break;

            case 2:
                mnUnderline.setBackgroundColor(settings.getHighlightColor());
                tlUnderline.setBackgroundColor(0);
                trUnderline.setBackgroundColor(0);
                break;
        }
    }


    private void openTweet(long tweetId, long userId, String username) {
        Intent intent = new Intent(this, TweetDetail.class);
        intent.putExtra("tweetID", tweetId);
        intent.putExtra("userID", userId);
        intent.putExtra("username", username);
        startActivityForResult(intent, TWEET);
    }


    private void animate() {
        final int ANIM_DUR = 300;
        final float LEFT = -1.0f;
        final float RIGHT = 1.0f;
        final float NULL = 0.0f;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;

        View currentTab = tabhost.getCurrentView();
        if (tabhost.getCurrentTab() > tabIndex) {
            Animation lOut = new TranslateAnimation(DIMENS, NULL, DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL);
            Animation rIn = new TranslateAnimation(DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
            lOut.setDuration(ANIM_DUR);
            rIn.setDuration(ANIM_DUR);
            lastTab.setAnimation(lOut);
            currentTab.setAnimation(rIn);
        } else {
            Animation lIn = new TranslateAnimation(DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
            Animation rOut = new TranslateAnimation(DIMENS, NULL, DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL);
            lIn.setDuration(ANIM_DUR);
            rOut.setDuration(ANIM_DUR);
            lastTab.setAnimation(rOut);
            currentTab.setAnimation(lIn);
        }
        lastTab = tabhost.getCurrentView();
    }
}