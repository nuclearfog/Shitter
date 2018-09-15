package org.nuclearfog.twidda;

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
import android.widget.TabHost.TabSpec;

import org.nuclearfog.twidda.backend.MainPage;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TrendAdapter;
import org.nuclearfog.twidda.window.AppSettings;
import org.nuclearfog.twidda.window.LoginPage;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.TweetPopup;
import org.nuclearfog.twidda.window.UserProfile;

import static android.os.AsyncTask.Status.RUNNING;

/**
 * Main Activity
 *
 * @see MainPage
 */
public class MainActivity extends AppCompatActivity implements OnRefreshListener, OnTabChangeListener,
        TimelineAdapter.OnItemClicked, TrendAdapter.OnItemClicked {

    private SwipeRefreshLayout timelineReload, trendReload, mentionReload;
    private RecyclerView timelineList, trendList, mentionList;
    private GlobalSettings settings;
    private MainPage home;
    private View lastTab;
    private TabHost tabhost;
    private int tabIndex = 0;
    private boolean settingChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        settings = GlobalSettings.getInstance(this);

        if (!settings.getLogin()) {
            Intent i = new Intent(this, LoginPage.class);
            startActivityForResult(i, 1);
        }

        timelineList = findViewById(R.id.tl_list);
        trendList = findViewById(R.id.tr_list);
        mentionList = findViewById(R.id.m_list);
        timelineReload = findViewById(R.id.timeline);
        trendReload = findViewById(R.id.trends);
        mentionReload = findViewById(R.id.mention);
        tabhost = findViewById(R.id.main_tabhost);
        Toolbar toolbar = findViewById(R.id.profile_toolbar);

        timelineList.setLayoutManager(new LinearLayoutManager(this));
        timelineList.setHasFixedSize(true);

        trendList.setLayoutManager(new LinearLayoutManager(this));
        trendList.setHasFixedSize(true);

        mentionList.setLayoutManager(new LinearLayoutManager(this));
        mentionList.setHasFixedSize(true);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        tabhost.setup();
        TabSpec tab1 = tabhost.newTabSpec("timeline");
        tab1.setContent(R.id.timeline);
        tab1.setIndicator("", getDrawable(R.drawable.home));
        tabhost.addTab(tab1);
        TabSpec tab2 = tabhost.newTabSpec("trends");
        tab2.setContent(R.id.trends);
        tab2.setIndicator("", getDrawable(R.drawable.hash));
        tabhost.addTab(tab2);
        TabSpec tab3 = tabhost.newTabSpec("mention");
        tab3.setContent(R.id.mention);
        tab3.setIndicator("", getDrawable(R.drawable.mention));
        tabhost.addTab(tab3);

        lastTab = tabhost.getCurrentView();
        tabhost.setOnTabChangedListener(this);
        timelineReload.setOnRefreshListener(this);
        trendReload.setOnRefreshListener(this);
        mentionReload.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (settingChanged) {
            timelineList.setAdapter(null);
            trendList.setAdapter(null);
            mentionList.setAdapter(null);
        }
        if (home == null || settingChanged) {
            settingChanged = false;
            home = new MainPage(this);
            home.execute(MainPage.DATA, 1);
        }
    }


    @Override
    protected void onStop() {
        if (home != null && home.getStatus() == RUNNING) {
            home.cancel(true);
            timelineReload.setRefreshing(false);
            trendReload.setRefreshing(false);
            mentionReload.setRefreshing(false);
        }
        super.onStop();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode, returnCode, i);
        if (returnCode == RESULT_CANCELED) {
            overridePendingTransition(0, 0);
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.home, m);
        MenuItem search = m.findItem(R.id.action_search);
        SearchView searchQuery = (SearchView) search.getActionView();
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(MainActivity.this, SearchPage.class);
                intent.putExtra("search", s);
                startActivity(intent);
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_profile:
                long homeId = settings.getUserId();
                intent = new Intent(this, UserProfile.class);
                intent.putExtra("userID", homeId);
                intent.putExtra("username", "");
                startActivity(intent);
                break;

            case R.id.action_tweet:
                intent = new Intent(this, TweetPopup.class);
                startActivity(intent);
                break;

            case R.id.action_settings:
                settingChanged = true;
                intent = new Intent(this, AppSettings.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tabIndex == 0) {
            super.onBackPressed();
            overridePendingTransition(0, 0);
        } else {
            tabhost.setCurrentTab(0);
        }
    }


    @Override
    public void onRefresh() {
        if (home != null && home.getStatus() == RUNNING)
            home.cancel(true);
        home = new MainPage(MainActivity.this);
        if (tabIndex == 0)
            home.execute(MainPage.HOME, 1);
        else if (tabIndex == 1)
            home.execute(MainPage.TRND, 1);
        else if (tabIndex == 2)
            home.execute(MainPage.MENT, 1);
    }


    @Override
    public void onTabChanged(String tabId) {
        animate();
        tabIndex = tabhost.getCurrentTab();
        invalidateOptionsMenu();
    }


    @Override
    public void onItemClick(ViewGroup parent, int position) {
        switch (parent.getId()) {
            case R.id.tl_list:
                if (!timelineReload.isRefreshing()) {
                    TimelineAdapter timelineAdapter = (TimelineAdapter) timelineList.getAdapter();
                    if (timelineAdapter != null) {
                        Tweet tweet = timelineAdapter.getData().get(position);
                        if (tweet.embedded != null) {
                            tweet = tweet.embedded;
                        }
                        Intent intent = new Intent(this, TweetDetail.class);
                        intent.putExtra("tweetID", tweet.tweetID);
                        intent.putExtra("userID", tweet.user.userID);
                        intent.putExtra("username", tweet.user.screenname);
                        startActivity(intent);
                    }
                }
                break;

            case R.id.tr_list:
                if (!trendReload.isRefreshing()) {
                    TrendAdapter trendAdapter = (TrendAdapter) trendList.getAdapter();
                    if (trendAdapter != null) {
                        String search = trendAdapter.getData().get(position).trend;
                        Intent intent = new Intent(this, SearchPage.class);
                        if (!search.startsWith("#")) {
                            search = '\"' + search + '\"';
                        }
                        intent.putExtra("search", search);
                        startActivity(intent);
                    }
                }
                break;

            case R.id.m_list:
                if (!mentionReload.isRefreshing()) {
                    TimelineAdapter mentionAdapter = (TimelineAdapter) mentionList.getAdapter();
                    if (mentionAdapter != null) {
                        Tweet tweet = mentionAdapter.getData().get(position);
                        if (tweet.embedded != null) {
                            tweet = tweet.embedded;
                        }
                        Intent intent = new Intent(this, TweetDetail.class);
                        intent.putExtra("tweetID", tweet.tweetID);
                        intent.putExtra("userID", tweet.user.userID);
                        intent.putExtra("username", tweet.user.screenname);
                        startActivity(intent);
                    }
                }
                break;
        }
    }


    private void animate() {
        final int ANIM_DUR = 300;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;
        final float LEFT = -1.0f;
        final float RIGHT = 1.0f;
        final float NULL = 0.0f;
        Animation lIn = new TranslateAnimation(DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
        Animation rIn = new TranslateAnimation(DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
        Animation lOut = new TranslateAnimation(DIMENS, NULL, DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL);
        Animation rOut = new TranslateAnimation(DIMENS, NULL, DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL);
        lIn.setDuration(ANIM_DUR);
        rIn.setDuration(ANIM_DUR);
        lOut.setDuration(ANIM_DUR);
        rOut.setDuration(ANIM_DUR);

        View currentTab = tabhost.getCurrentView();
        if (tabhost.getCurrentTab() > tabIndex) {
            lastTab.setAnimation(lOut);
            currentTab.setAnimation(rIn);
        } else {
            lastTab.setAnimation(rOut);
            currentTab.setAnimation(lIn);
        }
        lastTab = tabhost.getCurrentView();
    }
}