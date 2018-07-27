package org.nuclearfog.twidda;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TabHost.TabSpec;

import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.MainPage;
import org.nuclearfog.twidda.backend.Registration;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TrendRecycler;
import org.nuclearfog.twidda.window.AppSettings;
import org.nuclearfog.twidda.window.LoginPage;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.TweetPopup;
import org.nuclearfog.twidda.window.UserProfile;

/**
 * MainPage of the App
 * @see Registration Registing App in Twitter
 * @see MainPage show Home Window
 */
public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener,
        TimelineRecycler.OnItemClicked, TrendRecycler.OnItemClicked
{
    private SwipeRefreshLayout timelineReload,trendReload,mentionReload;
    private RecyclerView timelineList, trendList,mentionList;
    private MenuItem profile, tweet, search, setting;
    private SearchView searchQuery;
    private GlobalSettings settings;
    private MainPage home;
    private View lastTab;
    private Toolbar toolbar;
    private TabHost tabhost;
    private int tabIndex = 0;
    private long homeId = 0L;
    private boolean settingChanged = false;
    private final int REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
        TwitterEngine mTwitter = TwitterEngine.getInstance(this);
        settings = GlobalSettings.getInstance(this);
        boolean login = mTwitter.loggedIn();
        if( !login ) {
            Intent i = new Intent(this, LoginPage.class);
            startActivityForResult(i,REQ_CODE);
        } else {
            login();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode,returnCode,i);
        if(reqCode == REQ_CODE) {
            if(returnCode == RESULT_OK) {
                login();
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        toolbar.inflateMenu(R.menu.home);
        profile = m.findItem(R.id.action_profile);
        tweet = m.findItem(R.id.action_tweet);
        search = m.findItem(R.id.action_search);
        setting = m.findItem(R.id.action_settings);
        searchQuery = (SearchView)m.findItem(R.id.action_search).getActionView();
        searchQuery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(getApplicationContext(), SearchPage.class);
                Bundle bundle = new Bundle();
                bundle.putString("search", s);
                intent.putExtras(bundle);
                startActivity(intent);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.action_profile:
                intent = new Intent(this, UserProfile.class);
                Bundle bundle = new Bundle();
                bundle.putLong("userID",homeId);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.action_tweet:
                intent = new Intent(this, TweetPopup.class);
                Bundle b = new Bundle();
                b.putLong("TweetID", -1);
                intent.putExtras(b);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                settingChanged = true;
                intent = new Intent(this, AppSettings.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onPause() {
        if(home != null) {
            home.cancel(true);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(settingChanged) {
            timelineList.setAdapter(null);
            trendList.setAdapter(null);
            mentionList.setAdapter(null);
            setTabContent();
            settingChanged = false;
        }
    }

    /**
     * Home Button
     */
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {
        if( tabIndex == 0) {
            super.onBackPressed();
            overridePendingTransition(0,0);
        }else {
            tabhost.setCurrentTab(0);
        }
    }

    @Override
    public void onRefresh() {
        home = new MainPage(MainActivity.this);
        switch (tabIndex) {
            case 0:
                home.execute(MainPage.HOME,1);
                break;
            case 1:
                home.execute(MainPage.TRND,1);
                break;
            case 2:
                home.execute(MainPage.MENT,1);
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        mentionReload.setRefreshing(false);
        trendReload.setRefreshing(false);
        timelineReload.setRefreshing(false);
        searchQuery.onActionViewCollapsed();
        animate();
        tabIndex = tabhost.getCurrentTab();

        switch(tabId) {
            case "timeline":
                searchQuery.onActionViewCollapsed();
                profile.setVisible(true);
                search.setVisible(false);
                tweet.setVisible(true);
                setting.setVisible(false);
                break;
            case "trends":
                profile.setVisible(false);
                search.setVisible(true);
                tweet.setVisible(false);
                setting.setVisible(true);
                break;
            case "mention":
                searchQuery.onActionViewCollapsed();
                profile.setVisible(false);
                search.setVisible(false);
                tweet.setVisible(false);
                setting.setVisible(true);
                break;
        }
    }

    @Override
    public void onItemClick(View v, ViewGroup parent, int position) {
        switch(parent.getId()) {
            case R.id.tl_list:
                if(!timelineReload.isRefreshing()) {
                    TimelineRecycler tlAdp = (TimelineRecycler) timelineList.getAdapter();
                    Tweet tweet = tlAdp.getData().get(position);
                    long tweetID = tweet.tweetID;
                    long userID = tweet.user.userID;
                    String username = tweet.user.screenname;
                    Intent intent = new Intent(this, TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetID);
                    bundle.putLong("userID",userID);
                    bundle.putString("username",username);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.tr_list:
                if(!trendReload.isRefreshing()) {
                    TrendRecycler trend = (TrendRecycler) trendList.getAdapter();
                    String search = trend.getData().get(position).trend;
                    Intent intent = new Intent(this, SearchPage.class);
                    Bundle bundle = new Bundle();
                    if(search.startsWith("#")) {
                        bundle.putString("Addition", search);
                        bundle.putString("search", search);
                    } else {
                        search = '\"'+ search + '\"';
                        bundle.putString("search", search);
                    }
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.m_list:
                if(!mentionReload.isRefreshing()) {
                    TimelineRecycler tlAdp = (TimelineRecycler) mentionList.getAdapter();
                    Tweet tweet = tlAdp.getData().get(position);
                    long tweetID = tweet.tweetID;
                    long userID = tweet.user.userID;
                    String username = tweet.user.screenname;
                    Intent intent = new Intent(this, TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetID);
                    bundle.putLong("userID",userID);
                    bundle.putString("username",username);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
        }
    }


    /**
     * Login Handle
     */
    private void login() {
        homeId = settings.getUserId();
        timelineList = findViewById(R.id.tl_list);
        trendList = findViewById(R.id.tr_list);
        mentionList = findViewById(R.id.m_list);
        timelineReload = findViewById(R.id.timeline);
        trendReload = findViewById(R.id.trends);
        mentionReload = findViewById(R.id.mention);
        tabhost = findViewById(R.id.main_tabhost);
        toolbar = findViewById(R.id.profile_toolbar);
        timelineList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        trendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mentionList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        timelineList.setHasFixedSize(true);
        trendList.setHasFixedSize(true);
        mentionList.setHasFixedSize(true);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        tabhost.setup();
        TabSpec tab1 = tabhost.newTabSpec("timeline");
        tab1.setContent(R.id.timeline);
        tab1.setIndicator("",ContextCompat.getDrawable(getApplicationContext(),R.drawable.home));
        tabhost.addTab(tab1);
        TabSpec tab2 = tabhost.newTabSpec("trends");
        tab2.setContent(R.id.trends);
        tab2.setIndicator("",ContextCompat.getDrawable(getApplicationContext(),R.drawable.hash));
        tabhost.addTab(tab2);
        TabSpec tab3 = tabhost.newTabSpec("mention");
        tab3.setContent(R.id.mention);
        tab3.setIndicator("",ContextCompat.getDrawable(getApplicationContext(),R.drawable.mention));
        tabhost.addTab(tab3);

        tabhost.setOnTabChangedListener(this);
        timelineReload.setOnRefreshListener(this);
        trendReload.setOnRefreshListener(this);
        mentionReload.setOnRefreshListener(this);
        setTabContent();
    }

    /**
     * Set Tab Content
     */
    private void setTabContent() {
        int background = settings.getBackgroundColor();
        int fontcolor = settings.getFontColor();
        int highlight = settings.getHighlightColor();

        timelineList.setBackgroundColor(background);
        trendList.setBackgroundColor(background);
        mentionList.setBackgroundColor(background);

        TimelineRecycler homeRc = (TimelineRecycler) timelineList.getAdapter();
        TrendRecycler trendRc = (TrendRecycler) trendList.getAdapter();
        TimelineRecycler mentRc = (TimelineRecycler) mentionList.getAdapter();

        if(homeRc == null || homeRc.getItemCount() == 0) {
            new MainPage(this).execute(MainPage.H_LOAD,1);
        } else {
            homeRc.setColor(highlight,fontcolor);
            homeRc.notifyDataSetChanged();
        }
        if(mentRc == null || mentRc.getItemCount() == 0) {
            new MainPage(this).execute(MainPage.M_LOAD,1);
        } else {
            mentRc.setColor(highlight, fontcolor);
            mentRc.notifyDataSetChanged();
        }
        if(trendRc == null || trendRc.getItemCount() == 0) {
            new MainPage(this).execute(MainPage.T_LOAD,1);
        } else {
            trendRc.setColor(fontcolor);
            trendRc.notifyDataSetChanged();
        }
        lastTab = tabhost.getCurrentView();
    }

    private void animate() {
        final int ANIM_DUR = 300;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;

        Animation leftIn = new TranslateAnimation(DIMENS,-1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation rightIn = new TranslateAnimation(DIMENS,1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation leftOut = new TranslateAnimation(DIMENS,0.0f,DIMENS, -1.0f,DIMENS, 0.0f,DIMENS,0.0f);
        Animation rightOut = new TranslateAnimation(DIMENS,0.0f,DIMENS, 1.0f,DIMENS, 0.0f,DIMENS,0.0f);
        leftIn.setDuration(ANIM_DUR);
        rightIn.setDuration(ANIM_DUR);
        leftOut.setDuration(ANIM_DUR);
        rightOut.setDuration(ANIM_DUR);

        View currentTab = tabhost.getCurrentView();

        if( tabhost.getCurrentTab() > tabIndex ) {
            lastTab.setAnimation(leftOut);
            currentTab.setAnimation(rightIn);
        } else {
            lastTab.setAnimation(rightOut);
            currentTab.setAnimation(leftIn);
        }
        lastTab = tabhost.getCurrentView();
    }
}