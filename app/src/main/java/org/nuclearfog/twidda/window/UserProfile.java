package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;

import java.util.List;

/**
 * User Profile Class uses AsyncTask
 * @see ProfileLoader
 */
public class UserProfile extends AppCompatActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener,
        TimelineRecycler.OnItemClicked, ProfileLoader.OnProfileFinished {

    private ProfileLoader mProfile, mTweets, mFavorits;
    private SwipeRefreshLayout homeReload, favoriteReload;
    private RecyclerView homeTweets, homeFavorits;
    private long userId;
    private boolean home, imageload;
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
        imageload = settings.getBoolean("image_load", true);
        homeTweets = (RecyclerView) findViewById(R.id.ht_list);
        homeFavorits = (RecyclerView)findViewById(R.id.hf_list);
        homeTweets.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        homeFavorits.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        homeReload = (SwipeRefreshLayout) findViewById(R.id.hometweets);
        favoriteReload = (SwipeRefreshLayout) findViewById(R.id.homefavorits);
        TextView txtFollowing = (TextView)findViewById(R.id.following);
        TextView txtFollower  = (TextView)findViewById(R.id.follower);
        TabHost mTab = (TabHost)findViewById(R.id.profile_tab);
        setTabs(mTab);
        mTab.setOnTabChangedListener(this);
        txtFollowing.setOnClickListener(this);
        txtFollower.setOnClickListener(this);
        homeReload.setOnRefreshListener(this);
        favoriteReload.setOnRefreshListener(this);

        initElements();
    }

    @Override
    public void onBackPressed() {
        mProfile.cancel(true);
        if(mTweets != null)
            mTweets.cancel(true);
        if(mFavorits != null)
            mFavorits.cancel(true);
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

    @Override
    public void onItemClick(View v, ViewGroup parent, int position){
        TimelineRecycler tlAdp;
        if(parent.getId() == R.id.ht_list) {
            tlAdp = (TimelineRecycler) homeTweets.getAdapter();
        } else {
            tlAdp = (TimelineRecycler) homeFavorits.getAdapter();
        }
        Tweet tweet = tlAdp.getData().get(position);
        if(tweet.embedded != null)
            tweet = tweet.embedded;
        long tweetID = tweet.tweetID;
        long userID = tweet.userID;
        String username = tweet.screenname;
        Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
        Bundle bundle = new Bundle();
        bundle.putLong("tweetID",tweetID);
        bundle.putLong("userID",userID);
        bundle.putString("username", username);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void setTabs(TabHost mTab) {
        mTab.setup();
        TabHost.TabSpec tab1 = mTab.newTabSpec("tweets");
        tab1.setContent(R.id.hometweets);
        tab1.setIndicator("",getResources().getDrawable(R.drawable.home));
        mTab.addTab(tab1);
        TabHost.TabSpec tab2 = mTab.newTabSpec("favorites");
        tab2.setContent(R.id.homefavorits);
        tab2.setIndicator("",getResources().getDrawable(R.drawable.favorite));
        mTab.addTab(tab2);
    }

    /**
     * Tab Content
     */
    @Override
    public void onLoaded() {
        new Thread( new Runnable() {
                @Override
                public void run() {
                    ColorPreferences mcolor = ColorPreferences.getInstance(getApplicationContext());

                    int highlight  = mcolor.getColor(ColorPreferences.HIGHLIGHTING);
                    int background = mcolor.getColor(ColorPreferences.BACKGROUND);
                    int font_color = mcolor.getColor(ColorPreferences.FONT_COLOR);

                    TweetDatabase mTweet = new TweetDatabase(getApplicationContext());
                    TweetDatabase fTweet = new TweetDatabase(getApplicationContext());
                    List<Tweet> userTweets = mTweet.load(TweetDatabase.TWEET,userId);
                    List<Tweet> userFavorit = fTweet.load(TweetDatabase.FAVT,userId);

                    mTweets = new ProfileLoader(UserProfile.this);
                    mFavorits = new ProfileLoader(UserProfile.this);
                    homeTweets.setBackgroundColor(background);
                    homeFavorits.setBackgroundColor(background);

                    if( userTweets.size() > 0 ) {
                        TimelineRecycler tlRc = new TimelineRecycler(userTweets,UserProfile.this);
                        tlRc.setColor(highlight,font_color);
                        tlRc.toggleImage(imageload);
                        homeTweets.setAdapter(tlRc);
                    } else {
                        mTweets.execute(userId, ProfileLoader.GET_TWEETS,1L);
                    }
                    if( userFavorit.size() > 0 ) {
                        TimelineRecycler tlRc = new TimelineRecycler(userFavorit,UserProfile.this);
                        tlRc.setColor(highlight,font_color);
                        tlRc.toggleImage(imageload);
                        homeFavorits.setAdapter(tlRc);
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