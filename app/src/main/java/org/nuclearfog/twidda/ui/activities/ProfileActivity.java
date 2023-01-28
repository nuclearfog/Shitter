package org.nuclearfog.twidda.ui.activities;

import static android.content.Intent.ACTION_VIEW;
import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.ui.activities.MessageEditor.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.KEY_STATUS_ID;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.KEY_STATUS_NAME;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.TWITTER_LINK_PATTERN;
import static org.nuclearfog.twidda.ui.activities.StatusEditor.KEY_STATUS_EDITOR_TEXT;
import static org.nuclearfog.twidda.ui.activities.UserlistsActivity.KEY_USERLIST_OWNER_ID;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.KEY_USERS_ID;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.KEY_USERS_MODE;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.USERS_FOLLOWER;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.USERS_FRIENDS;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.USERS_REQUESTS;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Transformation;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.UserAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

import java.text.SimpleDateFormat;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Activity class for user profile page
 *
 * @author nuclearfog
 */
public class ProfileActivity extends AppCompatActivity implements OnClickListener, OnTagClickListener,
		OnTabSelectedListener, OnConfirmListener, Callback {

	/**
	 * Key for the user ID
	 * value type is Long
	 */
	public static final String KEY_PROFILE_ID = "profile_id";

	/**
	 * key for user object
	 * value type is {@link User}
	 */
	public static final String KEY_PROFILE_USER = "profile_user";

	/**
	 * key for relation object
	 * value type is {@link Relation}
	 */
	private static final String KEY_PROFILE_RELATION = "profile_relation";

	/**
	 * key to prevent this activity to reload profile information as they are up to date
	 * value type is Boolean
	 */
	public static final String KEY_PROFILE_DISABLE_RELOAD = "profile_no_reload";

	/**
	 * key to send updated user data
	 * value type is {@link User}
	 */
	public static final String KEY_USER_UPDATE = "user_update";

	/**
	 * request code for {@link ProfileEditor}
	 */
	public static final int REQUEST_PROFILE_CHANGED = 0x322F;

	/**
	 * Return code to update user information
	 */
	public static final int RETURN_USER_UPDATED = 0x9996498C;

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
	private Picasso picasso;

	private ConfirmDialog confirmDialog;

	private TextView[] tabIndicator;
	private TextView user_location, user_createdAt, user_website, user_bio, follow_back, username, screenName;
	private ImageView profileImage, bannerImage, toolbarBackground;
	private Button following, follower;
	private ViewPager tabPages;
	private TabLayout tabLayout;
	private Toolbar toolbar;

	@Nullable
	private Relation relation;
	@Nullable
	private User user;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_profile);
		ViewGroup root = findViewById(R.id.user_view);
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

		picasso = PicassoBuilder.get(this);
		settings = GlobalSettings.getInstance(this);
		if (!settings.toolbarOverlapEnabled()) {
			ConstraintSet constraints = new ConstraintSet();
			constraints.clone(profileView);
			constraints.connect(R.id.profile_banner, ConstraintSet.TOP, R.id.profile_toolbar, ConstraintSet.BOTTOM);
			constraints.applyTo(profileView);
		}
		following.setCompoundDrawablesWithIntrinsicBounds(R.drawable.following, 0, 0, 0);
		follower.setCompoundDrawablesWithIntrinsicBounds(R.drawable.follower, 0, 0, 0);
		user_createdAt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.date, 0, 0, 0);
		user_location.setCompoundDrawablesWithIntrinsicBounds(R.drawable.location, 0, 0, 0);
		user_website.setCompoundDrawablesWithIntrinsicBounds(R.drawable.link, 0, 0, 0);
		follow_back.setCompoundDrawablesWithIntrinsicBounds(R.drawable.back, 0, 0, 0);
		toolbar.setBackgroundColor(settings.getBackgroundColor() & TOOLBAR_TRANSPARENCY);
		username.setBackgroundColor(settings.getBackgroundColor() & TEXT_TRANSPARENCY);
		follow_back.setBackgroundColor(settings.getBackgroundColor() & TEXT_TRANSPARENCY);
		user_bio.setMovementMethod(LinkAndScrollMovement.getInstance());
		user_bio.setLinkTextColor(settings.getHighlightColor());
		AppStyles.setTheme(root);
		user_website.setTextColor(settings.getHighlightColor());
		tabLayout.setBackgroundColor(Color.TRANSPARENT);

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		adapter = new FragmentAdapter(this, getSupportFragmentManager());
		tabPages.setAdapter(adapter);
		tabPages.setOffscreenPageLimit(2);
		tabLayout.setupWithViewPager(tabPages);
		confirmDialog = new ConfirmDialog(this);

		Intent i = getIntent();
		Object o = i.getSerializableExtra(KEY_PROFILE_USER);
		Account login = settings.getLogin();
		boolean enableFavs = login.getApiType() == Account.API_TWITTER_1 || login.getApiType() == Account.API_TWITTER_2;
		if (o instanceof User) {
			user = (User) o;
			adapter.setupProfilePage(user.getId(), enableFavs || login.getId() == user.getId());
		} else {
			long userId = i.getLongExtra(KEY_PROFILE_ID, 0);
			adapter.setupProfilePage(userId, enableFavs || login.getId() == userId);
		}
		if (settings.likeEnabled()) {
			tabIndicator = AppStyles.setTabIconsWithText(tabLayout, settings, R.array.profile_tab_icons_like);
		} else {
			tabIndicator = AppStyles.setTabIconsWithText(tabLayout, settings, R.array.profile_tab_icons);
		}
		tabLayout.addOnTabSelectedListener(this);
		following.setOnClickListener(this);
		follower.setOnClickListener(this);
		profileImage.setOnClickListener(this);
		bannerImage.setOnClickListener(this);
		user_website.setOnClickListener(this);
		confirmDialog.setConfirmListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (profileAsync == null) {
			Intent data = getIntent();
			if (user == null) {
				long userId = data.getLongExtra(KEY_PROFILE_ID, 0);
				profileAsync = new UserAction(this, UserAction.PROFILE_DB, userId);
				profileAsync.execute();
			} else if (relation == null) {
				setUser(user);
				if (!data.getBooleanExtra(KEY_PROFILE_DISABLE_RELOAD, false)) {
					profileAsync = new UserAction(this, UserAction.PROFILE_lOAD, user.getId());
					profileAsync.execute();
				}
			}
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_PROFILE_USER, user);
		outState.putSerializable(KEY_PROFILE_RELATION, relation);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		Object data = savedInstanceState.getSerializable(KEY_PROFILE_USER);
		if (data instanceof User)
			user = (User) data;
		data = savedInstanceState.getSerializable(KEY_PROFILE_RELATION);
		if (data instanceof Relation)
			relation = (Relation) data;
		super.onRestoreInstanceState(savedInstanceState);
	}


	@Override
	protected void onDestroy() {
		if (profileAsync != null && profileAsync.getStatus() == RUNNING)
			profileAsync.cancel(true);
		super.onDestroy();
	}


	@Override
	public void onActivityResult(int reqCode, int returnCode, @Nullable Intent i) {
		super.onActivityResult(reqCode, returnCode, i);
		if (i != null && reqCode == REQUEST_PROFILE_CHANGED) {
			if (returnCode == ProfileEditor.RETURN_PROFILE_CHANGED) {
				Object data = i.getSerializableExtra(ProfileEditor.KEY_UPDATED_PROFILE);
				if (data instanceof User) {
					// remove blur background
					toolbarBackground.setImageResource(0);
					// re initialize updated user
					setUser((User) data);
					adapter.notifySettingsChanged();
				}
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.profile, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onPrepareOptionsMenu(@NonNull Menu m) {
		if (user != null) {
			MenuItem listItem = m.findItem(R.id.profile_lists);
			switch (settings.getLogin().getApiType()) {
				case Account.API_TWITTER_1:
				case Account.API_TWITTER_2:
					if (user.isCurrentUser()) {
						MenuItem requestItem = m.findItem(R.id.profile_requests);
						requestItem.setVisible(true);
						listItem.setVisible(true);
					} else if (!user.isProtected()) {
						listItem.setVisible(true);
					}
					break;

				case Account.API_MASTODON:
					if (user.isCurrentUser()) {
						listItem.setVisible(true);
					}
					break;
			}
			if (user.followRequested()) {
				MenuItem followIcon = m.findItem(R.id.profile_follow);
				AppStyles.setMenuItemColor(followIcon, settings.getFollowPendingColor());
				followIcon.setTitle(R.string.menu_follow_requested);
			}
			if (user.isCurrentUser()) {
				MenuItem setting = m.findItem(R.id.profile_settings);
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
			if (relation.isFollowing()) {
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
		// write status
		if (item.getItemId() == R.id.profile_post) {
			Intent intent = new Intent(this, StatusEditor.class);
			if (user != null && !user.isCurrentUser()) {
				// add username to status
				String prefix = user.getScreenname() + " ";
				intent.putExtra(KEY_STATUS_EDITOR_TEXT, prefix);
			}
			startActivity(intent);
		}
		// follow / unfollow user
		else if (item.getItemId() == R.id.profile_follow) {
			if (relation != null && user != null) {
				if (!relation.isFollowing() && (profileAsync == null || profileAsync.getStatus() != RUNNING)) {
					profileAsync = new UserAction(this, UserAction.ACTION_FOLLOW, user.getId());
					profileAsync.execute();
				} else {
					confirmDialog.show(ConfirmDialog.PROFILE_UNFOLLOW);
				}
			}
		}
		// mute user
		else if (item.getItemId() == R.id.profile_mute) {
			if (relation != null && user != null) {
				if (relation.isMuted() && (profileAsync == null || profileAsync.getStatus() != RUNNING)) {
					profileAsync = new UserAction(this, UserAction.ACTION_UNMUTE, user.getId());
					profileAsync.execute();
				} else {
					confirmDialog.show(ConfirmDialog.PROFILE_MUTE);
				}
			}
		}
		// block user
		else if (item.getItemId() == R.id.profile_block) {
			if (relation != null && user != null) {
				if (relation.isBlocked() && (profileAsync == null || profileAsync.getStatus() != RUNNING)) {
					profileAsync = new UserAction(this, UserAction.ACTION_UNBLOCK, user.getId());
					profileAsync.execute();
				} else {
					confirmDialog.show(ConfirmDialog.PROFILE_BLOCK);
				}
			}
		}
		// open profile editor
		else if (item.getItemId() == R.id.profile_settings) {
			Intent editProfile = new Intent(this, ProfileEditor.class);
			editProfile.putExtra(ProfileEditor.KEY_PROFILE_DATA, user);
			startActivityForResult(editProfile, REQUEST_PROFILE_CHANGED);
		}
		// open direct message
		else if (item.getItemId() == R.id.profile_message) {
			Intent intent = new Intent(this, MessageEditor.class);
			if (user != null && !user.isCurrentUser())
				intent.putExtra(KEY_DM_PREFIX, user.getScreenname());
			startActivity(intent);
		}
		// open users list
		else if (item.getItemId() == R.id.profile_lists) {
			if (user != null) {
				Intent intent = new Intent(this, UserlistsActivity.class);
				intent.putExtra(KEY_USERLIST_OWNER_ID, user.getId());
				startActivity(intent);
			}
		}
		// open request list
		else if (item.getItemId() == R.id.profile_requests) {
			Intent usersIntent = new Intent(this, UsersActivity.class);
			usersIntent.putExtra(KEY_USERS_MODE, USERS_REQUESTS);
			startActivity(usersIntent);
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
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra(KEY_SEARCH_QUERY, text);
		startActivity(intent);
	}


	@Override
	public void onLinkClick(String tag) {
		Uri link = Uri.parse(tag);
		// open status link
		if (TWITTER_LINK_PATTERN.matcher(link.getScheme() + "://" + link.getHost() + link.getPath()).matches()) {
			List<String> segments = link.getPathSegments();
			Intent intent = new Intent(this, StatusActivity.class);
			intent.putExtra(KEY_STATUS_ID, Long.parseLong(segments.get(2)));
			intent.putExtra(KEY_STATUS_NAME, segments.get(0));
			startActivity(intent);
		}
		// open link in browser
		else {
			Intent intent = new Intent(Intent.ACTION_VIEW, link);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(getApplicationContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
			}
		}
	}


	@Override
	public void onClick(View v) {
		if (user == null)
			return;
		// open following page
		if (v.getId() == R.id.following) {
			Intent intent = new Intent(this, UsersActivity.class);
			intent.putExtra(KEY_USERS_ID, user.getId());
			intent.putExtra(KEY_USERS_MODE, USERS_FRIENDS);
			switch (settings.getLogin().getApiType()) {
				case Account.API_TWITTER_1:
				case Account.API_TWITTER_2:
					if (!user.isProtected() || user.isCurrentUser() || (relation != null && relation.isFollowing())) {
						startActivity(intent);
					}
					break;

				case Account.API_MASTODON:
					startActivity(intent);
					break;
			}
		}
		// open follower page
		else if (v.getId() == R.id.follower) {
			Intent intent = new Intent(this, UsersActivity.class);
			intent.putExtra(KEY_USERS_ID, user.getId());
			intent.putExtra(KEY_USERS_MODE, USERS_FOLLOWER);
			switch (settings.getLogin().getApiType()) {
				case Account.API_TWITTER_1:
				case Account.API_TWITTER_2:
					if (!user.isProtected() || user.isCurrentUser() || (relation != null && relation.isFollowing())) {
						startActivity(intent);
					}
					break;

				case Account.API_MASTODON:
					startActivity(intent);
					break;
			}
		}
		// open link added to profile
		else if (v.getId() == R.id.links) {
			if (!user.getProfileUrl().isEmpty()) {
				String link = user.getProfileUrl();
				Intent intent = new Intent(ACTION_VIEW, Uri.parse(link));
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException err) {
					Toast.makeText(getApplicationContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
				}
			}
		}
		// open profile image
		else if (v.getId() == R.id.profile_img) {
			if (!user.getOriginalProfileImageUrl().isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.IMAGE_URI, Uri.parse(user.getOriginalProfileImageUrl()));
				startActivity(intent);
			}
		}
		// open banner image
		else if (v.getId() == R.id.profile_banner) {
			if (!user.getOriginalBannerImageUrl().isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.IMAGE_URI, Uri.parse(user.getOriginalBannerImageUrl()));
				startActivity(intent);
			}
		}
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
		if (user == null)
			return;
		// confirmed unfollowing user
		if (type == ConfirmDialog.PROFILE_UNFOLLOW) {
			profileAsync = new UserAction(this, UserAction.ACTION_UNFOLLOW, user.getId());
			profileAsync.execute();
		}
		// confirmed blocking user
		else if (type == ConfirmDialog.PROFILE_BLOCK) {
			profileAsync = new UserAction(this, UserAction.ACTION_BLOCK, user.getId());
			profileAsync.execute();
		}
		// confirmed muting user
		else if (type == ConfirmDialog.PROFILE_MUTE) {
			profileAsync = new UserAction(this, UserAction.ACTION_MUTE, user.getId());
			profileAsync.execute();
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
			AppStyles.setToolbarBackground(ProfileActivity.this, bannerImage, toolbarBackground);
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

		Spanned bio = Tagger.makeTextWithLinks(user.getDescription(), settings.getHighlightColor(), this);
		following.setText(StringTools.NUMBER_FORMAT.format(user.getFollowing()));
		follower.setText(StringTools.NUMBER_FORMAT.format(user.getFollower()));
		username.setText(user.getUsername());
		screenName.setText(user.getScreenname());
		if (user.getStatusCount() >= 0) {
			tabIndicator[0].setText(StringTools.NUMBER_FORMAT.format(user.getStatusCount()));
		} else {
			tabIndicator[0].setText("");
		}
		if (tabIndicator.length > 1) {
			if (user.getFavoriteCount() >= 0) {
				tabIndicator[1].setText(StringTools.NUMBER_FORMAT.format(user.getFavoriteCount()));
			} else {
				tabIndicator[1].setText("");
			}
		}
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
		if (user.isProtected()) {
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
		if (!user.getDescription().isEmpty()) {
			user_bio.setVisibility(VISIBLE);
			user_bio.setText(bio);
		} else {
			user_bio.setVisibility(GONE);
		}
		if (!user.getProfileUrl().isEmpty()) {
			String link = user.getProfileUrl();
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
			String profileImageUrl = user.getBannerImageThumbnailUrl();
			String bannerImageUrl = user.getProfileImageThumbnailUrl();
			if (!profileImageUrl.isEmpty()) {
				picasso.load(profileImageUrl).error(R.drawable.no_banner).into(bannerImage, this);
			} else {
				bannerImage.setImageResource(0);
				toolbarBackground.setImageResource(0);
			}
			if (!bannerImageUrl.isEmpty()) {
				Transformation roundCorner = new RoundedCornersTransformation(5, 0);
				picasso.load(bannerImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profileImage);
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
	public void onAction(@NonNull Relation relation) {
		if (this.relation != null) {
			// check if block status changed
			if (relation.isBlocked() != this.relation.isBlocked()) {
				if (relation.isBlocked()) {
					Toast.makeText(getApplicationContext(), R.string.info_user_blocked, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.info_user_unblocked, Toast.LENGTH_SHORT).show();
				}
			}
			// check if following status changed
			else if (relation.isFollowing() != this.relation.isFollowing()) {
				if (relation.isFollowing()) {
					Toast.makeText(getApplicationContext(), R.string.info_followed, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.info_unfollowed, Toast.LENGTH_SHORT).show();
				}
			}
			// check if mute status changed
			else if (relation.isMuted() != this.relation.isMuted()) {
				if (relation.isMuted()) {
					Toast.makeText(getApplicationContext(), R.string.info_user_muted, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.info_user_unmuted, Toast.LENGTH_SHORT).show();
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
	public void onError(@Nullable ConnectionException err) {
		ErrorHandler.handleFailure(this, err);
		if (user == null || (err != null
				&& (err.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND
				|| err.getErrorCode() == ConnectionException.USER_NOT_FOUND))) {
			finish();
		}
	}
}