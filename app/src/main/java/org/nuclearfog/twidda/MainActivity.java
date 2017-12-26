package org.nuclearfog.twidda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;
import android.content.SharedPreferences;

import org.nuclearfog.twidda.DataBase.TrendDatabase;
import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.Engine.RegisterAccount;
import org.nuclearfog.twidda.Engine.TwitterEngine;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.ViewAdapter.TrendsAdapter;
import org.nuclearfog.twidda.Window.Profile;
import org.nuclearfog.twidda.Window.Settings;
import org.nuclearfog.twidda.Window.TweetWindow;

public class MainActivity extends AppCompatActivity
{
    private SwipeRefreshLayout timelineReload,trendReload,mentionReload;
    private ListView timelineList, trendList,mentionList;
    private SharedPreferences settings;
    private EditText pin;
    private Context con;
    private Toolbar toolbar;
    private MenuItem profile;
    private MenuItem search;
    private MenuItem tweet;

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
        profile = toolbar.getMenu().findItem(R.id.action_profile);
        search = toolbar.getMenu().findItem(R.id.action_search);
        tweet = toolbar.getMenu().findItem(R.id.action_tweet);
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
                intent = new Intent(this, Profile.class);
                Bundle bundle = new Bundle();
                bundle.putLong("userID",settings.getLong("userID", -1));
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.action_tweet:
                intent = new Intent(this, TweetWindow.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
        }
        return true;
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
        tab1.setIndicator("Timeline");
        tabhost.addTab(tab1);
        // Tab #2
        TabSpec tab2 = tabhost.newTabSpec("trends");
        tab2.setContent(R.id.trends);
        tab2.setIndicator("Trend");
        tabhost.addTab(tab2);
        // Tab #3
        TabSpec tab3 = tabhost.newTabSpec("mention");
        tab3.setContent(R.id.mention);
        tab3.setIndicator("Mention");
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
        setTabContent();
    }

    /**
     * Set DB Content
     * separate THREAD
     */
    private void setTabContent() {
        new Thread() {
            @Override
            public void run() {
                TweetDatabase tweetDeck = new TweetDatabase(con,TweetDatabase.HOME_TL, 0L);
                TimelineAdapter tlAdapt = new TimelineAdapter (MainActivity.this,R.layout.tweet,tweetDeck);
                timelineList.setAdapter(tlAdapt);
                TrendDatabase trendDeck = new TrendDatabase(con);
                TrendsAdapter trendAdp = new TrendsAdapter(con,R.layout.tweet,trendDeck);
                trendList.setAdapter(trendAdp);
            }
        }.run();
    }

    /**
     * Swipe To Refresh Listener
     */
    private void setRefreshListener() {
        timelineReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TwitterEngine homeView = new TwitterEngine(MainActivity.this);
                homeView.execute(0);
            }
        });
        trendReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TwitterEngine homeView = new TwitterEngine(MainActivity.this);
                homeView.execute(1);
            }
        });
        mentionReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TwitterEngine homeView = new TwitterEngine(MainActivity.this);
                homeView.execute(2);
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
                break;
            case "trends":
                profile.setVisible(false);
                search.setVisible(true);
                tweet.setVisible(false);
                break;
            case "mention":
                profile.setVisible(false);
                search.setVisible(false);
                tweet.setVisible(false);
                break;
        }
    }
}