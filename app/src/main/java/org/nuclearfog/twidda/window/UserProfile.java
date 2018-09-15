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
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter.OnItemClicked;

import static android.os.AsyncTask.Status.RUNNING;


/**
 * User Profile Activity
 *
 * @see ProfileLoader
 */
public class UserProfile extends AppCompatActivity implements OnClickListener,
        OnRefreshListener, OnTabChangeListener, OnItemClicked {

    private ProfileLoader mProfile;
    private SwipeRefreshLayout homeReload, favoriteReload;
    private RecyclerView homeList, favoriteList;
    private TabHost mTab;
    private View lastTab;
    private boolean isFollowing, isBlocked, isMuted;
    private boolean home;
    private long userId = 0;
    private int tabIndex = 0;
    private String username = "";


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.profilepage);

        b = getIntent().getExtras();
        if (b != null) {
            userId = b.getLong("userID");
            username = b.getString("username");
        }

        Toolbar tool = findViewById(R.id.profile_toolbar);
        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        home = userId == settings.getUserId();
        int background = settings.getBackgroundColor();

        homeList = findViewById(R.id.ht_list);
        homeReload = findViewById(R.id.hometweets);
        homeList.setLayoutManager(new LinearLayoutManager(this));
        homeList.setBackgroundColor(background);

        favoriteList = findViewById(R.id.hf_list);
        favoriteReload = findViewById(R.id.homefavorits);
        favoriteList.setLayoutManager(new LinearLayoutManager(this));
        favoriteList.setBackgroundColor(background);

        View txtFollowing = findViewById(R.id.following);
        View txtFollower = findViewById(R.id.follower);
        mTab = findViewById(R.id.profile_tab);
        mTab.setup();
        TabHost.TabSpec tab1 = mTab.newTabSpec("tweets");
        tab1.setContent(R.id.hometweets);
        tab1.setIndicator("", getDrawable(R.drawable.home));
        mTab.addTab(tab1);
        TabHost.TabSpec tab2 = mTab.newTabSpec("favorites");
        tab2.setContent(R.id.homefavorits);
        tab2.setIndicator("", getDrawable(R.drawable.favorite));
        mTab.addTab(tab2);
        lastTab = mTab.getCurrentView();

        mTab.setOnTabChangedListener(this);
        txtFollowing.setOnClickListener(this);
        txtFollower.setOnClickListener(this);
        homeReload.setOnRefreshListener(this);
        favoriteReload.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mProfile == null) {
            mProfile = new ProfileLoader(this);
            mProfile.execute(userId, 0L);
            homeReload.setRefreshing(true);
            favoriteReload.setRefreshing(true);
        }
    }


    @Override
    protected void onStop() {
        if (mProfile != null && mProfile.getStatus() == RUNNING) {
            mProfile.cancel(true);
            homeReload.setRefreshing(false);
            favoriteReload.setRefreshing(false);
        }
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.profile, m);
        if (!home) {
            m.findItem(R.id.profile_follow).setVisible(true);
            m.findItem(R.id.profile_block).setVisible(true);
            m.findItem(R.id.profile_mute).setVisible(true);
        }
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        MenuItem followIcon = m.findItem(R.id.profile_follow);
        MenuItem blockIcon = m.findItem(R.id.profile_block);
        MenuItem muteIcon = m.findItem(R.id.profile_mute);

        if (isFollowing) {
            followIcon.setIcon(R.drawable.follow_enabled);
            followIcon.setTitle(R.string.unfollow);
        } else {
            followIcon.setIcon(R.drawable.follow);
            followIcon.setTitle(R.string.follow);
        }
        if (isBlocked) {
            blockIcon.setTitle(R.string.unblock);
            followIcon.setVisible(false);
        } else {
            blockIcon.setTitle(R.string.block);
            followIcon.setVisible(true);
        }
        if (isMuted) {
            muteIcon.setTitle(R.string.unmute);
        } else {
            muteIcon.setTitle(R.string.mute);
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mProfile != null && mProfile.getStatus() != RUNNING) {
            switch (item.getItemId()) {
                case R.id.profile_tweet:
                    Intent intent = new Intent(this, TweetPopup.class);
                    if (!home)
                        intent.putExtra("Addition", username);
                    startActivity(intent);
                    break;

                case R.id.profile_follow:
                    mProfile = new ProfileLoader(this);
                    mProfile.execute(userId, ProfileLoader.ACTION_FOLLOW);
                    break;

                case R.id.profile_block:
                    mProfile = new ProfileLoader(this);
                    mProfile.execute(userId, ProfileLoader.ACTION_BLOCK);
                    break;

                case R.id.profile_mute:
                    mProfile = new ProfileLoader(this);
                    mProfile.execute(userId, ProfileLoader.ACTION_MUTE);
                    break;

                case R.id.profile_message:
                    if (home) {
                        Intent dm = new Intent(this, DirectMessage.class);
                        startActivity(dm);
                    } else {
                        Intent sendDm = new Intent(this, MessagePopup.class);
                        sendDm.putExtra("username", username);
                        startActivity(sendDm);
                    }
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tabIndex == 0) {
            super.onBackPressed();
        } else {
            mTab.setCurrentTab(0);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.following:
                Intent following = new Intent(this, UserDetail.class);
                following.putExtra("userID", userId);
                following.putExtra("mode", 0);
                startActivity(following);
                break;
            case R.id.follower:
                Intent follower = new Intent(this, UserDetail.class);
                follower.putExtra("userID", userId);
                follower.putExtra("mode", 1);
                startActivity(follower);
                break;
        }
    }


    @Override
    public void onRefresh() {
        if (mProfile != null && mProfile.getStatus() == RUNNING)
            mProfile.cancel(true);

        if (tabIndex == 0) {
            mProfile = new ProfileLoader(this);
            mProfile.execute(userId, ProfileLoader.GET_TWEETS, 1L);
        } else {
            mProfile = new ProfileLoader(this);
            mProfile.execute(userId, ProfileLoader.GET_FAVORS, 1L);
        }
    }


    @Override
    public void onTabChanged(String tabId) {
        animate();
        tabIndex = mTab.getCurrentTab();

        if (tabIndex == 0)
            favoriteList.smoothScrollToPosition(0);
        else
            homeList.smoothScrollToPosition(0);
    }


    @Override
    public void onItemClick(ViewGroup parent, int position) {
        TimelineAdapter tweetAdapter;
        if (parent.getId() == R.id.ht_list)
            tweetAdapter = (TimelineAdapter) homeList.getAdapter();
        else
            tweetAdapter = (TimelineAdapter) favoriteList.getAdapter();

        if (tweetAdapter != null) {
            Tweet tweet = tweetAdapter.getData().get(position);
            if (tweet.embedded != null)
                tweet = tweet.embedded;

            Intent intent = new Intent(this, TweetDetail.class);
            intent.putExtra("tweetID", tweet.tweetID);
            intent.putExtra("userID", tweet.user.userID);
            intent.putExtra("username", tweet.user.screenname);
            startActivity(intent);
        }
    }


    private void animate() {
        final int ANIM_DUR = 300;
        final int DIMENS = Animation.RELATIVE_TO_PARENT;
        final float LEFT = -1.0f;
        final float RIGHT = 1.0f;
        final float NULL = 0.0f;
        Animation lIn = new TranslateAnimation(DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
        Animation rIn = new TranslateAnimation(DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL, DIMENS, NULL);
        Animation lOut = new TranslateAnimation(DIMENS, NULL, DIMENS, LEFT, DIMENS, NULL, DIMENS, NULL);
        Animation rOut = new TranslateAnimation(DIMENS, NULL, DIMENS, RIGHT, DIMENS, NULL, DIMENS, NULL);
        lIn.setDuration(ANIM_DUR);
        rIn.setDuration(ANIM_DUR);
        lOut.setDuration(ANIM_DUR);
        rOut.setDuration(ANIM_DUR);

        View currentTab = mTab.getCurrentView();
        if (mTab.getCurrentTab() > tabIndex) {
            lastTab.setAnimation(lOut);
            currentTab.setAnimation(rIn);
        } else {
            lastTab.setAnimation(rOut);
            currentTab.setAnimation(lIn);
        }
        lastTab = mTab.getCurrentView();
    }


    public void setConnection(boolean isFollowing, boolean isMuted, boolean isBlocked) {
        this.isFollowing = isFollowing;
        this.isMuted = isMuted;
        this.isBlocked = isBlocked;
    }
}