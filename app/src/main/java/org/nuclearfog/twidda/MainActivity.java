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

import org.nuclearfog.twidda.engine.TrendDatabase;
import org.nuclearfog.twidda.engine.TweetDatabase;
import org.nuclearfog.twidda.engine.TwitterEngine;
import org.nuclearfog.twidda.engine.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.engine.ViewAdapter.TrendsAdapter;
import org.nuclearfog.twidda.newwindow.Profile;

public class MainActivity extends AppCompatActivity
{
    private Button linkButton, verifierButton, loginButton;
    private EditText pin;
    private Context con;
    private SharedPreferences einstellungen;
    private TabHost tabhost;
    private SwipeRefreshLayout refresh;
    private ListView list;
    private String currentTab = "timeline";

    /**
     * Create Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        einstellungen = getApplicationContext().getSharedPreferences("settings", 0);
        con = getApplicationContext();
        if( !loggedIn() ) {
            setContentView(R.layout.activity_login);
            pin = (EditText) findViewById(R.id.pin);
            linkButton  = (Button) findViewById(R.id.linkButton);
            verifierButton = (Button) findViewById(R.id.verifier);
            loginButton = (Button) findViewById(R.id.loginButton);
            linkButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View arg0){linkTwitter();}});
            verifierButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View arg0){verifier();}});
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View arg0){login();}});
        } else {
            login();
        }
    }

    /**
     * Create Actionbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.buttons, m);
        return true;
    }

    /**
     * Actionbar selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_profile:
                Intent i = new Intent(this, Profile.class);
                Bundle bundle = new Bundle();
                bundle.putString("username","home");
                i.putExtras(bundle);
                startActivity(i);
                break;
            case R.id.action_tweet:
                break;
        }
        return true;
    }

    /**
     * Load Preferences
     */
    private void linkTwitter() {
        RegisterAccount account = new RegisterAccount(con);
        account.execute("");
    }

    /**
     * Check Twitter PIN
     */
    private void verifier() {
        String twitterPin = pin.getText().toString();
        if(!twitterPin.trim().isEmpty()) {
            RegisterAccount account = new RegisterAccount(con);
            account.setButton(loginButton,verifierButton);
            account.execute(twitterPin);
        } else {
            Toast.makeText(con,"PIN eingeben!",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Login Handle
     */
    private void login() {
        setContentView(R.layout.main_layout);
        list = (ListView) findViewById(R.id.list);
        setRefreshListener();
        setTabListener();
        Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setTabContent();
    }

    /**
     * Set Tab Listener
     */
    private void setTabListener() {
        tabhost = (TabHost)findViewById(R.id.tabhost);
        tabhost.setup();
        // Tab #1
        TabSpec tab1 = tabhost.newTabSpec("timeline");
        tab1.setIndicator("Timeline").setContent(R.id.list);
        tabhost.addTab(tab1);
        // Tab #2
        TabSpec tab2 = tabhost.newTabSpec("trends");
        tab2.setIndicator("Trend").setContent(R.id.list);
        tabhost.addTab(tab2);
        // Tab #3
        TabSpec tab3 = tabhost.newTabSpec("mention");
        tab3.setIndicator("Mention").setContent(R.id.list);
        tabhost.addTab(tab3);
        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                currentTab = tabId;
                setTabContent();}});
    }

    /**
     * Set DB Content
     * separate THREAD
     */
    private void setTabContent() {
        Thread thread = new Thread(){
            @Override
            public void run(){
                switch(currentTab){
                    case "timeline":
                        TweetDatabase tweetDeck = new TweetDatabase(con);
                        TimelineAdapter tlAdapt = new TimelineAdapter (con,R.layout.tweet,tweetDeck);
                        list.setAdapter(tlAdapt);
                        tlAdapt.notifyDataSetChanged();
                        break;
                    case "trends":
                        TrendDatabase trendDeck = new TrendDatabase(con);
                        TrendsAdapter trendAdp = new TrendsAdapter(con,R.layout.tweet,trendDeck);
                        list.setAdapter(trendAdp);
                        trendAdp.notifyDataSetChanged();
                        break;
                    case "mention":
                        list.setAdapter(null);
                        break;
                }
            }
        };
        thread.run();
    }

    /**
     * Swipe To Refresh Listener
     */
    private void setRefreshListener() {
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TwitterEngine homeView = new TwitterEngine(getApplicationContext(), list);
                homeView.setRefresh(refresh);
                switch(tabhost.getCurrentTab()) {
                    case(0):
                        homeView.execute(0);
                        break;
                    case(1):
                        homeView.execute(1);
                        break;
                    case(2):
                        homeView.execute(2);
                        break;
                }
            }
        });
    }

    /**
     * Login Check
     */
    private boolean loggedIn() {
        return einstellungen.getBoolean("login", false);
    }
}