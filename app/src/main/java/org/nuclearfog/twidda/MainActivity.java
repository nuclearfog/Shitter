package org.nuclearfog.twidda;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.database.TrendDatabase;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.backend.Registration;
import org.nuclearfog.twidda.backend.MainPage;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TrendRecycler;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.window.LoginPage;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.UserProfile;
import org.nuclearfog.twidda.window.AppSettings;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.TweetPopup;

import java.util.List;

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
    private Toolbar toolbar;
    private TabHost tabhost;
    private String currentTab = "timeline";
    private int background, font_color, highlight;
    private long homeId = 0L;
    private final int REQCODE = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
        TwitterEngine mTwitter = TwitterEngine.getInstance(this);
        boolean login = mTwitter.loggedIn();
        if( !login ) {
            Intent i = new Intent(this, LoginPage.class);
            startActivityForResult(i,REQCODE);
        } else {
            login();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode,returnCode,i);
        if(reqCode == REQCODE) {
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
                intent = new Intent(this, AppSettings.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        setTabContent();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
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
        if( currentTab.equals("timeline") ) {
            super.onBackPressed();
        }else {
            tabhost.setCurrentTab(0);
        }
    }

    @Override
    public void onRefresh() {
        MainPage homeView = new MainPage(MainActivity.this);
        switch (currentTab) {
            case "timeline":
                homeView.execute(MainPage.HOME,1);
                break;
            case "trends":
                homeView.execute(MainPage.TRND,1);
                break;
            case "mention":
                homeView.execute(MainPage.MENT,1);
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        mentionReload.setRefreshing(false);
        trendReload.setRefreshing(false);
        timelineReload.setRefreshing(false);
        searchQuery.onActionViewCollapsed();
        currentTab = tabId;
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
                    String search = trend.getData().getTrendname(position);
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
        homeId = TwitterEngine.getHomeId();
        timelineList = (RecyclerView) findViewById(R.id.tl_list);
        trendList = (RecyclerView) findViewById(R.id.tr_list);
        mentionList = (RecyclerView) findViewById(R.id.m_list);
        timelineReload = (SwipeRefreshLayout) findViewById(R.id.timeline);
        trendReload = (SwipeRefreshLayout) findViewById(R.id.trends);
        mentionReload = (SwipeRefreshLayout) findViewById(R.id.mention);
        tabhost = (TabHost)findViewById(R.id.main_tabhost);
        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
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
    }

    /**
     * Set Tab Content
     */
    private void setTabContent() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ColorPreferences mColor = ColorPreferences.getInstance(getApplicationContext());
                background = mColor.getColor(ColorPreferences.BACKGROUND);
                font_color = mColor.getColor(ColorPreferences.FONT_COLOR);
                highlight  = mColor.getColor(ColorPreferences.HIGHLIGHTING);
                boolean imageload = mColor.loadImage();

                timelineList.setBackgroundColor(background);
                trendList.setBackgroundColor(background);
                mentionList.setBackgroundColor(background);

                TimelineRecycler tlRc = (TimelineRecycler) timelineList.getAdapter();
                TrendRecycler  trendRc = (TrendRecycler) trendList.getAdapter();
                TimelineRecycler mentRc = (TimelineRecycler) mentionList.getAdapter();

                if(tlRc == null || tlRc.getItemCount() == 0) {
                    DatabaseAdapter tweetDeck = new DatabaseAdapter(getApplicationContext());
                    List<Tweet> tweets = tweetDeck.load(DatabaseAdapter.HOME, -1L);
                    tlRc = new TimelineRecycler(tweets, MainActivity.this);
                    timelineList.setAdapter(tlRc);
                }
                if(mentRc == null || mentRc.getItemCount() == 0) {
                    DatabaseAdapter mentDeck  = new DatabaseAdapter(getApplicationContext());
                    List<Tweet> tweets = mentDeck.load(DatabaseAdapter.MENT,-1L);
                    mentRc = new TimelineRecycler(tweets, MainActivity.this);
                    mentionList.setAdapter(mentRc);
                }
                if(trendRc == null || trendRc.getItemCount() == 0) {
                    TrendDatabase trendDeck = new TrendDatabase(getApplicationContext());
                    trendRc  = new TrendRecycler(trendDeck, MainActivity.this);
                    trendList.setAdapter(trendRc);
                }

                tlRc.setColor(highlight,font_color);
                tlRc.toggleImage(imageload);
                tlRc.notifyDataSetChanged();
                trendRc.setColor(font_color);
                trendRc.notifyDataSetChanged();
                mentRc.setColor(highlight,font_color);
                mentRc.toggleImage(imageload);
                mentRc.notifyDataSetChanged();
            }
        }).run();
    }
}