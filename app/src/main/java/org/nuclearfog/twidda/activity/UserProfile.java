package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.UserAction;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.model.Relation;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_IMAGE;
import static org.nuclearfog.twidda.activity.MessageEditor.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.activity.ProfileEditor.KEY_USER_DATA;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.activity.TweetActivity.LINK_PATTERN;
import static org.nuclearfog.twidda.activity.TweetEditor.KEY_TWEETPOPUP_TEXT;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_ID;
import static org.nuclearfog.twidda.activity.UserDetail.KEY_USERDETAIL_MODE;
import static org.nuclearfog.twidda.activity.UserDetail.USERLIST_FOLLOWER;
import static org.nuclearfog.twidda.activity.UserDetail.USERLIST_FRIENDS;
import static org.nuclearfog.twidda.activity.UserLists.KEY_USERLIST_OWNER_ID;
import static org.nuclearfog.twidda.backend.UserAction.Action.ACTION_BLOCK;
import static org.nuclearfog.twidda.backend.UserAction.Action.ACTION_FOLLOW;
import static org.nuclearfog.twidda.backend.UserAction.Action.ACTION_MUTE;
import static org.nuclearfog.twidda.backend.UserAction.Action.ACTION_UNBLOCK;
import static org.nuclearfog.twidda.backend.UserAction.Action.ACTION_UNFOLLOW;
import static org.nuclearfog.twidda.backend.UserAction.Action.ACTION_UNMUTE;
import static org.nuclearfog.twidda.backend.UserAction.Action.PROFILE_DB;
import static org.nuclearfog.twidda.backend.UserAction.Action.PROFILE_lOAD;
import static org.nuclearfog.twidda.database.GlobalSettings.PROFILE_IMG_HIGH_RES;
import static org.nuclearfog.twidda.fragment.UserFragment.KEY_USER_UPDATE;
import static org.nuclearfog.twidda.fragment.UserFragment.RETURN_USER_UPDATED;

/**
 * Activity class for user profile page
 *
 * @author nuclearfog
 */
