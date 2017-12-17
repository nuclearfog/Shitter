package org.nuclearfog.twidda.newwindow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.engine.TwitterEngine;

public class Profile extends AppCompatActivity {

    private ListView homeTl;
    private TabHost mtab;
    private TextView username, bio,link,following;
    private ImageView profile_img, profile_banner;
    private SwipeRefreshLayout refresh;
    private String value;


    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.profile);
        value = getIntent().getExtras().getString("username");
        initElements();
        initTabs();
        initSwipe();


    }

    @Override
    protected void onDestroy(){



        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.buttons, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(item.getItemId())
        {
            case R.id.action_profile:
                Intent i = new Intent(this, Profile.class);
                startActivity(i);
                break;
            case R.id.action_tweet:
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

    private void loadContent(){}

    private void initHomeTimeline(){


    }

    /**
     * Profile Contents
     */
    private void initElements() {
        homeTl = (ListView) findViewById(R.id.home_tl);
        username = (TextView) findViewById(R.id.my_username);
        bio = (TextView) findViewById(R.id.bio);
        link = (TextView) findViewById(R.id.links);
        following = (TextView) findViewById(R.id.follow);
        profile_img = (ImageView) findViewById(R.id.profile_img);
        profile_banner = (ImageView) findViewById(R.id.banner);
    }

    /**
     * Swipe Refresh Layout
     */
    private void initSwipe(){
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { }
        });
    }

}
