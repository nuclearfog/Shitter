package org.nuclearfog.twidda.ui.activities;

import static android.content.Intent.ACTION_VIEW;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.ui.activities.MessageEditor.KEY_MESSAGE_PREFIX;
import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;
import androidx.core.widget.NestedScrollView.OnScrollChangeListener;
import androidx.viewpager2.widget.ViewPager2;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiParam;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiResult;
import org.nuclearfog.twidda.backend.async.RelationLoader;
import org.nuclearfog.twidda.backend.async.RelationLoader.RelationParam;
import org.nuclearfog.twidda.backend.async.RelationLoader.RelationResult;
import org.nuclearfog.twidda.backend.async.UserLoader;
import org.nuclearfog.twidda.backend.async.UserLoader.UserParam;
import org.nuclearfog.twidda.backend.async.UserLoader.UserResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.FragmentAdapter;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.views.LockableLinearLayout;
import org.nuclearfog.twidda.ui.views.LockableLinearLayout.LockCallback;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Activity class for user profile page
 *
 * @author nuclearfog
 */
public class ProfileActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, OnScrollChangeListener,
		OnClickListener, OnTagClickListener, OnTabSelectedListener, OnConfirmListener, Callback, LockCallback {

	/**
	 * Key for the user ID
	 * value type is Long
	 */
	public static final String KEY_PROFILE_ID = "profile_id";

	/**
	 * key to save user data
	 * value type is {@link User}
	 */
	public static final String KEY_PROFILE_USER = "profile_user";

	/**
	 * key to save relation data
	 * value type is {@link Relation}
	 */
	private static final String KEY_PROFILE_RELATION = "profile_relation";

	/**
	 * key to send updated user data
	 * value type is {@link User}
	 */
	public static final String RETURN_USER_UPDATE = "user_update";

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

	/**
	 * scrollview position threshold to lock/unlock child scrolling
	 */
	private static final int SCROLL_THRESHOLD = 10;

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
	private AsyncCallback<RelationResult> relationCallback = this::setRelationResult;
	private AsyncCallback<UserResult> userCallback = this::setUserResult;
	private AsyncCallback<EmojiResult> usernameUpdate = this::onUsernameUpdate;
	private AsyncCallback<EmojiResult> userDescriptionUpdate = this::onUserDescriptionUpdate;

	private FragmentAdapter adapter;
	private GlobalSettings settings;
	private Picasso picasso;
	private ConfirmDialog confirmDialog;

	private RelationLoader relationLoader;
	private UserLoader userLoader;
	private TextEmojiLoader emojiLoader;

	private NestedScrollView root;
	private ConstraintLayout header;
	private LockableLinearLayout body;
	private TextView user_location, user_createdAt, user_website, description, follow_back, username, screenName;
	private ImageView profileImage, bannerImage, toolbarBackground;
	private Button following, follower;
	private ViewPager2 viewPager;
	private TabSelector tabSelector;
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
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_profile);
		header = findViewById(R.id.page_profile_header);
		body = findViewById(R.id.page_profile_body);
		root = findViewById(R.id.user_view);
		toolbar = findViewById(R.id.profile_toolbar);
		description = findViewById(R.id.bio);
		following = findViewById(R.id.following);
		follower = findViewById(R.id.follower);
		user_website = findViewById(R.id.links);
		profileImage = findViewById(R.id.profile_img);
		bannerImage = findViewById(R.id.profile_banner);
		toolbarBackground = findViewById(R.id.profile_toolbar_background);
		username = findViewById(R.id.profile_username);
		screenName = findViewById(R.id.profile_screenname);
		user_location = findViewById(R.id.location);
		user_createdAt = findViewById(R.id.profile_date);
		follow_back = findViewById(R.id.follow_back);
		tabSelector = findViewById(R.id.profile_tab);
		viewPager = findViewById(R.id.profile_pager);

		relationLoader = new RelationLoader(this);
		userLoader = new UserLoader(this);
		emojiLoader = new TextEmojiLoader(this);
		picasso = PicassoBuilder.get(this);
		settings = GlobalSettings.getInstance(this);
		if (!settings.toolbarOverlapEnabled()) {
			ConstraintSet constraints = new ConstraintSet();
			constraints.clone(header);
			constraints.connect(R.id.profile_banner, ConstraintSet.TOP, R.id.profile_toolbar, ConstraintSet.BOTTOM);
			constraints.applyTo(header);
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
		description.setMovementMethod(LinkAndScrollMovement.getInstance());
		description.setLinkTextColor(settings.getHighlightColor());
		AppStyles.setTheme(root);
		user_website.setTextColor(settings.getHighlightColor());
		tabSelector.setBackgroundColor(Color.TRANSPARENT);

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		adapter = new FragmentAdapter(this);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(3);
		tabSelector.addViewPager(viewPager);
		confirmDialog = new ConfirmDialog(this);

		// get parameters
		if (savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras();
		}
		if (savedInstanceState == null) {
			return;
		}
		long userId = savedInstanceState.getLong(KEY_PROFILE_ID, 0L);
		Serializable serializedUser = savedInstanceState.getSerializable(KEY_PROFILE_USER);
		Serializable serializedRelation = savedInstanceState.getSerializable(KEY_PROFILE_RELATION);
		// get relation data
		if (serializedRelation instanceof Relation) {
			relation = (Relation) serializedRelation;
		}
		// get user data
		if (serializedUser instanceof User) {
			user = (User) serializedUser;
			userId = user.getId();
		}
		// set user/relation data and initialize loaders
		if (user != null) {
			setUser(user);
			UserParam param = new UserParam(UserParam.ONLINE, userId);
			userLoader.execute(param, userCallback);
		} else {
			UserParam param = new UserParam(UserParam.DATABASE, userId);
			userLoader.execute(param, userCallback);
		}
		if (relation == null && userId != settings.getLogin().getId()) {
			RelationParam param = new RelationParam(userId, RelationParam.LOAD);
			relationLoader.execute(param, relationCallback);
		}
		adapter.setupProfilePage(userId);
		if (settings.likeEnabled()) {
			tabSelector.addTabIcons(R.array.profile_tab_icons_like);
		} else {
			tabSelector.addTabIcons(R.array.profile_tab_icons);
		}

		tabSelector.addOnTabSelectedListener(this);
		following.setOnClickListener(this);
		follower.setOnClickListener(this);
		profileImage.setOnClickListener(this);
		bannerImage.setOnClickListener(this);
		user_website.setOnClickListener(this);
		confirmDialog.setConfirmListener(this);
		root.setOnScrollChangeListener(this);
		body.addLockCallback(this);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_PROFILE_USER, user);
		outState.putSerializable(KEY_PROFILE_RELATION, relation);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onDestroy() {
		relationLoader.cancel();
		userLoader.cancel();
		emojiLoader.cancel();
		super.onDestroy();
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			body.getLayoutParams().height = root.getMeasuredHeight();
			root.scrollTo(0, 0);
		}
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getData() != null) {
			if (result.getResultCode() == ProfileEditor.RETURN_PROFILE_CHANGED) {
				Object data = result.getData().getSerializableExtra(ProfileEditor.KEY_UPDATED_PROFILE);
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
		boolean result = super.onPrepareOptionsMenu(m);
		if (user != null) {
			MenuItem listItem = m.findItem(R.id.profile_lists);

			switch (settings.getLogin().getConfiguration()) {
				case TWITTER1:
				case TWITTER2:
					if (user.isCurrentUser()) {
						MenuItem requestItem = m.findItem(R.id.profile_requests);
						requestItem.setVisible(true);
						listItem.setVisible(true);
					} else if (!user.isProtected() || (relation != null && relation.isFollowing())) {
						listItem.setVisible(true);
					}
					break;

				case MASTODON:
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
			result = true;
		}
		if (relation != null) {
			if (relation.isFollowing()) {
				MenuItem followIcon = m.findItem(R.id.profile_follow);
				AppStyles.setMenuItemColor(followIcon, settings.getFollowIconColor());
				followIcon.setTitle(R.string.menu_user_unfollow);
			}
			if (relation.isBlocked()) {
				MenuItem blockIcon = m.findItem(R.id.profile_block);
				blockIcon.setTitle(R.string.menu_user_unblock);
			}
			if (relation.isMuted()) {
				MenuItem muteIcon = m.findItem(R.id.profile_mute);
				muteIcon.setTitle(R.string.menu_unmute_user);
			}
			if (relation.privateMessagingEnabled()) {
				MenuItem dmIcon = m.findItem(R.id.profile_message);
				dmIcon.setVisible(true);
			}
			if (relation.isFollower()) {
				follow_back.setVisibility(VISIBLE);
			}
			result = true;
		}
		return result;
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
			return true;
		}
		// follow / unfollow user
		else if (item.getItemId() == R.id.profile_follow) {
			if (relation != null && user != null) {
				if (!relation.isFollowing()) {
					if (relationLoader.isIdle()) {
						RelationParam param = new RelationParam(user.getId(), RelationParam.FOLLOW);
						relationLoader.execute(param, relationCallback);
					}
				} else {
					confirmDialog.show(ConfirmDialog.PROFILE_UNFOLLOW);
				}
			}
			return true;
		}
		// mute user
		else if (item.getItemId() == R.id.profile_mute) {
			if (relation != null && user != null) {
				if (relation.isMuted()) {
					if (relationLoader.isIdle()) {
						RelationParam param = new RelationParam(user.getId(), RelationParam.UNMUTE);
						relationLoader.execute(param, relationCallback);
					}
				} else {
					confirmDialog.show(ConfirmDialog.PROFILE_MUTE);
				}
			}
			return true;
		}
		// block user
		else if (item.getItemId() == R.id.profile_block) {
			if (relation != null && user != null) {
				if (relation.isBlocked()) {
					if (relationLoader.isIdle()) {
						RelationParam param = new RelationParam(user.getId(), RelationParam.UNBLOCK);
						relationLoader.execute(param, relationCallback);
					}
				} else {
					confirmDialog.show(ConfirmDialog.PROFILE_BLOCK);
				}
			}
			return true;
		}
		// open profile editor
		else if (item.getItemId() == R.id.profile_settings) {
			Intent editProfile = new Intent(this, ProfileEditor.class);
			editProfile.putExtra(ProfileEditor.KEY_PROFILE_DATA, user);
			activityResultLauncher.launch(editProfile);
		}
		// open direct message
		else if (item.getItemId() == R.id.profile_message) {
			Intent intent = new Intent(this, MessageEditor.class);
			if (user != null && !user.isCurrentUser())
				intent.putExtra(KEY_MESSAGE_PREFIX, user.getScreenname());
			startActivity(intent);
			return true;
		}
		// open users list
		else if (item.getItemId() == R.id.profile_lists) {
			if (user != null) {
				Intent intent = new Intent(this, UserlistsActivity.class);
				intent.putExtra(KEY_USERLIST_OWNER_ID, user.getId());
				startActivity(intent);
			}
			return true;
		}
		// open request list
		else if (item.getItemId() == R.id.profile_requests) {
			Intent usersIntent = new Intent(this, UsersActivity.class);
			usersIntent.putExtra(KEY_USERS_MODE, USERS_REQUESTS);
			startActivity(usersIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			Intent returnData = new Intent();
			returnData.putExtra(RETURN_USER_UPDATE, user);
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
		LinkUtils.openLink(this, tag);
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
			switch (settings.getLogin().getConfiguration()) {
				case TWITTER1:
				case TWITTER2:
					if (!user.isProtected() || user.isCurrentUser() || (relation != null && relation.isFollowing())) {
						startActivity(intent);
					}
					break;

				case MASTODON:
					startActivity(intent);
					break;
			}
		}
		// open follower page
		else if (v.getId() == R.id.follower) {
			Intent intent = new Intent(this, UsersActivity.class);
			intent.putExtra(KEY_USERS_ID, user.getId());
			intent.putExtra(KEY_USERS_MODE, USERS_FOLLOWER);
			switch (settings.getLogin().getConfiguration()) {
				case TWITTER1:
				case TWITTER2:
					if (!user.isProtected() || user.isCurrentUser() || (relation != null && relation.isFollowing())) {
						startActivity(intent);
					}
					break;

				case MASTODON:
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
				intent.putExtra(ImageViewer.IMAGE_TYPE, ImageViewer.IMAGE_DEFAULT);
				startActivity(intent);
			}
		}
		// open banner image
		else if (v.getId() == R.id.profile_banner) {
			if (!user.getOriginalBannerImageUrl().isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.IMAGE_URI, Uri.parse(user.getOriginalBannerImageUrl()));
				intent.putExtra(ImageViewer.IMAGE_TYPE, ImageViewer.IMAGE_DEFAULT);
				startActivity(intent);
			}
		}
	}


	@Override
	public void onConfirm(int type) {
		if (user != null) {
			// confirmed unfollowing user
			if (type == ConfirmDialog.PROFILE_UNFOLLOW) {
				RelationParam param = new RelationParam(user.getId(), RelationParam.UNFOLLOW);
				relationLoader.execute(param, relationCallback);
			}
			// confirmed blocking user
			else if (type == ConfirmDialog.PROFILE_BLOCK) {
				RelationParam param = new RelationParam(user.getId(), RelationParam.BLOCK);
				relationLoader.execute(param, relationCallback);
			}
			// confirmed muting user
			else if (type == ConfirmDialog.PROFILE_MUTE) {
				RelationParam param = new RelationParam(user.getId(), RelationParam.MUTE);
				relationLoader.execute(param, relationCallback);
			}
		}
	}


	@Override
	public void onTabSelected(int oldPosition) {
		adapter.scrollToTop(oldPosition);
	}


	@Override
	public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		body.lock(scrollY > header.getMeasuredHeight() + SCROLL_THRESHOLD && scrollY < header.getMeasuredHeight() - SCROLL_THRESHOLD);
	}


	@Override
	public boolean aquireLock() {
		return root.getScrollY() < header.getMeasuredHeight() - SCROLL_THRESHOLD;
	}


	@Override
	public void onSuccess() {
		// setup toolbar background
		if (settings.toolbarOverlapEnabled()) {
			AppStyles.setToolbarBackground(this, bannerImage, toolbarBackground);
		}
	}


	@Override
	public void onError(Exception e) {
	}

	/**
	 * set user result information
	 *
	 * @param result user result from async executor
	 */
	private void setUserResult(@NonNull UserResult result) {
		switch (result.mode) {
			case UserResult.DATABASE:
				if (result.user != null) {
					UserParam param = new UserParam(UserParam.ONLINE, result.user.getId());
					userLoader.execute(param, userCallback);
				}
				// fall through

			case UserResult.ONLINE:
				if (result.user != null) {
					setUser(result.user);
				}
				break;

			case UserResult.ERROR:
				String message = ErrorHandler.getErrorMessage(this, result.exception);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				if (user == null || (result.exception != null
						&& (result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND
						|| result.exception.getErrorCode() == ConnectionException.USER_NOT_FOUND))) {
					finish();
				}
				break;
		}
	}

	/**
	 * set user relation information
	 *
	 * @param result relation result from async executor
	 */
	private void setRelationResult(@NonNull RelationResult result) {
		switch (result.mode) {
			case RelationResult.BLOCK:
				Toast.makeText(getApplicationContext(), R.string.info_user_blocked, Toast.LENGTH_SHORT).show();
				break;

			case RelationResult.UNBLOCK:
				Toast.makeText(getApplicationContext(), R.string.info_user_unblocked, Toast.LENGTH_SHORT).show();
				break;

			case RelationResult.MUTE:
				Toast.makeText(getApplicationContext(), R.string.info_user_muted, Toast.LENGTH_SHORT).show();
				break;

			case RelationResult.UNMUTE:
				Toast.makeText(getApplicationContext(), R.string.info_user_unmuted, Toast.LENGTH_SHORT).show();
				break;

			case RelationResult.FOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_followed, Toast.LENGTH_SHORT).show();
				break;

			case RelationResult.UNFOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_unfollowed, Toast.LENGTH_SHORT).show();
				break;

			case RelationResult.ERROR:
				String message = ErrorHandler.getErrorMessage(this, result.exception);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				break;
		}
		if (result.relation != null) {
			relation = result.relation;
			invalidateOptionsMenu();
		}
	}

	/**
	 * Set User Information
	 *
	 * @param user User data
	 */
	private void setUser(@NonNull User user) {
		this.user = user;
		following.setText(StringUtils.NUMBER_FORMAT.format(user.getFollowing()));
		follower.setText(StringUtils.NUMBER_FORMAT.format(user.getFollower()));
		following.setVisibility(VISIBLE);
		follower.setVisibility(VISIBLE);
		user_createdAt.setVisibility(VISIBLE);
		screenName.setText(user.getScreenname());
		// set status count
		if (user.getStatusCount() >= 0) {
			tabSelector.setLabel(0, StringUtils.NUMBER_FORMAT.format(user.getStatusCount()));
		} else {
			tabSelector.setLabel(0, "");
		}
		// set favorites count
		if (user.getFavoriteCount() >= 0) {
			tabSelector.setLabel(1, StringUtils.NUMBER_FORMAT.format(user.getFavoriteCount()));
		} else {
			tabSelector.setLabel(1, "");
		}
		// set username and emojis
		if (!user.getUsername().trim().isEmpty() && user.getEmojis().length > 0) {
			Spannable usernameSpan = new SpannableString(user.getUsername());
			usernameSpan = EmojiUtils.removeTags(usernameSpan);
			username.setText(usernameSpan);
		} else {
			username.setText(user.getUsername());
		}
		// set user join date
		if (settings.getLogin().getConfiguration() == Configuration.MASTODON) {
			user_createdAt.setText(SimpleDateFormat.getDateInstance().format(user.getTimestamp()));
		} else {
			user_createdAt.setText(SimpleDateFormat.getDateTimeInstance().format(user.getTimestamp()));
		}
		// set user description
		if (!user.getDescription().isEmpty()) {
			Spannable descriptionSpan = Tagger.makeTextWithLinks(user.getDescription(), settings.getHighlightColor(), this);
			if (user.getEmojis().length > 0) {
				descriptionSpan = EmojiUtils.removeTags(descriptionSpan);
			}
			description.setText(descriptionSpan);
			description.setVisibility(VISIBLE);
		} else {
			description.setVisibility(GONE);
		}
		// set user verified icon
		if (user.isVerified()) {
			username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
			AppStyles.setDrawableColor(username, settings.getIconColor());
		} else {
			username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		// set user protected icon
		if (user.isProtected()) {
			screenName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
			AppStyles.setDrawableColor(screenName, settings.getIconColor());
		} else {
			screenName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		// set user location
		if (!user.getLocation().isEmpty()) {
			user_location.setText(user.getLocation());
			user_location.setVisibility(VISIBLE);
		} else {
			user_location.setVisibility(GONE);
		}
		// set profile url
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
		// set profile/banner images
		if (settings.imagesEnabled()) {
			String bannerImageUrl = user.getBannerImageThumbnailUrl();
			String profileImageUrl = user.getProfileImageThumbnailUrl();
			if (!bannerImageUrl.isEmpty()) {
				picasso.load(bannerImageUrl).error(R.drawable.no_banner).into(bannerImage, this);
			} else {
				bannerImage.setImageResource(0);
				toolbarBackground.setImageResource(0);
			}
			if (!profileImageUrl.isEmpty()) {
				Transformation roundCorner = new RoundedCornersTransformation(5, 0);
				picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profileImage);
			} else {
				profileImage.setImageResource(0);
			}
		}
		// initialize emoji loading for username/description
		if (settings.imagesEnabled() && user.getEmojis().length > 0) {
			if (!user.getUsername().isEmpty()) {
				SpannableString usernameSpan = new SpannableString(user.getUsername());
				EmojiParam param = new EmojiParam(user.getEmojis(), usernameSpan, getResources().getDimensionPixelSize(R.dimen.profile_icon_size));
				emojiLoader.execute(param, usernameUpdate);
			}
			if (!user.getDescription().trim().isEmpty()) {
				Spannable descriptionSpan = new SpannableString(user.getDescription());
				EmojiParam param = new EmojiParam(user.getEmojis(), descriptionSpan, getResources().getDimensionPixelSize(R.dimen.profile_icon_size));
				emojiLoader.execute(param, userDescriptionUpdate);
			}
		}
	}

	/**
	 * update username with emojis
	 */
	private void onUsernameUpdate(@NonNull EmojiResult result) {
		if (result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}

	/**
	 * update user description with emojis
	 */
	private void onUserDescriptionUpdate(@NonNull EmojiResult result) {
		if (result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			description.setText(spannable);
		}
	}
}