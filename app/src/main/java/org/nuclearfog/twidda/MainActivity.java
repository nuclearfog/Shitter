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

public class MainActivity extends AppCompatActivity
{
    private SwipeRefreshLayout timelineReload,trendReload,mentionReload;
    private ListView timelineList, trendList,mentionList;
    private MenuItem profile, tweet, search, setting;
    private SharedPreferences settings;
    private EditText pin;
    private Context con;
    private Toolbar toolbar;

    /**
     * Create Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        con = getApplicationContext();
        settings = con.getSharedPreferences("settings", 0);
        if( !(settings.getBoolean("login", false)) ) {
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
        SearchView searchQuery = (SearchView)m.findItem(R.id.action_search).getActionView();

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
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.action_tweet:
                intent = new Intent(this, TweetPopup.class);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setRefreshListener();
        setTabListener();
        setTabContent();
        setListViewListener();
    }

    /**
     * Set Tab Listener
     * @see #setTabContent()
     */
    private void setTabListener() {
        TabHost tabhost = (TabHost)findViewById(R.id.main_tabhost);
        tabhost.setup();
        // Tab #1
        TabSpec tab1 = tabhost.newTabSpec("timeline");
        tab1.setContent(R.id.timeline);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.timeline_icon));
        tabhost.addTab(tab1);
        // Tab #2
        TabSpec tab2 = tabhost.newTabSpec("trends");
        tab2.setContent(R.id.trends);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.trends_icon));
        tabhost.addTab(tab2);
        // Tab #3
        TabSpec tab3 = tabhost.newTabSpec("mention");
        tab3.setContent(R.id.mention);
        tab3.setIndicator("",getResources().getDrawable(R.drawable.mention_icon));
        tabhost.addTab(tab3);
        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mentionReload.setRefreshing(false);
                trendReload.setRefreshing(false);
                timelineReload.setRefreshing(false);
                setVisibility(tabId);
            }
        });
    }

    /**
     * Set DB Content
     * separate THREAD
     */
    private void setTabContent() {
        TweetDatabase tweetDeck = new TweetDatabase(con,TweetDatabase.HOME_TL, 0L);
        TrendDatabase trendDeck = new TrendDatabase(con);
        TimelineAdapter tlAdapt = new TimelineAdapter(con,tweetDeck);
        TrendAdapter trendAdp = new TrendAdapter(con,trendDeck);
        timelineList.setAdapter(tlAdapt);
        trendList.setAdapter(trendAdp);
    }

    /**
     * Swipe To Refresh Listener
     */
    private void setRefreshListener() {
        timelineReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainPage homeView = new MainPage(MainActivity.this);
                homeView.execute(0);
            }
        });
        trendReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainPage homeView = new MainPage(MainActivity.this);
                homeView.execute(1);
            }
        });
        mentionReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainPage homeView = new MainPage(MainActivity.this);
                homeView.execute(2);
            }
        });
    }

    /**
     * Set On Item Click Listener for the main Listviews
     */
    private void setListViewListener() {
        timelineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });
        trendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrendAdapter trend = (TrendAdapter) trendList.getAdapter();
                String search = trend.getDatabase().getTrendname(position);
                Intent intent = new Intent(con, TwitterSearch.class);
                Bundle bundle = new Bundle();
                bundle.putString("search", search);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        mentionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!mentionReload.isRefreshing()) {
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
            }
        });
    }

    /**
     * Toolbar Items
     * @param currentTab 3 Tabs "timeline" , "trends" , "mention"
     */
    private void setVisibility(String currentTab) {
        switch(currentTab) {
            case "timeline":
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
                profile.setVisible(false);
                search.setVisible(false);
                tweet.setVisible(false);
                setting.setVisible(true);
                break;
        }
    }
}