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
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;

/**
 * User Profile Class uses AsyncTask
 * @see ProfileLoader
 */
public class UserProfile extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener,
        TabHost.OnTabChangeListener {

    private ProfileLoader mProfile, mTweets, mFavorits;
    private SwipeRefreshLayout homeReload, favoriteReload;
    private ListView homeTweets, homeFavorits;
    private long userId;
    private boolean home;
    private String username = "";
    private String currentTab = "tweets";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.profile);
        getExtras(getIntent().getExtras());
        Toolbar tool = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        SharedPreferences settings = getSharedPreferences("settings", 0);
        home = userId == settings.getLong("userID", -1);
        homeTweets = (ListView)findViewById(R.id.ht_list);
        homeFavorits = (ListView)findViewById(R.id.hf_list);
        homeReload = (SwipeRefreshLayout) findViewById(R.id.hometweets);
        favoriteReload = (SwipeRefreshLayout) findViewById(R.id.homefavorits);
        TextView txtFollowing = (TextView)findViewById(R.id.following);
        TextView txtFollower  = (TextView)findViewById(R.id.follower);
        TabHost mTab = (TabHost)findViewById(R.id.profile_tab);
        setTabs(mTab);
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
    protected void onDestroy() {
        mProfile.cancel(true);
        mTweets.cancel(true);
        mFavorits.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    /**
     * Home Button
     */
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        overridePendingTransition(0,0);
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
        mProfile = new ProfileLoader(this);
        switch(item.getItemId()) {
            case R.id.profile_tweet:
                Bundle extra = new Bundle();
                intent = new Intent(this, TweetPopup.class);
                if(username != null)
                    extra.putString("Addition", username);
                intent.putExtras(extra);
                startActivity(intent);
                return true;
            case R.id.profile_follow:
                mProfile.execute(userId, ProfileLoader.ACTION_FOLLOW);
                return true;
            case R.id.profile_block:
                mProfile.execute(userId, ProfileLoader.ACTION_MUTE);
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
        TimelineAdapter tlAdp;
        if(parent.getId() == R.id.ht_list) {
            tlAdp = (TimelineAdapter) homeTweets.getAdapter();
        } else {
            tlAdp = (TimelineAdapter) homeFavorits.getAdapter();
        }
        TweetDatabase twDB = tlAdp.getData();
        long tweetID = twDB.getTweetId(position);
        long userID = twDB.getUserID(position);
        String username = twDB.getScreenname(position);
        Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
        Bundle bundle = new Bundle();
        bundle.putLong("tweetID",tweetID);
        bundle.putLong("userID",userID);
        bundle.putString("username", username);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        switch(currentTab) {
            case "tweets":
                mTweets = new ProfileLoader(this);
                mTweets.execute(userId, ProfileLoader.GET_TWEETS,1L);
                break;
            case "favorites":
                mFavorits = new ProfileLoader(this);
                mFavorits.execute(userId, ProfileLoader.GET_FAVS,1L);
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        homeReload.setRefreshing(false);
        favoriteReload.setRefreshing(false);
        currentTab = tabId;
    }

    private void setTabs(TabHost mTab) {
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
    }

    /**
     * Tab Content
     */
    private void getContent() {
        new Thread( new Runnable() {
                @Override
                public void run() {
                    TweetDatabase mTweet = new TweetDatabase(UserProfile.this, TweetDatabase.USER_TL, userId);
                    TweetDatabase fTweet = new TweetDatabase(UserProfile.this, TweetDatabase.FAV_TL, userId);
                    mTweets = new ProfileLoader(UserProfile.this);
                    mFavorits = new ProfileLoader(UserProfile.this);
                    if( mTweet.getSize() > 0 ) {
                        homeTweets.setAdapter(new TimelineAdapter(UserProfile.this,mTweet));
                    }else {
                        mTweets.execute(userId, ProfileLoader.GET_TWEETS,1L);
                    }
                    if( fTweet.getSize() > 0 ) {
                        homeFavorits.setAdapter(new TimelineAdapter(UserProfile.this,fTweet));
                    } else {
                        mFavorits.execute(userId, ProfileLoader.GET_FAVS,1L);
                    }
                }
            }
        ).run();
    }

    /**
     * Profile Information
     */
    private void initElements() {
        mProfile = new ProfileLoader(this);
        mProfile.execute(userId, ProfileLoader.GET_INFORMATION,1L);
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

    @SuppressWarnings("ConstantConditions")
    private void getExtras(Bundle b) {
        userId = b.getLong("userID");
        username = b.getString("username");
    }
}