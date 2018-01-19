package org.nuclearfog.twidda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import org.nuclearfog.twidda.database.TrendDatabase;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.backend.RegisterAccount;
import org.nuclearfog.twidda.backend.MainPage;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TrendAdapter;
import org.nuclearfog.twidda.window.UserProfile;
import org.nuclearfog.twidda.window.AppSettings;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.window.TweetPopup;
import org.nuclearfog.twidda.window.TwitterSearch;

/**
 * MainPage of the App
 * @see RegisterAccount Registing App in Twitter
 * @see MainPage show Home Window
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener
{
    private SwipeRefreshLayout timelineReload,trendReload,mentionReload;
    private ListView timelineList, trendList,mentionList;
    private MenuItem profile, tweet, search, setting;
    private SharedPreferences settings;
    private SearchView searchQuery;
    private EditText pin;
    private Context con;
    private Toolbar toolbar;
    private boolean settingFlag = false;
    private String currentTab = "timeline";

    /**
     * Create Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        con = getApplicationContext();
        settings = con.getSharedPreferences("settings", 0);
        boolean login = settings.getBoolean("login", false);
        if( !login ) {
            setContentView(R.layout.login);
            pin = (EditText) findViewById(R.id.pin);
            Button linkButton  = (Button) findViewById(R.id.linkButton);
            Button verifierButton = (Button) findViewById(R.id.verifier);
            Button loginButton = (Button) findViewById(R.id.login);
            linkButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View arg0){linkTwitter();}});
            verifierButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View arg0){verifier();}});
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View arg0){login();}});
        } else { login(); }
    }

    /**
     * Create Actionbar
     */
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
                Intent intent = new Intent(con, TwitterSearch.class);
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

    /**
     * Actionbar selection
     */
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
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.action_tweet:
                intent = new Intent(this, TweetPopup.class);
                Bundle b = new Bundle();
                b.putLong("TweetID", -1);
                intent.putExtras(b);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(settingFlag) {
            setTabContent();
            settingFlag = false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.tl_list:
                if(!timelineReload.isRefreshing()) {
                    TimelineAdapter tlAdp = (TimelineAdapter) timelineList.getAdapter();
                    TweetDatabase twDB = tlAdp.getAdapter();
                    long tweetID = twDB.getTweetId(position);
                    long userID = twDB.getUserID(position);
                    Intent intent = new Intent(con, TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetID);
                    bundle.putLong("userID",userID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.tr_list:
                if(!trendReload.isRefreshing()) {
                    TrendAdapter trend = (TrendAdapter) trendList.getAdapter();
                    String search = trend.getDatabase().getTrendname(position);
                    Intent intent = new Intent(con, TwitterSearch.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("search", search);
                    if(search.startsWith("#")) {
                        bundle.putString("Hashtag", search);
                    }
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.m_list:
                if(!mentionReload.isRefreshing()) {
                    TimelineAdapter tlAdp = (TimelineAdapter) mentionList.getAdapter();
                    TweetDatabase twDB = tlAdp.getAdapter();
                    long tweetID = twDB.getTweetId(position);
                    long userID = twDB.getUserID(position);
                    Intent intent = new Intent(con, TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetID);
                    bundle.putLong("userID",userID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        MainPage homeView = new MainPage(MainActivity.this);
        switch (currentTab) {
            case "timeline":
                homeView.execute(0,1);
                break;
            case "trends":
                homeView.execute(1,1);
                break;
            case "mention":
                homeView.execute(2,1);
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

    /**
     * Load Preferences
     */
    private void linkTwitter() {
        RegisterAccount account = new RegisterAccount(this);
        account.execute("");
    }

    /**
     * Check Twitter PIN
     */
    private void verifier() {
        String twitterPin = pin.getText().toString();
        if(!twitterPin.trim().isEmpty()) {
            RegisterAccount account = new RegisterAccount(this);
            account.execute(twitterPin);
        } else {
            Toast.makeText(con,"PIN eingeben!",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Login Handle
     */
    private void login() {
        setContentView(R.layout.mainpage);
        timelineList = (ListView) findViewById(R.id.tl_list);
        trendList = (ListView) findViewById(R.id.tr_list);
        mentionList = (ListView) findViewById(R.id.m_list);
        timelineReload = (SwipeRefreshLayout) findViewById(R.id.timeline);
        trendReload = (SwipeRefreshLayout) findViewById(R.id.trends);
        mentionReload = (SwipeRefreshLayout) findViewById(R.id.mention);
        TabHost tabhost = (TabHost)findViewById(R.id.main_tabhost);
        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        timelineList.setOnItemClickListener(this);
        trendList.setOnItemClickListener(this);
        mentionList.setOnItemClickListener(this);
        timelineReload.setOnRefreshListener(this);
        trendReload.setOnRefreshListener(this);
        mentionReload.setOnRefreshListener(this);

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
        setTabContent();
    }

    /**
     * Set DB Content
     */
    private void setTabContent() {
        TweetDatabase tweetDeck = new TweetDatabase(con,TweetDatabase.HOME_TL, 0L);
        TrendDatabase trendDeck = new TrendDatabase(con);
        TweetDatabase mentDeck  = new TweetDatabase(con, TweetDatabase.GET_MENT, 0L);
        TimelineAdapter tlAdapt = new TimelineAdapter(this,tweetDeck);
        TrendAdapter trendAdp = new TrendAdapter(this,trendDeck);
        TimelineAdapter ment  = new TimelineAdapter(this, mentDeck);
        timelineList.setAdapter(tlAdapt);
        trendList.setAdapter(trendAdp);
        mentionList.setAdapter(ment);
    }
}