public class UserProfile extends AppCompatActivity implements OnClickListener, OnTagClickListener,
        OnTabSelectedListener, OnConfirmListener, Callback {

    /**
     * Key for the user ID
     */
    public static final String KEY_PROFILE_ID = "profile_id";

    /**
     * key for user object
     */
    public static final String KEY_PROFILE_DATA = "profile_data";

    /**
     * key to prevent this activity to reload profile information as they are up to date
     */
    public static final String KEY_PROFILE_DISABLE_RELOAD = "profile_no_reload";

    /**
     * key when profile data changes
     */
    public static final String RETURN_PROFILE_DATA = "profile-update";

    /**
     * request code for {@link ProfileEditor}
     */
    public static final int REQUEST_PROFILE_CHANGED = 0x322F;

    /**
     * return code if {@link ProfileEditor} changed profile information
     */
    public static final int RETURN_PROFILE_CHANGED = 0xF5C0E570;

    /**
     * background color transparency mask for TextView backgrounds
     */
    private static final int TEXT_TRANSPARENCY = 0xafffffff;

    /**
     * background color transparency mask for toolbar background
     */
    public static final int TOOLBAR_TRANSPARENCY = 0x5fffffff;

    private FragmentAdapter adapter;
    private GlobalSettings settings;
    private UserAction profileAsync;

    private TextView[] tabTweetCount;
    private TextView user_location, user_createdAt, user_website, user_bio, follow_back, username, screenName;
    private ImageView profileImage, bannerImage, toolbarBackground;
    private Button following, follower;
    private ViewPager tabPages;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private Dialog unfollowConfirm, blockConfirm, muteConfirm;

    @Nullable
    private Relation relation;
    @Nullable
    private User user;


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_profile);
        View root = findViewById(R.id.user_view);
        ConstraintLayout profileView = findViewById(R.id.profile_content);
        toolbar = profileView.findViewById(R.id.profile_toolbar);
        user_bio = profileView.findViewById(R.id.bio);
        following = profileView.findViewById(R.id.following);
        follower = profileView.findViewById(R.id.follower);
        user_website = profileView.findViewById(R.id.links);
        profileImage = profileView.findViewById(R.id.profile_img);
        bannerImage = profileView.findViewById(R.id.profile_banner);
        toolbarBackground = profileView.findViewById(R.id.profile_toolbar_background);
        username = profileView.findViewById(R.id.profile_username);
        screenName = profileView.findViewById(R.id.profile_screenname);
        user_location = profileView.findViewById(R.id.location);
        user_createdAt = profileView.findViewById(R.id.profile_date);
        follow_back = profileView.findViewById(R.id.follow_back);
        tabLayout = findViewById(R.id.profile_tab);
        tabPages = findViewById(R.id.profile_pager);

        settings = GlobalSettings.getInstance(this);
        if (!settings.toolbarOverlapEnabled()) {
            ConstraintSet constraints = new ConstraintSet();
            constraints.clone(profileView);
            constraints.connect(R.id.profile_banner, ConstraintSet.TOP, R.id.profile_toolbar, ConstraintSet.BOTTOM);
            constraints.applyTo(profileView);
        }
        following.setCompoundDrawablesWithIntrinsicBounds(R.drawable.following, 0, 0, 0);
        follower.setCompoundDrawablesWithIntrinsicBounds(R.drawable.follower, 0, 0, 0);
        user_createdAt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.calendar, 0, 0, 0);
        user_location.setCompoundDrawablesWithIntrinsicBounds(R.drawable.userlocation, 0, 0, 0);
        user_website.setCompoundDrawablesWithIntrinsicBounds(R.drawable.link, 0, 0, 0);
        follow_back.setCompoundDrawablesWithIntrinsicBounds(R.drawable.followback, 0, 0, 0);
        toolbar.setBackgroundColor(settings.getBackgroundColor() & TOOLBAR_TRANSPARENCY);
        username.setBackgroundColor(settings.getBackgroundColor() & TEXT_TRANSPARENCY);
        follow_back.setBackgroundColor(settings.getBackgroundColor() & TEXT_TRANSPARENCY);
        user_bio.setMovementMethod(LinkAndScrollMovement.getInstance());
        user_bio.setLinkTextColor(settings.getHighlightColor());
        AppStyles.setTheme(settings, root);
        user_website.setTextColor(settings.getHighlightColor());
        tabLayout.setBackgroundColor(Color.TRANSPARENT);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        adapter = new FragmentAdapter(getSupportFragmentManager());
        tabPages.setAdapter(adapter);
        tabPages.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(tabPages);

        unfollowConfirm = new ConfirmDialog(this, DialogType.PROFILE_UNFOLLOW, this);
        blockConfirm = new ConfirmDialog(this, DialogType.PROFILE_BLOCK, this);
        muteConfirm = new ConfirmDialog(this, DialogType.PROFILE_MUTE, this);

        Intent i = getIntent();
        user = (User) i.getSerializableExtra(KEY_PROFILE_DATA);
        if (user != null) {
            adapter.setupProfilePage(user.getId());
        } else {
            long userId = i.getLongExtra(KEY_PROFILE_ID, 0);
            adapter.setupProfilePage(userId);
        }
        tabTweetCount = AppStyles.setTabIconsWithText(tabLayout, settings, R.array.profile_tab_icons);

        tabLayout.addOnTabSelectedListener(this);
        following.setOnClickListener(this);
        follower.setOnClickListener(this);
        profileImage.setOnClickListener(this);
        bannerImage.setOnClickListener(this);
        user_website.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (profileAsync == null) {
            Intent data = getIntent();
            if (user == null) {
                long userId = data.getLongExtra(KEY_PROFILE_ID, 0);
                profileAsync = new UserAction(this, userId);
                profileAsync.execute(PROFILE_DB);
            } else {
                setUser(user);
                if (!data.getBooleanExtra(KEY_PROFILE_DISABLE_RELOAD, false)) {
                    profileAsync = new UserAction(this, user.getId());
                    profileAsync.execute(PROFILE_lOAD);
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (profileAsync != null && profileAsync.getStatus() == RUNNING)
            profileAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int reqCode, int returnCode, @Nullable Intent i) {
        if (i != null && reqCode == REQUEST_PROFILE_CHANGED) {
            if (returnCode == RETURN_PROFILE_CHANGED) {
                Object data = i.getSerializableExtra(RETURN_PROFILE_DATA);
                if (data instanceof User) {
                    // remove blur background
                    toolbarBackground.setImageResource(0);
                    // re initialize updated user
                    setUser((User) data);
                    adapter.notifySettingsChanged();
                }
            }
        }
        super.onActivityResult(reqCode, returnCode, i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.profile, m);
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        if (user != null) {
            if (user.followRequested()) {
                MenuItem followIcon = m.findItem(R.id.profile_follow);
                AppStyles.setMenuItemColor(followIcon, settings.getFollowPendingColor());
                followIcon.setTitle(R.string.menu_follow_requested);
            }
            if (user.isCurrentUser() || !user.isLocked()) {
                MenuItem listItem = m.findItem(R.id.profile_lists);
                listItem.setVisible(true);
            }
            if (user.isCurrentUser()) {
                MenuItem dmIcon = m.findItem(R.id.profile_message);
                MenuItem setting = m.findItem(R.id.profile_settings);
                dmIcon.setVisible(true);
                setting.setVisible(true);
            } else {
                MenuItem followIcon = m.findItem(R.id.profile_follow);
                MenuItem blockIcon = m.findItem(R.id.profile_block);
                MenuItem muteIcon = m.findItem(R.id.profile_mute);
                followIcon.setVisible(true);
                blockIcon.setVisible(true);
                muteIcon.setVisible(true);
            }
        }
        if (relation != null) {
            if (relation.isFriend()) {
                MenuItem followIcon = m.findItem(R.id.profile_follow);
                MenuItem listItem = m.findItem(R.id.profile_lists);
                AppStyles.setMenuItemColor(followIcon, settings.getFollowIconColor());
                followIcon.setTitle(R.string.menu_user_unfollow);
                listItem.setVisible(true);
            }
            if (relation.isBlocked()) {
                MenuItem blockIcon = m.findItem(R.id.profile_block);
                blockIcon.setTitle(R.string.menu_user_unblock);
            }
            if (relation.isMuted()) {
                MenuItem muteIcon = m.findItem(R.id.profile_mute);
                muteIcon.setTitle(R.string.menu_unmute_user);
            }
            if (relation.canDm()) {
                MenuItem dmIcon = m.findItem(R.id.profile_message);
                dmIcon.setVisible(true);
            }
            if (relation.isFollower()) {
                follow_back.setVisibility(VISIBLE);
            }
        }
        return super.onPrepareOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (user != null) {
            // write tweet
            if (item.getItemId() == R.id.profile_tweet) {
                Intent tweet = new Intent(this, TweetEditor.class);
                if (!user.isCurrentUser()) {
                    // add username to tweet
                    String tweetPrefix = user.getScreenname() + " ";
                    tweet.putExtra(KEY_TWEETPOPUP_TEXT, tweetPrefix);
                }
                startActivity(tweet);
            }
            // follow / unfollow user
            else if (item.getItemId() == R.id.profile_follow) {
                if (relation != null) {
                    if (!relation.isFriend()) {
                        profileAsync = new UserAction(this, user.getId());
                        profileAsync.execute(ACTION_FOLLOW);
                    } else if (!unfollowConfirm.isShowing()) {
                        unfollowConfirm.show();
                    }
                }
            }
            // mute user
            else if (item.getItemId() == R.id.profile_mute) {
                if (relation != null) {
                    if (relation.isMuted()) {
                        profileAsync = new UserAction(this, user.getId());
                        profileAsync.execute(ACTION_UNMUTE);
                    } else if (!muteConfirm.isShowing()) {
                        muteConfirm.show();
                    }
                }
            }
            // block user
            else if (item.getItemId() == R.id.profile_block) {
                if (relation != null) {
                    if (relation.isBlocked()) {
                        profileAsync = new UserAction(this, user.getId());
                        profileAsync.execute(ACTION_UNBLOCK);
                    } else if (!blockConfirm.isShowing()) {
                        blockConfirm.show();
                    }
                }
            }
            // open profile editor
            else if (item.getItemId() == R.id.profile_settings) {
                Intent editProfile = new Intent(this, ProfileEditor.class);
                editProfile.putExtra(KEY_USER_DATA, user);
                startActivityForResult(editProfile, REQUEST_PROFILE_CHANGED);
            }
            // open direct message
            else if (item.getItemId() == R.id.profile_message) {
                Intent dmPage;
                if (user.isCurrentUser()) {
                    dmPage = new Intent(this, DirectMessage.class);
                } else {
                    dmPage = new Intent(this, MessageEditor.class);
                    dmPage.putExtra(KEY_DM_PREFIX, user.getScreenname());
                }
                startActivity(dmPage);
            }
            // open users list
            else if (item.getItemId() == R.id.profile_lists) {
                Intent listPage = new Intent(this, UserLists.class);
                listPage.putExtra(KEY_USERLIST_OWNER_ID, user.getId());
                startActivity(listPage);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() > 0) {
            tabPages.setCurrentItem(0);
        } else {
            Intent returnData = new Intent();
            returnData.putExtra(KEY_USER_UPDATE, user);
            setResult(RETURN_USER_UPDATED, returnData);
            super.onBackPressed();
        }
    }


    @Override
    public void onTagClick(String text) {
        Intent intent = new Intent(this, SearchPage.class);
        intent.putExtra(KEY_SEARCH_QUERY, text);
        startActivity(intent);
    }


    @Override
    public void onLinkClick(final String tag) {
        String shortLink;
        // remove query from link if exists
        int cut = tag.indexOf('?');
        if (cut > 0) {
            shortLink = tag.substring(0, cut);
        } else {
            shortLink = tag;
        }
        // link points to a tweet
        if (LINK_PATTERN.matcher(shortLink).matches()) {
            try {
                String name = shortLink.substring(20, shortLink.indexOf('/', 20));
                long id = Long.parseLong(shortLink.substring(shortLink.lastIndexOf('/') + 1));
                Intent intent = new Intent(this, TweetActivity.class);
                intent.putExtra(KEY_TWEET_ID, id);
                intent.putExtra(KEY_TWEET_NAME, name);
                startActivity(intent);
                return;
            } catch (Exception err) {
                err.printStackTrace();
                // open link in browser if an error occurs
            }
        }
        // open link in browser
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tag));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException err) {
            Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        // open following page
        if (v.getId() == R.id.following) {
            if (user != null && relation != null) {
                if (!user.isLocked() || user.isCurrentUser() || relation.isFriend()) {
                    Intent following = new Intent(this, UserDetail.class);
                    following.putExtra(KEY_USERDETAIL_ID, user.getId());
                    following.putExtra(KEY_USERDETAIL_MODE, USERLIST_FRIENDS);
                    startActivity(following);
                }
            }
        }
        // open follower page
        else if (v.getId() == R.id.follower) {
            if (user != null && relation != null) {
                if (!user.isLocked() || user.isCurrentUser() || relation.isFriend()) {
                    Intent follower = new Intent(this, UserDetail.class);
                    follower.putExtra(KEY_USERDETAIL_ID, user.getId());
                    follower.putExtra(KEY_USERDETAIL_MODE, USERLIST_FOLLOWER);
                    startActivity(follower);
                }
            }
        }
        // open link added to profile
        else if (v.getId() == R.id.links) {
            if (user != null && !user.getLink().isEmpty()) {
                String link = user.getLink();
                Intent browserIntent = new Intent(ACTION_VIEW, Uri.parse(link));
                try {
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException err) {
                    Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
                }
            }
        }
        // open profile image
        else if (v.getId() == R.id.profile_img) {
            if (user != null && user.hasProfileImage()) {
                String[] link = {user.getImageLink()};
                Intent mediaImage = new Intent(this, MediaViewer.class);
                mediaImage.putExtra(KEY_MEDIA_LINK, link);
                mediaImage.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMAGE);
                startActivity(mediaImage);
            }
        }
        // open banner image
        else if (v.getId() == R.id.profile_banner) {
            if (user != null && user.hasBannerImage()) {
                String[] link = {user.getBannerLink()};
                Intent mediaBanner = new Intent(this, MediaViewer.class);
                mediaBanner.putExtra(KEY_MEDIA_LINK, link);
                mediaBanner.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMAGE);
                startActivity(mediaBanner);
            }
        }
    }


    @Override
    public void onConfirm(DialogType type) {
        if (user != null) {
            profileAsync = new UserAction(this, user.getId());
            // confirmed unfollowing user
            if (type == DialogType.PROFILE_UNFOLLOW) {
                profileAsync.execute(ACTION_UNFOLLOW);
            }
            // confirmed blocking user
            else if (type == DialogType.PROFILE_BLOCK) {
                profileAsync.execute(ACTION_BLOCK);
            }
            // confirmed muting user
            else if (type == DialogType.PROFILE_MUTE) {
                profileAsync.execute(ACTION_MUTE);
            }
        }
    }


    @Override
    public void onTabSelected(Tab tab) {
    }


    @Override
    public void onTabUnselected(Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onSuccess() {
        // setup toolbar background
        if (settings.toolbarOverlapEnabled()) {
            AppStyles.setToolbarBackground(UserProfile.this, bannerImage, toolbarBackground);
        }
    }


    @Override
    public void onError(Exception e) {
    }


    /**
     * Set User Information
     *
     * @param user User data
     */
    public void setUser(User user) {
        this.user = user;
        NumberFormat formatter = NumberFormat.getIntegerInstance();
        Spanned bio = Tagger.makeTextWithLinks(user.getBio(), settings.getHighlightColor(), this);

        tabTweetCount[0].setText(formatter.format(user.getTweetCount()));
        tabTweetCount[1].setText(formatter.format(user.getFavorCount()));
        following.setText(formatter.format(user.getFollowing()));
        follower.setText(formatter.format(user.getFollower()));
        username.setText(user.getUsername());
        screenName.setText(user.getScreenname());

        if (user_createdAt.getVisibility() != VISIBLE) {
            String date = SimpleDateFormat.getDateTimeInstance().format(user.getCreatedAt());
            user_createdAt.setVisibility(VISIBLE);
            user_createdAt.setText(date);
        }
        if (user.isVerified()) {
            username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
            AppStyles.setDrawableColor(username, settings.getIconColor());
        } else {
            username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (user.isLocked()) {
            screenName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
            AppStyles.setDrawableColor(screenName, settings.getIconColor());
        } else {
            screenName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (!user.getLocation().isEmpty()) {
            user_location.setText(user.getLocation());
            user_location.setVisibility(VISIBLE);
        } else {
            user_location.setVisibility(GONE);
        }
        if (!user.getBio().isEmpty()) {
            user_bio.setVisibility(VISIBLE);
            user_bio.setText(bio);
        } else {
            user_bio.setVisibility(GONE);
        }
        if (!user.getLink().isEmpty()) {
            String link = user.getLink();
            if (link.startsWith("http://"))
                user_website.setText(link.substring(7));
            else if (link.startsWith("https://"))
                user_website.setText(link.substring(8));
            else
                user_website.setText(link);
            user_website.setVisibility(VISIBLE);
        } else {
            user_website.setVisibility(GONE);
        }
        if (settings.imagesEnabled()) {
            if (user.hasBannerImage()) {
                String bannerLink = user.getBannerLink() + settings.getBannerSuffix();
                Picasso.get().load(bannerLink).error(R.drawable.no_banner).into(bannerImage, this);
            } else {
                bannerImage.setImageResource(0);
            }
            if (user.hasProfileImage()) {
                String imgLink = user.getImageLink();
                if (!user.hasDefaultProfileImage())
                    imgLink += PROFILE_IMG_HIGH_RES;
                Picasso.get().load(imgLink).transform(new RoundedCornersTransformation(5, 0)).error(R.drawable.no_image).into(profileImage);
            } else {
                profileImage.setImageResource(0);
            }
        }
        if (following.getVisibility() != VISIBLE) {
            following.setVisibility(VISIBLE);
            follower.setVisibility(VISIBLE);
        }
    }

    /**
     * sets user relation information and checks for status changes
     *
     * @param relation relation to an user
     */
    public void onAction(Relation relation) {
        if (this.relation != null) {
            // check if block status changed
            if (relation.isBlocked() != this.relation.isBlocked()) {
                if (relation.isBlocked()) {
                    Toast.makeText(this, R.string.info_user_blocked, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.info_user_unblocked, Toast.LENGTH_SHORT).show();
                }
            }
            // check if following status changed
            else if (relation.isFriend() != this.relation.isFriend()) {
                if (relation.isFriend()) {
                    Toast.makeText(this, R.string.info_followed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.info_unfollowed, Toast.LENGTH_SHORT).show();
                }
            }
            // check if mute status changed
            else if (relation.isMuted() != this.relation.isMuted()) {
                if (relation.isMuted()) {
                    Toast.makeText(this, R.string.info_user_muted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.info_user_unmuted, Toast.LENGTH_SHORT).show();
                }
            }
        }
        this.relation = relation;
        invalidateOptionsMenu();
    }

    /**
     * called if an error occurs
     *
     * @param err Engine Exception
     */
    public void onError(@Nullable EngineException err) {
        ErrorHandler.handleFailure(this, err);
        if (user == null || (err != null && err.resourceNotFound())) {
            finish();
        }
    }
}