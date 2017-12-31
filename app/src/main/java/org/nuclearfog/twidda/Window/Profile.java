package org.nuclearfog.twidda.Window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;

import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.Backend.ProfileInfo;
import org.nuclearfog.twidda.Backend.ProfileTweets;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;

public class Profile extends AppCompatActivity {

    private SwipeRefreshLayout homeReload, favoriteReload;
    private ListView homeTweets, homeFavorits;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.profile);
        Toolbar tool = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userId = getIntent().getExtras().getLong("userID");
        homeTweets = (ListView)findViewById(R.id.ht_list);
        homeFavorits = (ListView)findViewById(R.id.hf_list);
        initElements();
        initTabs();
        initSwipe();
        getContent();
        setListener();
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
        switch(item.getItemId()) {
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
        TabHost mTab = (TabHost)findViewById(R.id.profile_tab);
        mTab.setup();
        // Tab #1
        TabHost.TabSpec tab1 = mTab.newTabSpec("tweets");
        tab1.setContent(R.id.hometweets);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.timeline_icon));
        mTab.addTab(tab1);
        // Tab #2
        TabHost.TabSpec tab2 = mTab.newTabSpec("favorites");
        tab2.setContent(R.id.homefavorits);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.favorite_icon));
        mTab.addTab(tab2);
        // Listener
        mTab.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                homeReload.setRefreshing(false);
                favoriteReload.setRefreshing(false);
            }
        });
    }

    /**
     * Init SwipeRefresh
     */
    private void initSwipe(){
        homeReload = (SwipeRefreshLayout) findViewById(R.id.hometweets);
        homeReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTweets(0L);
            }
        });
        favoriteReload = (SwipeRefreshLayout) findViewById(R.id.homefavorits);
        favoriteReload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTweets(1L);
            }
        });
    }

    /**
     * Load Content from Database
     */
    private void getContent(){
        new Thread(){
            @Override
            public void run(){
                TweetDatabase mTweet = new TweetDatabase(Profile.this, TweetDatabase.USER_TL, userId);
                TimelineAdapter tl = new TimelineAdapter(Profile.this,mTweet);
                homeTweets.setAdapter(tl);
                TweetDatabase fTweet = new TweetDatabase(Profile.this, TweetDatabase.FAV_TL, userId);
                TimelineAdapter fl = new TimelineAdapter(Profile.this,fTweet);
                homeFavorits.setAdapter(fl);
            }
        }.run();
    }

    /**
     * Profile Contents
     */
    private void initElements() {
        ProfileInfo profile = new ProfileInfo(this);
        profile.execute(userId);
    }

    /**
     * Download Content
     * @param mode  0 = Home Tweets, 1 = Home Favorite Tweets
     */
    private void getTweets(long mode ){
        ProfileTweets mProfile = new ProfileTweets(this);
        mProfile.execute(userId, mode);
    }

    private void setListener(){}
}