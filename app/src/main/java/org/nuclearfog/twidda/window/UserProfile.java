package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType;
import org.nuclearfog.twidda.backend.ProfileLoader;
import org.nuclearfog.twidda.backend.items.UserBundle;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.RUNNING;
import static android.view.Gravity.CENTER;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.backend.ProfileLoader.Mode.LDR_PROFILE;
import static org.nuclearfog.twidda.window.MessagePopup.KEY_DM_ADDITION;
import static org.nuclearfog.twidda.window.SearchPage.KEY_SEARCH;
import static org.nuclearfog.twidda.window.TweetPopup.KEY_TWEETPOPUP_ADDITION;
import static org.nuclearfog.twidda.window.UserDetail.KEY_USERLIST_ID;
import static org.nuclearfog.twidda.window.UserDetail.KEY_USERLIST_MODE;
import static org.nuclearfog.twidda.window.UserDetail.UserType.FOLLOWERS;
import static org.nuclearfog.twidda.window.UserDetail.UserType.FRIENDS;


public class UserProfile extends AppCompatActivity implements OnClickListener,
        OnTouchListener, OnTagClickListener, OnTabSelectedListener {

    public static final String KEY_PROFILE_ID = "userID";
    private static final int REQUEST_PROFILE_CHANGED = 1;
    public static final int RETURN_PROFILE_CHANGED = 2;

    private ProfileLoader profileAsync;
    private FragmentAdapter adapter;
    private TextView tweetTabTxt, favorTabTxt;
    private ViewPager pager;
    private View follow_back;

    @Nullable
    private UserBundle userBundle;
    private long userId;

    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_profile);

        Bundle param = getIntent().getExtras();
        if (param != null && param.containsKey(KEY_PROFILE_ID)) {
            userId = param.getLong(KEY_PROFILE_ID);
        } else if (BuildConfig.DEBUG) {
            throw new AssertionError();
        }

        Toolbar tool = findViewById(R.id.profile_toolbar);
        TabLayout tab = findViewById(R.id.profile_tab);
        TextView bioTxt = findViewById(R.id.bio);
        Button following = findViewById(R.id.following);
        Button follower = findViewById(R.id.follower);
        ViewGroup root = findViewById(R.id.user_view);
        TextView lnkTxt = findViewById(R.id.links);
        follow_back = findViewById(R.id.follow_back);
        pager = findViewById(R.id.profile_pager);
        tweetTabTxt = new TextView(getApplicationContext());
        favorTabTxt = new TextView(getApplicationContext());

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        GlobalSettings settings = GlobalSettings.getInstance(this);

        bioTxt.setMovementMethod(LinkMovementMethod.getInstance());
        tab.setSelectedTabIndicatorColor(settings.getHighlightColor());
        bioTxt.setLinkTextColor(settings.getHighlightColor());
        lnkTxt.setLinkTextColor(settings.getHighlightColor());
        root.setBackgroundColor(settings.getBackgroundColor());
        tweetTabTxt.setTextColor(settings.getFontColor());
        favorTabTxt.setTextColor(settings.getFontColor());
        tweetTabTxt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home_profile, 0, 0);
        favorTabTxt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorite_profile, 0, 0);
        tweetTabTxt.setGravity(CENTER);
        favorTabTxt.setGravity(CENTER);
        tweetTabTxt.setTextSize(10);
        favorTabTxt.setTextSize(10);

        adapter = new FragmentAdapter(getSupportFragmentManager(), AdapterType.PROFILE_TAB, userId, "");
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(adapter);
        tab.setupWithViewPager(pager);
        Tab tweetTab = tab.getTabAt(0);
        Tab favorTab = tab.getTabAt(1);
        if (tweetTab != null && favorTab != null) {
            tweetTab.setCustomView(tweetTabTxt);
            favorTab.setCustomView(favorTabTxt);
        }
        tab.addOnTabSelectedListener(this);
        following.setOnClickListener(this);
        follower.setOnClickListener(this);
        bioTxt.setOnTouchListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (profileAsync == null) {
            profileAsync = new ProfileLoader(this, LDR_PROFILE);
            profileAsync.execute(userId);
        }
    }


    @Override
    protected void onDestroy() {
        if (profileAsync != null && profileAsync.getStatus() == RUNNING)
            profileAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, Intent i) {
        if (reqCode == REQUEST_PROFILE_CHANGED && returnCode == RETURN_PROFILE_CHANGED) {
            profileAsync = null;
            adapter.clearData();
        }
        super.onActivityResult(reqCode, returnCode, i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.profile, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        if (userBundle != null) {
            MenuItem dmIcon = m.findItem(R.id.profile_message);
            MenuItem setting = m.findItem(R.id.profile_settings);
            if (userBundle.getProperties().isHome()) {
                dmIcon.setVisible(true);
                setting.setVisible(true);
            } else {
                MenuItem followIcon = m.findItem(R.id.profile_follow);
                MenuItem blockIcon = m.findItem(R.id.profile_block);
                MenuItem muteIcon = m.findItem(R.id.profile_mute);
                followIcon.setVisible(true);
                blockIcon.setVisible(true);
                muteIcon.setVisible(true);

                if (userBundle.getProperties().isFriend()) {
                    followIcon.setIcon(R.drawable.follow_enabled);
                    followIcon.setTitle(R.string.unfollow);
                } else if (userBundle.getUser().followRequested()) {
                    followIcon.setIcon(R.drawable.follow_requested);
                    followIcon.setTitle(R.string.follow_requested);
                } else {
                    followIcon.setIcon(R.drawable.follow);
                    followIcon.setTitle(R.string.follow);
                }
                if (userBundle.getProperties().isBlocked()) {
                    blockIcon.setTitle(R.string.unblock);
                    followIcon.setVisible(false);
                } else {
                    blockIcon.setTitle(R.string.block);
                    followIcon.setVisible(true);
                }
                if (userBundle.getProperties().isMuted())
                    muteIcon.setTitle(R.string.unmute);
                else
                    muteIcon.setTitle(R.string.mute);
                if (userBundle.getProperties().canDm())
                    dmIcon.setVisible(true);
                if (userBundle.getProperties().isFollower())
                    follow_back.setVisibility(VISIBLE);
            }
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (profileAsync != null && profileAsync.getStatus() != RUNNING && userBundle != null) {
            switch (item.getItemId()) {
                case R.id.profile_tweet:
                    Intent tweet = new Intent(this, TweetPopup.class);
                    if (!userBundle.getProperties().isHome())
                        tweet.putExtra(KEY_TWEETPOPUP_ADDITION, userBundle.getUser().getScreenname());
                    startActivity(tweet);
                    break;

                case R.id.profile_follow:
                    profileAsync = new ProfileLoader(this, ProfileLoader.Mode.ACTION_FOLLOW);
                    if (!userBundle.getProperties().isFriend()) {
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
                    if (userBundle.getProperties().isBlocked()) {
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
                    Intent dmPage;
                    if (userBundle.getProperties().isHome()) {
                        dmPage = new Intent(this, DirectMessage.class);
                    } else {
                        dmPage = new Intent(this, MessagePopup.class);
                        dmPage.putExtra(KEY_DM_ADDITION, userBundle.getUser().getScreenname());
                    }
                    startActivity(dmPage);
                    break;

                case R.id.profile_settings:
                    Intent editProfile = new Intent(this, ProfileEdit.class);
                    startActivityForResult(editProfile, REQUEST_PROFILE_CHANGED);
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
        intent.putExtra(KEY_SEARCH, text);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        if (userBundle != null) {
            switch (v.getId()) {
                case R.id.following:
                    if (!userBundle.getUser().isLocked() || userBundle.getProperties().isFriend()) {
                        Intent following = new Intent(this, UserDetail.class);
                        following.putExtra(KEY_USERLIST_ID, userId);
                        following.putExtra(KEY_USERLIST_MODE, FRIENDS);
                        startActivity(following);
                    }
                    break;

                case R.id.follower:
                    if (!userBundle.getUser().isLocked() || userBundle.getProperties().isFriend()) {
                        Intent follower = new Intent(this, UserDetail.class);
                        follower.putExtra(KEY_USERLIST_ID, userId);
                        follower.putExtra(KEY_USERLIST_MODE, FOLLOWERS);
                        startActivity(follower);
                    }
                    break;

                case R.id.links:
                    ConnectivityManager mConnect = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    if (mConnect.getActiveNetworkInfo() != null && mConnect.getActiveNetworkInfo().isConnected()) {
                        Intent browserIntent = new Intent(ACTION_VIEW);
                        String link = userBundle.getUser().getLink();
                        browserIntent.setData(Uri.parse(link));
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(this, R.string.connection_failed, LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case ACTION_DOWN:
                v.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case ACTION_UP:
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return v.performClick();
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        tabIndex = tab.getPosition();
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }


    public void setTweetCount(int tweets, int favors) {
        NumberFormat formatter = NumberFormat.getIntegerInstance();
        tweetTabTxt.setText(formatter.format(tweets));
        favorTabTxt.setText(formatter.format(favors));
    }


    public void setConnection(UserBundle mUser) {
        this.userBundle = mUser;
        invalidateOptionsMenu();
    }
}