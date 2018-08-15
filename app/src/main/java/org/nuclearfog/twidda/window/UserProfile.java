package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.GlobalSettings;
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler.OnItemClicked;

/**
 * User Profile Activity
 * @see ProfileLoader
 */
public class UserProfile extends AppCompatActivity implements OnClickListener,
        OnRefreshListener, OnTabChangeListener, OnItemClicked {

    private ProfileLoader mProfile, mTweets, mFavorites;
    private SwipeRefreshLayout homeReload, favoriteReload;
    private RecyclerView homeList, favoriteList;

    private TabHost mTab;
    private View lastView;
    private boolean home;
    private long userId = 0;
    private int tabIndex = 0;
    private String username = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        b = getIntent().getExtras();
        if (b != null) {
            userId = b.getLong("userID");
            username = b.getString("username");
        }
        setContentView(R.layout.profilepage);

        Toolbar tool = findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        home = userId == settings.getUserId();
        int background = settings.getBackgroundColor();

        homeList = findViewById(R.id.ht_list);
        homeList.setLayoutManager(new LinearLayoutManager(this));
        homeList.setBackgroundColor(background);

        favoriteList = findViewById(R.id.hf_list);
        favoriteList.setLayoutManager(new LinearLayoutManager(this));
        favoriteList.setBackgroundColor(background);

        homeReload = findViewById(R.id.hometweets);
        homeReload.setBackgroundColor(0xffff0000);

        favoriteReload = findViewById(R.id.homefavorits);
        favoriteReload.setBackgroundColor(0xffff0000);

        View txtFollowing = findViewById(R.id.following);
        View txtFollower  = findViewById(R.id.follower);
        mTab = findViewById(R.id.profile_tab);
        setTabs();

        mTab.setOnTabChangedListener(this);
        txtFollowing.setOnClickListener(this);
        txtFollower.setOnClickListener(this);
        homeReload.setOnRefreshListener(this);
        favoriteReload.setOnRefreshListener(this);

        getProfileTweets();
    }

    @Override
    protected void onPause() {
        if (mProfile != null && !mProfile.isCancelled()) {
            mProfile.cancel(true);
        }
        if (mTweets != null && !mTweets.isCancelled()) {
            mTweets.cancel(true);
            homeReload.setRefreshing(false);
        }
        if (mFavorites != null && !mFavorites.isCancelled()) {
            mFavorites.cancel(true);
            favoriteReload.setRefreshing(false);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(tabIndex == 0) {
            super.onBackPressed();
        } else {
            mTab.setCurrentTab(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.profile, m);
        if(!home) {
            m.findItem(R.id.profile_follow).setVisible(true);
            m.findItem(R.id.profile_block).setVisible(true);
            m.findItem(R.id.profile_mute).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mProfile = new ProfileLoader(this);
        switch(item.getItemId()) {
            case R.id.profile_tweet:
                Intent intent = new Intent(this, TweetPopup.class);
                if(!home)
                    intent.putExtra("Addition", username);
                startActivity(intent);
                return true;

            case R.id.profile_follow:
                mProfile.execute(userId, ProfileLoader.ACTION_FOLLOW);
                return true;

            case R.id.profile_block:
                mProfile.execute(userId, ProfileLoader.ACTION_BLOCK);
                return true;

            case R.id.profile_mute:
                mProfile.execute(userId, ProfileLoader.ACTION_MUTE);
                return true;

            default: return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.following:
                getConnection(0);
                break;
            case R.id.follower:
                getConnection(1);
                break;
        }
    }

    @Override
    public void onRefresh() {
        switch(tabIndex) {
            case 0:
                mTweets = new ProfileLoader(this);
                mTweets.execute(userId, ProfileLoader.GET_TWEETS,1L);
                break;
            case 1:
                mFavorites = new ProfileLoader(this);
                mFavorites.execute(userId, ProfileLoader.GET_FAVS,1L);
                break;
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        animate();
        tabIndex = mTab.getCurrentTab();
    }

    @Override
    public void onItemClick(View v, ViewGroup parent, int position){
        TimelineRecycler tweetAdapter;
        if(parent.getId() == R.id.ht_list) {
            tweetAdapter = (TimelineRecycler) homeList.getAdapter();
        } else {
            tweetAdapter = (TimelineRecycler) favoriteList.getAdapter();
        }
        if (tweetAdapter != null) {
            Tweet tweet = tweetAdapter.getData().get(position);
            if (tweet.embedded != null) {
                tweet = tweet.embedded;
            }
            Intent intent = new Intent(this, TweetDetail.class);
            intent.putExtra("tweetID", tweet.tweetID);
            intent.putExtra("userID", tweet.user.userID);
            intent.putExtra("username", tweet.user.screenname);
            startActivity(intent);
        }
    }

    private void setTabs() {
        mTab.setup();
        TabHost.TabSpec tab1 = mTab.newTabSpec("tweets");
        tab1.setContent(R.id.hometweets);
        tab1.setIndicator("", getDrawable(R.drawable.home));
        mTab.addTab(tab1);
        TabHost.TabSpec tab2 = mTab.newTabSpec("favorites");
        tab2.setContent(R.id.homefavorits);
        tab2.setIndicator("",getDrawable(R.drawable.favorite));
        mTab.addTab(tab2);
        lastView = mTab.getCurrentView();
    }

    private void animate() {
        final int ANIM_DUR = 300;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;

        Animation leftIn = new TranslateAnimation(DIMENS,-1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation rightIn = new TranslateAnimation(DIMENS,1.0f,DIMENS,0.0f,DIMENS,0.0f,DIMENS,0.0f);
        Animation leftOut = new TranslateAnimation(DIMENS, 0.0f, DIMENS, -1.0f, DIMENS, 0.0f, DIMENS, 0.0f);
        Animation rightOut = new TranslateAnimation(DIMENS, 0.0f, DIMENS, 1.0f, DIMENS, 0.0f, DIMENS, 0.0f);
        leftIn.setDuration(ANIM_DUR);
        rightIn.setDuration(ANIM_DUR);
        leftOut.setDuration(ANIM_DUR);
        rightOut.setDuration(ANIM_DUR);

        View currentTab = mTab.getCurrentView();

        if( mTab.getCurrentTab() > tabIndex ) {
            lastView.setAnimation(leftOut);
            currentTab.setAnimation(rightIn);
        } else {
            lastView.setAnimation(rightOut);
            currentTab.setAnimation(leftIn);
        }
        lastView = mTab.getCurrentView();
    }

    private void getProfileTweets() {
        new ProfileLoader(this).execute(userId, ProfileLoader.LOAD_DB, 1L);
        mProfile = new ProfileLoader(this);
        mTweets = new ProfileLoader(this);
        mFavorites = new ProfileLoader(this);
        mProfile.execute(userId, ProfileLoader.GET_INFORMATION,1L);
        mTweets.execute(userId, ProfileLoader.GET_TWEETS,1L);
        mFavorites.execute(userId, ProfileLoader.GET_FAVS,1L);
    }

    private void getConnection(int mode) {
        Intent intent = new Intent(this, UserDetail.class);
        intent.putExtra("userID", userId);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }
}