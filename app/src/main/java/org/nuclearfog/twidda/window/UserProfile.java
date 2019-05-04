package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ProfileTabAdapter;
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.backend.ProfileLoader.Mode.LDR_PROFILE;
import static org.nuclearfog.twidda.window.TweetDetail.STAT_CHANGED;

/**
 * User Profile Activity
 *
 * @see ProfileLoader
 */
public class UserProfile extends AppCompatActivity implements OnTagClickListener, OnTabSelectedListener {

    private static final int TWEET = 1;

    private ProfileLoader profileAsync;
    private ViewPager pager;
    private View[] icons;

    private boolean home, isFollowing, isBlocked, isMuted, canDm, requested;
    private String username;
    private long userId;

    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_profile);

        TextView bioTxt = findViewById(R.id.bio);
        TextView lnkTxt = findViewById(R.id.links);
        View root = findViewById(R.id.user_view);
        TabLayout tab = findViewById(R.id.profile_tab);
        pager = findViewById(R.id.profile_pager);
        Toolbar tool = findViewById(R.id.profile_toolbar);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            if (BuildConfig.DEBUG && param.size() != 2)
                throw new AssertionError();
            userId = param.getLong("userID");
            username = param.getString("username");
        }

        GlobalSettings settings = GlobalSettings.getInstance(this);
        home = userId == settings.getUserId();

        root.setBackgroundColor(settings.getBackgroundColor());
        bioTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
        lnkTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
        bioTxt.setLinkTextColor(settings.getHighlightColor());
        lnkTxt.setLinkTextColor(settings.getHighlightColor());

        icons = new View[2];
        LayoutInflater inflater = LayoutInflater.from(this);
        icons[0] = inflater.inflate(R.layout.tab_tw, null);
        icons[1] = inflater.inflate(R.layout.tab_fa, null);

        ProfileTabAdapter adapter = new ProfileTabAdapter(getSupportFragmentManager(), userId);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(adapter);
        tab.setupWithViewPager(pager);
        tab.addOnTabSelectedListener(this);

        for(int i = 0 ; i < icons.length ; i++) {
            TabLayout.Tab t = tab.getTabAt(i);
            if(t != null)
                t.setCustomView(icons[i]);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(profileAsync == null) {
            profileAsync = new ProfileLoader(this, LDR_PROFILE);
            profileAsync.execute(userId);
        }
    }


    @Override
    protected void onStop() {
        if (profileAsync != null && profileAsync.getStatus() == RUNNING)
            profileAsync.cancel(true);
        super.onStop();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        if (reqCode == TWEET && returnCode == STAT_CHANGED)
            profileAsync = null;
        super.onActivityResult(reqCode, returnCode, i);
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
        if (profileAsync != null && profileAsync.getStatus() != RUNNING) {
            switch (item.getItemId()) {
                case R.id.profile_tweet:
                    Intent tweet = new Intent(this, TweetPopup.class);
                    if (!home)
                        tweet.putExtra("Addition", username);
                    startActivity(tweet);
                    break;

                case R.id.profile_follow:
                    profileAsync = new ProfileLoader(this, ProfileLoader.Mode.ACTION_FOLLOW);
                    if (!isFollowing) {
                        profileAsync.execute(userId);
                    } else {
                        new Builder(this).setMessage(R.string.confirm_unfollow)
                                .setNegativeButton(R.string.no_confirm, null)
                                .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        profileAsync.execute(userId);
                                    }
                                })
                                .show();
                    }
                    break;

                case R.id.profile_block:
                    profileAsync = new ProfileLoader(this, ProfileLoader.Mode.ACTION_BLOCK);
                    if (isBlocked) {
                        profileAsync.execute(userId);
                    } else {
                        new Builder(this).setMessage(R.string.confirm_block)
                                .setNegativeButton(R.string.no_confirm, null)
                                .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        profileAsync.execute(userId);
                                    }
                                })
                                .show();
                    }
                    break;

                case R.id.profile_mute:
                    profileAsync = new ProfileLoader(this, ProfileLoader.Mode.ACTION_MUTE);
                    profileAsync.execute(userId);
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
                    profileAsync = null;
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
            pager.setCurrentItem(0);
        }
    }


    @Override
    public void onClick(String text) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra("search", text);
        startActivity(intent);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        tabIndex = tab.getPosition();
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) { }


    @Override
    public void onTabReselected(TabLayout.Tab tab) { }


    public void setTweetCount(int tweets, int favors) {
        NumberFormat formatter = NumberFormat.getIntegerInstance();
        TextView tweetCount = icons[0].findViewById(R.id.profile_tweet_count);
        tweetCount.setText(formatter.format(tweets));
        TextView favorCount = icons[1].findViewById(R.id.profile_favor_count);
        favorCount.setText(formatter.format(favors));
    }


    public void setConnection(boolean isFollowing, boolean isMuted, boolean isBlocked, boolean canDm, boolean requested) {
        this.isFollowing = isFollowing;
        this.isMuted = isMuted;
        this.isBlocked = isBlocked;
        this.canDm = canDm;
        this.requested = requested;
    }
}