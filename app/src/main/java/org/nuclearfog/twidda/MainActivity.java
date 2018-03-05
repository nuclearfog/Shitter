package org.nuclearfog.twidda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import org.nuclearfog.twidda.database.TrendDatabase;
import org.nuclearfog.twidda.database.TweetDatabase;
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
    private SharedPreferences settings;
    private SearchView searchQuery;
    private Context con;
    private Toolbar toolbar;
    private TabHost tabhost;
    private boolean settingFlag = false;
    private String currentTab = "timeline";
    private int background, font_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
        con = getApplicationContext();
        settings = getSharedPreferences("settings", 0);
        boolean login = settings.getBoolean("login", false);
        if( !login ) {
            Intent i = new Intent(con,LoginPage.class);
            startActivityForResult(i,1);
        } else {
            login();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode,returnCode,i);
        if(returnCode == RESULT_OK) {
            login();
        } else {
            finish();
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
                Intent intent = new Intent(con, SearchPage.class);
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
                bundle.putLong("userID",settings.getLong("userID", -1));
                bundle.putBoolean("home", true);
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
                settingFlag = true;
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(settingFlag) {
            timelineList.setAdapter(null);
            trendList.setAdapter(null);
            mentionList.setAdapter(null);
            setTabContent();
            settingFlag = false;
        }
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
    protected void onUserLeaveHint(){
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
                    TweetDatabase twDB = tlAdp.getData();
                    long tweetID = twDB.getTweetId(position);
                    long userID = twDB.getUserID(position);
                    String username = twDB.getScreenname(position);
                    Intent intent = new Intent(con, TweetDetail.class);
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
                    Intent intent = new Intent(con, SearchPage.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("search", search);
                    if(search.startsWith("#")) {
                        bundle.putString("Addition", search);
                    }
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.m_list:
                if(!mentionReload.isRefreshing()) {
                    TimelineRecycler tlAdp = (TimelineRecycler) mentionList.getAdapter();
                    TweetDatabase twDB = tlAdp.getData();
                    long tweetID = twDB.getTweetId(position);
                    long userID = twDB.getUserID(position);
                    String username = twDB.getScreenname(position);
                    Intent intent = new Intent(con, TweetDetail.class);
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
        // Tab #1
        TabSpec tab1 = tabhost.newTabSpec("timeline");
        tab1.setContent(R.id.timeline);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.home));
        tabhost.addTab(tab1);
        // Tab #2
        TabSpec tab2 = tabhost.newTabSpec("trends");
        tab2.setContent(R.id.trends);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.hash));
        tabhost.addTab(tab2);
        // Tab #3
        TabSpec tab3 = tabhost.newTabSpec("mention");
        tab3.setContent(R.id.mention);
        tab3.setIndicator("",getResources().getDrawable(R.drawable.mention));
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                ColorPreferences mColor = ColorPreferences.getInstance(con);
                background = mColor.getColor(ColorPreferences.BACKGROUND);
                font_color = mColor.getColor(ColorPreferences.FONT_COLOR);

                TimelineRecycler rlRc = (TimelineRecycler) timelineList.getAdapter();
                TrendRecycler  trendRc = (TrendRecycler) trendList.getAdapter();
                TimelineRecycler mentRc = (TimelineRecycler) mentionList.getAdapter();

                if(rlRc == null || rlRc.getItemCount() == 0) {
                    TweetDatabase tweetDeck = new TweetDatabase(con,TweetDatabase.HOME_TL, 0L);
                    rlRc  = new TimelineRecycler(tweetDeck, MainActivity.this);
                } if(mentRc == null || mentRc.getItemCount() == 0) {
                    TweetDatabase mentDeck  = new TweetDatabase(con, TweetDatabase.GET_MENT, 0L);
                    mentRc = new TimelineRecycler(mentDeck, MainActivity.this);
                } if(trendRc == null || trendRc.getItemCount() == 0) {
                    TrendDatabase trendDeck = new TrendDatabase(con);
                    trendRc  = new TrendRecycler(trendDeck, MainActivity.this);
                }
                rlRc.setColor(background,font_color);
                trendRc.setColor(background,font_color);
                mentRc.setColor(background,font_color);
                timelineList.setAdapter(rlRc);
                trendList.setAdapter(trendRc);
                mentionList.setAdapter(mentRc);
            }
        }).run();
    }
}