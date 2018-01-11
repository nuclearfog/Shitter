package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.TwitterResource;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.backend.ProfileAction;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;

/**
 * User Profile Class uses AsyncTask
 * @see ProfileAction
 */
public class UserProfile extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener {

    private SwipeRefreshLayout homeReload, favoriteReload;
    private ListView homeTweets, homeFavorits;
    private TextView txtFollowing, txtFollower;
    private Toolbar tool;
    private long userId;
    private boolean home;
    private String currentTab = "tweets";

    private RecyclerView testV;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.profile);
        tool = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userId = getIntent().getExtras().getLong("userID");
        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", 0);
        long homeId = settings.getLong("userID", -1);
        home = userId == homeId;
        homeTweets = (ListView)findViewById(R.id.ht_list);
        homeFavorits = (ListView)findViewById(R.id.hf_list);
        txtFollowing = (TextView)findViewById(R.id.following);
        txtFollower  = (TextView)findViewById(R.id.follower);
        homeReload = (SwipeRefreshLayout) findViewById(R.id.hometweets);
        favoriteReload = (SwipeRefreshLayout) findViewById(R.id.homefavorits);
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


        mTab.setOnTabChangedListener(this);
        txtFollowing.setOnClickListener(this);
        txtFollower.setOnClickListener(this);
        homeTweets.setOnItemClickListener(this);
        homeFavorits.setOnItemClickListener(this);
        homeReload.setOnRefreshListener(this);
        favoriteReload.setOnRefreshListener(this);
        initElements();
        getContent();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.profile, m);
        if(!home) {
            m.findItem(R.id.profile_follow).setVisible(true);
            m.findItem(R.id.profile_block).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        ProfileAction action = new ProfileAction(this, tool);
        switch(item.getItemId()) {
            case R.id.profile_tweet:
                intent = new Intent(this, TweetPopup.class);
                Bundle b = new Bundle();
                if(home)
                    b.putLong("TweetID", -1);
                else
                    b.putLong("TweetID", userId);
                intent.putExtras(b);
                startActivity(intent);
                break;
            case R.id.profile_follow:
                action.execute(userId, ProfileAction.ACTION_FOLLOW);
                break;
            case R.id.profile_block:
                action.execute(userId, ProfileAction.ACTION_MUTE);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.following:
                getFollows(0L);
                break;
            case R.id.follower:
                getFollows(1L);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.ht_list:
                if(!homeReload.isRefreshing()) {
                    TimelineAdapter tlAdp = (TimelineAdapter) homeTweets.getAdapter();
                    TweetDatabase twDB = tlAdp.getAdapter();
                    long tweetID = twDB.getTweetId(position);
                    long userID = twDB.getUserID(position);
                    Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("tweetID",tweetID);
                    bundle.putLong("userID",userID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.hf_list:
                if(!favoriteReload.isRefreshing()) {
                    TimelineAdapter tlAdp = (TimelineAdapter) homeFavorits.getAdapter();
                    TweetDatabase twDB = tlAdp.getAdapter();
                    long tweetID = twDB.getTweetId(position);
                    long userID = twDB.getUserID(position);
                    Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
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
        ProfileAction tweets = new ProfileAction(this, tool);
        switch(currentTab) {
            case "tweets":
                tweets.execute(userId, ProfileAction.GET_TWEETS);
                break;
            case "favorites":
                tweets.execute(userId, ProfileAction.GET_FAVS);
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        homeReload.setRefreshing(false);
        favoriteReload.setRefreshing(false);
        currentTab = tabId;
    }

    private void getContent() {
        new Thread(){
            @Override
            public void run(){
                TweetDatabase mTweet = new TweetDatabase(UserProfile.this, TweetDatabase.USER_TL, userId);
                TimelineAdapter tl = new TimelineAdapter(UserProfile.this,mTweet);
                homeTweets.setAdapter(tl);
                TweetDatabase fTweet = new TweetDatabase(UserProfile.this, TweetDatabase.FAV_TL, userId);
                TimelineAdapter fl = new TimelineAdapter(UserProfile.this,fTweet);
                homeFavorits.setAdapter(fl);
            }
        }.run();
    }

    private void initElements() {
        ProfileAction profile = new ProfileAction(this, tool);
        profile.execute(userId, ProfileAction.GET_INFORMATION);
    }

    private void getFollows(long mode) {
        Intent intent = new Intent(getApplicationContext(), Follower.class);
        Bundle bundle = new Bundle();
        bundle.putLong("userID",userId);
        bundle.putLong("mode",mode);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}