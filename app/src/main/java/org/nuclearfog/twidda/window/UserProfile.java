package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.window.TweetDetail.TWEET_REMOVED;

/**
 * User Profile Activity
 *
 * @see ProfileLoader
 */
public class UserProfile extends AppCompatActivity implements OnRefreshListener,
        OnTabChangeListener, OnItemClickListener, OnTagClickListener {

    private static final int TWEET = 1;

    private ProfileLoader mProfile;
    private GlobalSettings settings;
    private RecyclerView homeList, favoriteList;
    private SwipeRefreshLayout homeReload, favoriteReload;
    private View lastTab, tweetIndicator, favorIndicator;
    private TabHost mTab;
    private NumberFormat formatter;
    private boolean home, isFollowing, isBlocked, isMuted, canDm, requested;
    private String username;
    private long userId;
    private int tabIndex = 0;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_profile);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            userId = param.getLong("userID");
            username = param.getString("username");
        }

        Toolbar tool = findViewById(R.id.profile_toolbar);
        TextView bioTxt = findViewById(R.id.bio);
        View root = findViewById(R.id.user_view);
        homeList = findViewById(R.id.ht_list);
        homeReload = findViewById(R.id.hometweets);
        favoriteList = findViewById(R.id.hf_list);
        favoriteReload = findViewById(R.id.homefavorits);
        mTab = findViewById(R.id.profile_tab);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        settings = GlobalSettings.getInstance(this);
        home = userId == settings.getUserId();
        formatter = NumberFormat.getIntegerInstance();

        homeList.setLayoutManager(new LinearLayoutManager(this));
        favoriteList.setLayoutManager(new LinearLayoutManager(this));
        root.setBackgroundColor(settings.getBackgroundColor());
        bioTxt.setMovementMethod(ScrollingMovementMethod.getInstance());

        LayoutInflater inflater = LayoutInflater.from(this);
        tweetIndicator = inflater.inflate(R.layout.tab_tw, null);
        favorIndicator = inflater.inflate(R.layout.tab_fa, null);

        mTab.setup();
        TabHost.TabSpec tab1 = mTab.newTabSpec("tweets");
        tab1.setContent(R.id.hometweets);
        tab1.setIndicator(tweetIndicator);
        mTab.addTab(tab1);
        TabHost.TabSpec tab2 = mTab.newTabSpec("favors");
        tab2.setContent(R.id.homefavorits);
        tab2.setIndicator(favorIndicator);
        mTab.addTab(tab2);
        lastTab = mTab.getCurrentView();

        mTab.setOnTabChangedListener(this);
        homeReload.setOnRefreshListener(this);
        favoriteReload.setOnRefreshListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mProfile == null) {
            TimelineAdapter homeTl = new TimelineAdapter(this);
            homeTl.setColor(settings.getHighlightColor(), settings.getFontColor());
            homeTl.toggleImage(settings.getImageLoad());
            homeList.setAdapter(homeTl);

            TimelineAdapter homeFav = new TimelineAdapter(this);
            homeFav.setColor(settings.getHighlightColor(), settings.getFontColor());
            homeFav.toggleImage(settings.getImageLoad());
            favoriteList.setAdapter(homeFav);

            mProfile = new ProfileLoader(this);
            mProfile.execute(userId, 0L);
            homeReload.setRefreshing(true);
            favoriteReload.setRefreshing(true);
        }
    }


    @Override
    protected void onStop() {
        if (mProfile != null && mProfile.getStatus() == RUNNING)
            mProfile.cancel(true);
        super.onStop();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode, returnCode, i);
        if (reqCode == TWEET && returnCode == TWEET_REMOVED) {
            mProfile = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.profile, m);
        if (!home) {
            m.findItem(R.id.profile_follow).setVisible(true);
            m.findItem(R.id.profile_block).setVisible(true);
            m.findItem(R.id.profile_mute).setVisible(true);
            m.findItem(R.id.profile_settings).setVisible(false);
        }
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        if (!home) {
            MenuItem followIcon = m.findItem(R.id.profile_follow);
            MenuItem blockIcon = m.findItem(R.id.profile_block);
            MenuItem muteIcon = m.findItem(R.id.profile_mute);
            MenuItem dmIcon = m.findItem(R.id.profile_message);

            if (isFollowing) {
                followIcon.setIcon(R.drawable.follow_enabled);
                followIcon.setTitle(R.string.unfollow);
            } else if (requested) {
                followIcon.setIcon(R.drawable.follow_requested);
                followIcon.setTitle(R.string.follow_requested);
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
            if (!canDm) {
                dmIcon.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mProfile != null && mProfile.getStatus() != RUNNING) {
            switch (item.getItemId()) {
                case R.id.profile_tweet:
                    Intent tweet = new Intent(this, TweetPopup.class);
                    if (!home)
                        tweet.putExtra("Addition", username);
                    startActivity(tweet);
                    break;

                case R.id.profile_follow:
                    mProfile = new ProfileLoader(this);
                    if (!isFollowing) {
                        mProfile.execute(userId, ProfileLoader.ACTION_FOLLOW);
                    } else {
                        new Builder(this).setMessage(R.string.confirm_unfollow)
                                .setNegativeButton(R.string.no_confirm, null)
                                .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mProfile.execute(userId, ProfileLoader.ACTION_FOLLOW);
                                    }
                                })
                                .show();
                    }
                    break;

                case R.id.profile_block:
                    mProfile = new ProfileLoader(this);
                    if (isBlocked) {
                        mProfile.execute(userId, ProfileLoader.ACTION_BLOCK);
                    } else {
                        new Builder(this).setMessage(R.string.confirm_block)
                                .setNegativeButton(R.string.no_confirm, null)
                                .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mProfile.execute(userId, ProfileLoader.ACTION_BLOCK);
                                    }
                                })
                                .show();
                    }
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

                case R.id.profile_settings:
                    Intent editProfile = new Intent(this, ProfileEdit.class);
                    startActivity(editProfile);
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
    public void onRefresh() {
        if (mProfile != null && mProfile.getStatus() == RUNNING)
            mProfile.cancel(true);
        mProfile = new ProfileLoader(this);

        switch (tabIndex) {
            case 0:
                mProfile.execute(userId, ProfileLoader.GET_TWEETS, 1L);
                break;
            case 1:
                mProfile.execute(userId, ProfileLoader.GET_FAVORS, 1L);
                break;
        }
    }


    @Override
    public void onTabChanged(String tabId) {
        animate();

        switch (tabIndex) {
            case 0:
                homeList.smoothScrollToPosition(0);
                favorIndicator.findViewById(R.id.favor_divider).setBackgroundResource(R.color.soylentgreen);
                tweetIndicator.findViewById(R.id.tweet_divider).setBackgroundResource(android.R.color.transparent);
                break;

            case 1:
                favoriteList.smoothScrollToPosition(0);
                tweetIndicator.findViewById(R.id.tweet_divider).setBackgroundResource(R.color.soylentgreen);
                favorIndicator.findViewById(R.id.favor_divider).setBackgroundResource(android.R.color.transparent);
                break;
        }
        tabIndex = mTab.getCurrentTab();
    }


    @Override
    public void onItemClick(RecyclerView parent, int position) {
        if (parent.getId() == R.id.ht_list) {
            TimelineAdapter tweetAdapter = (TimelineAdapter) homeList.getAdapter();
            if (tweetAdapter != null && !homeReload.isRefreshing()) {
                Tweet tweet = tweetAdapter.getData().get(position);
                if (tweet.getEmbeddedTweet() != null)
                    tweet = tweet.getEmbeddedTweet();
                openTweet(tweet.getId(), tweet.getUser().getId(), tweet.getUser().getScreenname());
            }
        } else {
            TimelineAdapter tweetAdapter = (TimelineAdapter) favoriteList.getAdapter();
            if (tweetAdapter != null && !favoriteReload.isRefreshing()) {
                Tweet tweet = tweetAdapter.getData().get(position);
                if (tweet.getEmbeddedTweet() != null)
                    tweet = tweet.getEmbeddedTweet();
                openTweet(tweet.getId(), tweet.getUser().getId(), tweet.getUser().getScreenname());
            }
        }
    }


    @Override
    public void onClick(String text) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra("search", text);
        startActivity(intent);
    }


    public void setTweetCount(int tweets, int favors) {
        TextView tweetCount = tweetIndicator.findViewById(R.id.profile_tweet_count);
        TextView favorCount = favorIndicator.findViewById(R.id.profile_favor_count);
        tweetCount.setText(formatter.format(tweets));
        favorCount.setText(formatter.format(favors));
    }


    public void setConnection(boolean isFollowing, boolean isMuted, boolean isBlocked, boolean canDm, boolean requested) {
        this.isFollowing = isFollowing;
        this.isMuted = isMuted;
        this.isBlocked = isBlocked;
        this.canDm = canDm;
        this.requested = requested;
    }


    private void openTweet(long tweetId, long userId, String username) {
        Intent intent = new Intent(this, TweetDetail.class);
        intent.putExtra("tweetID", tweetId);
        intent.putExtra("userID", userId);
        intent.putExtra("username", username);
        startActivityForResult(intent, TWEET);
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


    public void imageClick(String link) {
        Intent image = new Intent(this, ImageDetail.class);
        image.putExtra("link", new String[]{link});
        startActivity(image);
    }
}