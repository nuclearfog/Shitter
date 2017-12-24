package org.nuclearfog.twidda.Window;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import org.nuclearfog.twidda.Engine.ProfileInformation;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.Engine.TwitterEngine;

public class Profile extends AppCompatActivity {

    private TabHost mtab;
    private SwipeRefreshLayout refresh;
    private long userId;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.profile);
        Toolbar tool = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userId = getIntent().getExtras().getLong("userID");
        context = getApplicationContext();
        initElements();
        initTabs();
        initSwipe();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.home, m);
        m.findItem(R.id.action_profile).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId())
        {
            case R.id.action_tweet:
                intent = new Intent(this, TweetWindow.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(this,Settings.class);
                startActivity(intent);
                break;
        }
        return true;
    }


    /**
     * Init Tab Listener
     */
    private void initTabs(){
        mtab = (TabHost)findViewById(R.id.tabhost);
        mtab.setup();
        // Tab #1
        TabHost.TabSpec tab1 = mtab.newTabSpec("tweets");
        tab1.setIndicator("Tweets").setContent(R.id.home_tl);
        mtab.addTab(tab1);
        // Tab #2
        TabHost.TabSpec tab2 = mtab.newTabSpec("favorites");
        tab2.setIndicator("Favorits").setContent(R.id.home_tl);
        mtab.addTab(tab2);
        // Listener
        mtab.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) { }
        });
    }

    /**
     * Profile Contents
     */
    private void initElements() {
        ProfileInformation profile = new ProfileInformation(this);
        profile.execute(userId);
    }

    /**
     * Swipe Refresh Layout
     */
    private void initSwipe(){
        refresh = (SwipeRefreshLayout) findViewById(R.id.refreshHome);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTweets();
            }
        });
    }

    private void getTweets(){
        TwitterEngine twitterEngine = new TwitterEngine(this);
        twitterEngine.execute(3L,userId);
    }
}
