package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;

import java.text.DateFormat;
import java.util.List;

/**
 * User Profile Class uses AsyncTask
 * @see ProfileLoader
 */
public class UserProfile extends AppCompatActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, TabHost.OnTabChangeListener,
        TimelineRecycler.OnItemClicked {

    private ProfileLoader mProfile, mTweets, mFavorits;
    private SwipeRefreshLayout homeReload, favoriteReload;
    private RecyclerView homeList, favoritList;
    private long userId;
    private boolean home, imageload;
    private String username = "";
    private String currentTab = "tweets";
    int highlight, background, font_color;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.profilepage);
        Toolbar tool = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        getExtras(getIntent().getExtras());

        home = userId == TwitterEngine.getHomeId();
        ColorPreferences mcolor = ColorPreferences.getInstance(this);
        highlight  = mcolor.getColor(ColorPreferences.HIGHLIGHTING);
        background = mcolor.getColor(ColorPreferences.BACKGROUND);
        font_color = mcolor.getColor(ColorPreferences.FONT_COLOR);
        imageload = mcolor.loadImage();
        homeList = (RecyclerView) findViewById(R.id.ht_list);
        homeList.setLayoutManager(new LinearLayoutManager(this));
        homeList.setBackgroundColor(background);
        favoritList = (RecyclerView)findViewById(R.id.hf_list);
        favoritList.setLayoutManager(new LinearLayoutManager(this));
        favoritList.setBackgroundColor(background);
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
        getProfileInformation();
        getProfileTweets();
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
                if(!home)
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
                getConnection(0L);
                break;
            case R.id.follower:
                getConnection(1L);
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
            tlAdp = (TimelineRecycler) homeList.getAdapter();
        } else {
            tlAdp = (TimelineRecycler) favoritList.getAdapter();
        }
        Tweet tweet = tlAdp.getData().get(position);
        long tweetID = tweet.tweetID;
        long userID = tweet.user.userID;
        String username = tweet.user.screenname;
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
        tab1.setIndicator("", ContextCompat.getDrawable(getApplicationContext(),R.drawable.home));
        mTab.addTab(tab1);
        TabHost.TabSpec tab2 = mTab.newTabSpec("favorites");
        tab2.setContent(R.id.homefavorits);
        tab2.setIndicator("",ContextCompat.getDrawable(getApplicationContext(),R.drawable.favorite));
        mTab.addTab(tab2);
    }


    private void getProfileInformation() {
        new Thread( new Runnable() {
            @Override
            public void run() {
                DatabaseAdapter database = new DatabaseAdapter(getApplicationContext());
                TwitterUser user = database.getUser(userId);
                if(user != null) {
                    String dateString = "seit "+ DateFormat.getDateTimeInstance().format(user.created);
                    String followerStr = ""+user.follower;
                    String followingStr = ""+user.following;
                    TextView txtUser = (TextView)findViewById(R.id.profile_username);
                    TextView txtScrName = (TextView)findViewById(R.id.profile_screenname);
                    TextView txtBio = (TextView)findViewById(R.id.bio);
                    TextView txtCreated = (TextView)findViewById(R.id.profile_date);
                    TextView txtFollowing = (TextView)findViewById(R.id.following);
                    TextView txtFollower  = (TextView)findViewById(R.id.follower);
                    findViewById(R.id.following_icon).setVisibility(View.VISIBLE);
                    findViewById(R.id.follower_icon).setVisibility(View.VISIBLE);
                    TextView txtLocation = (TextView)findViewById(R.id.location);
                    TextView txtLink = (TextView)findViewById(R.id.links);
                    txtUser.setText(user.username);
                    txtScrName.setText(user.screenname);
                    txtBio.setText(user.bio);
                    txtCreated.setText(dateString);
                    txtFollower.setText(followerStr);
                    txtFollowing.setText(followingStr);
                    if(user.isVerified)
                        findViewById(R.id.profile_verify).setVisibility(View.VISIBLE);
                    if(user.isLocked)
                        findViewById(R.id.profile_locked).setVisibility(View.VISIBLE);
                    if(user.location != null && !user.location.isEmpty()) {
                        txtLocation.setText(user.location);
                        findViewById(R.id.location_img).setVisibility(View.VISIBLE);
                    }
                    if(user.link != null && !user.link.isEmpty()) {
                        txtLink.setText(user.link);
                        findViewById(R.id.link_img).setVisibility(View.VISIBLE);
                    }
                    findViewById(R.id.follower_icon).setVisibility(View.VISIBLE);
                    findViewById(R.id.following_icon).setVisibility(View.VISIBLE);
                }
            }}).run();
        // Refresh
        mProfile = new ProfileLoader(this);
        mProfile.execute(userId, ProfileLoader.GET_INFORMATION,1L);
    }


    private void getProfileTweets() {
        DatabaseAdapter mTweet = new DatabaseAdapter(getApplicationContext());
        DatabaseAdapter fTweet = new DatabaseAdapter(getApplicationContext());
        List<Tweet> userTweets = mTweet.load(DatabaseAdapter.TWEET,userId);
        List<Tweet> userFavorit = fTweet.load(DatabaseAdapter.FAVT,userId);

        mTweets = new ProfileLoader(UserProfile.this);
        mFavorits = new ProfileLoader(UserProfile.this);

        if( userTweets.size() > 0 ) {
            TimelineRecycler tlRc = new TimelineRecycler(userTweets,UserProfile.this);
            tlRc.setColor(highlight,font_color);
            tlRc.toggleImage(imageload);
            homeList.setAdapter(tlRc);
        } else {
            mTweets.execute(userId, ProfileLoader.GET_TWEETS,1L);
        }
        if( userFavorit.size() > 0 ) {
            TimelineRecycler tlRc = new TimelineRecycler(userFavorit,UserProfile.this);
            tlRc.setColor(highlight,font_color);
            tlRc.toggleImage(imageload);
            favoritList.setAdapter(tlRc);
        } else {
            mFavorits.execute(userId, ProfileLoader.GET_FAVS,1L);
        }
    }


    private void getConnection(long mode) {
        Intent intent = new Intent(getApplicationContext(), UserDetail.class);
        Bundle bundle = new Bundle();
        bundle.putLong("userID",userId);
        bundle.putLong("mode",mode);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void getExtras(Bundle b) {
        userId = b.getLong("userID");
        username = b.getString("username");
    }
}