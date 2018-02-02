package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
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
    private Toolbar tool;
    private long userId;
    private boolean home;
    private String currentTab = "tweets";

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.profile);
        tool = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userId = getIntent().getExtras().getLong("userID");
        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", 0);
        home = userId == settings.getLong("userID", -1);
        homeTweets = (ListView)findViewById(R.id.ht_list);
        homeFavorits = (ListView)findViewById(R.id.hf_list);
        TextView txtFollowing = (TextView)findViewById(R.id.following);
        TextView txtFollower  = (TextView)findViewById(R.id.follower);
        homeReload = (SwipeRefreshLayout) findViewById(R.id.hometweets);
        favoriteReload = (SwipeRefreshLayout) findViewById(R.id.homefavorits);
        TabHost mTab = (TabHost)findViewById(R.id.profile_tab);
        mTab.setup();
        // Tab #1
        TabHost.TabSpec tab1 = mTab.newTabSpec("tweets");
        tab1.setContent(R.id.hometweets);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.home));
        mTab.addTab(tab1);
        // Tab #2
        TabHost.TabSpec tab2 = mTab.newTabSpec("favorites");
        tab2.setContent(R.id.homefavorits);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.favorite));
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
                if(home) {
                    b.putLong("TweetID", -1);
                } else {
                    b.putLong("TweetID", userId);
                }
                intent.putExtras(b);
                startActivity(intent);
                return true;
            case R.id.profile_follow:
                if(!home) {
                    action.execute(userId, ProfileAction.ACTION_FOLLOW);
                }
                return true;
            case R.id.profile_block:
                if(!home) {
                    action.execute(userId, ProfileAction.ACTION_MUTE);
                }
                return true;
            default: return false;
        }
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
                tweets.execute(userId, ProfileAction.GET_TWEETS,1L);
                break;
            case "favorites":
                tweets.execute(userId, ProfileAction.GET_FAVS,1L);
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        homeReload.setRefreshing(false);
        favoriteReload.setRefreshing(false);
        currentTab = tabId;
    }

    /**
     * Tab Content
     */
    private void getContent() {
        TweetDatabase mTweet = new TweetDatabase(UserProfile.this, TweetDatabase.USER_TL, userId);
        TweetDatabase fTweet = new TweetDatabase(UserProfile.this, TweetDatabase.FAV_TL, userId);
        if(mTweet.getSize()>0) {
            homeTweets.setAdapter(new TimelineAdapter(UserProfile.this,mTweet));
        }else {
            new ProfileAction(this, tool).execute(userId, ProfileAction.GET_TWEETS,1L);
        }
        if(fTweet.getSize()>0) {
            homeFavorits.setAdapter(new TimelineAdapter(UserProfile.this,fTweet));
        } else {
            new ProfileAction(this, tool).execute(userId, ProfileAction.GET_FAVS,1L);
        }
    }

    /**
     * Profile Information
     */
    private void initElements() {
        new ProfileAction(this, tool).execute(userId, ProfileAction.GET_INFORMATION,1L);
    }

    /**
     *  @param mode 0L = Following , 1L Follower
     */
    private void getFollows(long mode) {
        Intent intent = new Intent(getApplicationContext(), UserDetail.class);
        Bundle bundle = new Bundle();
        bundle.putLong("userID",userId);
        bundle.putLong("mode",mode);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}