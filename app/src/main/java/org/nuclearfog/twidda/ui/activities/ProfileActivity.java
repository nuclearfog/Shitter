package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import androidx.viewpager2.widget.ViewPager2;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.DomainAction;
import org.nuclearfog.twidda.backend.async.RelationLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.UserLoader;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LinkAndScrollMovement;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.backend.utils.Tagger;
import org.nuclearfog.twidda.backend.utils.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.backend.utils.ToolbarUpdater;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.viewpager.ProfileAdapter;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ReportDialog;
import org.nuclearfog.twidda.ui.views.LockableConstraintLayout;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.text.SimpleDateFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Activity class for user profile page
 *
 * @author nuclearfog
 */
public class ProfileActivity extends AppCompatActivity implements OnClickListener, OnTagClickListener, OnTabSelectedListener, OnConfirmListener, Callback {

	/**
	 * Key for the user ID
	 * value type is Long
	 */
	public static final String KEY_ID = "profile_id";

	/**
	 * key to save user data
	 * value type is {@link User}
	 */
	public static final String KEY_USER = "profile_user";

	/**
	 * key to save relation data
	 * value type is {@link Relation}
	 */
	private static final String KEY_RELATION = "profile_relation";

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
	 * color of the profile image placeholder
	 */
	private static final int IMAGE_PLACEHOLDER_COLOR = 0x2F000000;

	private AsyncCallback<DomainAction.Result> domainCallback = this::setDomainResult;
	private AsyncCallback<RelationLoader.Result> relationCallback = this::setRelationResult;
	private AsyncCallback<UserLoader.Result> userCallback = this::setUserResult;
	private AsyncCallback<TextEmojiLoader.Result> usernameUpdate = this::onUsernameUpdate;
	private AsyncCallback<TextEmojiLoader.Result> userDescriptionUpdate = this::onUserDescriptionUpdate;

	private ProfileAdapter adapter;
	private GlobalSettings settings;
	private Picasso picasso;
	private ReportDialog reportDialog;

	private DomainAction domainAction;
	private RelationLoader relationLoader;
	private UserLoader userLoader;
	private TextEmojiLoader emojiLoader;

	private TextView user_location, user_createdAt, user_website, description, follow_back, username, screenName;
	private ImageView profileImage, bannerImage, toolbarBackground;
	private Button following, follower;

	private ViewPager2 viewPager;
	private TabSelector tabSelector;
	private Toolbar toolbar;
	@Nullable
	private LockableConstraintLayout body;

	@Nullable
	private Relation relation;
	@Nullable
	private User user;
	private String urlToRedirect;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_profile);
		ViewGroup root = findViewById(R.id.page_profile_root);
		View floatingButton = findViewById(R.id.page_profile_post_button);
		ConstraintLayout header = findViewById(R.id.page_profile_header);
		ConstraintLayout body = findViewById(R.id.page_profile_body);
		toolbar = findViewById(R.id.page_profile_toolbar);
		description = findViewById(R.id.page_profile_description);
		following = findViewById(R.id.page_profile_following);
		follower = findViewById(R.id.page_profile_follower);
		user_website = findViewById(R.id.page_profile_links);
		profileImage = findViewById(R.id.page_profile_image);
		bannerImage = findViewById(R.id.page_profile_banner);
		toolbarBackground = findViewById(R.id.page_profile_toolbar_background);
		username = findViewById(R.id.page_profile_username);
		screenName = findViewById(R.id.page_profile_screenname);
		user_location = findViewById(R.id.page_profile_location);
		user_createdAt = findViewById(R.id.page_profile_date);
		follow_back = findViewById(R.id.page_profile_followback);
		tabSelector = findViewById(R.id.page_profile_tab);
		viewPager = findViewById(R.id.page_profile_pager);

		relationLoader = new RelationLoader(this);
		domainAction = new DomainAction(this);
		userLoader = new UserLoader(this);
		emojiLoader = new TextEmojiLoader(this);
		reportDialog = new ReportDialog(this);
		picasso = PicassoBuilder.get(this);
		settings = GlobalSettings.get(this);
		adapter = new ProfileAdapter(this);

		if (body instanceof LockableConstraintLayout) {
			this.body = (LockableConstraintLayout) body;
		}
		if (!settings.toolbarOverlapEnabled()) {
			ConstraintSet constraints = new ConstraintSet();
			constraints.clone(header);
			constraints.connect(R.id.page_profile_banner, ConstraintSet.TOP, R.id.page_profile_toolbar, ConstraintSet.BOTTOM);
			constraints.applyTo(header);
		}
		if (!settings.floatingButtonEnabled()) {
			floatingButton.setVisibility(View.INVISIBLE);
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

		long userId = 0L;
		if (savedInstanceState != null) {
			Object userData = savedInstanceState.getSerializable(KEY_USER);
			Object relationData = savedInstanceState.getSerializable(KEY_RELATION);
			// get relation data
			if (relationData instanceof Relation) {
				relation = (Relation) relationData;
			}
			// get user data
			if (userData instanceof User) {
				user = (User) userData;
				userId = user.getId();
			}
		} else {
			Object userData = getIntent().getSerializableExtra(KEY_USER);
			if (userData instanceof User) {
				user = (User) userData;
				userId = user.getId();
			} else {
				userId = getIntent().getLongExtra(KEY_ID, 0L);
			}
		}
		// setup pager fragments
		adapter.setId(userId);
		viewPager.setAdapter(adapter);
		// set user/relation data and initialize loaders
		if (user != null) {
			setUser(user);
			UserLoader.Param param = new UserLoader.Param(UserLoader.Param.ONLINE, userId);
			userLoader.execute(param, userCallback);
		} else {
			UserLoader.Param param = new UserLoader.Param(UserLoader.Param.DATABASE, userId);
			userLoader.execute(param, userCallback);
		}
		if (relation == null && userId != settings.getLogin().getId()) {
			RelationLoader.Param param = new RelationLoader.Param(userId, RelationLoader.Param.LOAD);
			relationLoader.execute(param, relationCallback);
		}
		if (userId != settings.getLogin().getId()) {
			tabSelector.addTabIcons(R.array.profile_tab_icons);
			viewPager.setOffscreenPageLimit(3);
		} else if (settings.likeEnabled()) {
			tabSelector.addTabIcons(R.array.profile_tab_icons_like);
			viewPager.setOffscreenPageLimit(5);
		} else {
			tabSelector.addTabIcons(R.array.profile_tab_icons_favorite);
			viewPager.setOffscreenPageLimit(5);
		}
		tabSelector.addOnTabSelectedListener(this);
		following.setOnClickListener(this);
		follower.setOnClickListener(this);
		profileImage.setOnClickListener(this);
		bannerImage.setOnClickListener(this);
		user_website.setOnClickListener(this);
		floatingButton.setOnClickListener(this);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_USER, user);
		outState.putSerializable(KEY_RELATION, relation);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onDestroy() {
		relationLoader.cancel();
		userLoader.cancel();
		emojiLoader.cancel();
		domainAction.cancel();
		super.onDestroy();
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
			MenuItem domainItem = m.findItem(R.id.profile_block_domain);
			MenuItem reportItem = m.findItem(R.id.profile_report);

			switch (settings.getLogin().getConfiguration()) {
				case MASTODON:
					listItem.setVisible(user.isCurrentUser());
					domainItem.setVisible(!user.isCurrentUser());
					reportItem.setVisible(!user.isCurrentUser());
					break;
			}
			if (!user.isCurrentUser()) {
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
			if (relation.isFollower()) {
				follow_back.setVisibility(View.VISIBLE);
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
				intent.putExtra(StatusEditor.KEY_TEXT, prefix);
			}
			startActivity(intent);
			return true;
		}
		// follow / unfollow user
		else if (item.getItemId() == R.id.profile_follow) {
			if (relation != null && user != null) {
				if (!relation.isFollowing()) {
					if (relationLoader.isIdle()) {
						RelationLoader.Param param = new RelationLoader.Param(user.getId(), RelationLoader.Param.FOLLOW);
						relationLoader.execute(param, relationCallback);
					}
				} else {
					ConfirmDialog.show(this, ConfirmDialog.PROFILE_UNFOLLOW, null);
				}
			}
			return true;
		}
		// mute user
		else if (item.getItemId() == R.id.profile_mute) {
			if (relation != null && user != null) {
				if (relation.isMuted()) {
					if (relationLoader.isIdle()) {
						RelationLoader.Param param = new RelationLoader.Param(user.getId(), RelationLoader.Param.UNMUTE);
						relationLoader.execute(param, relationCallback);
					}
				} else {
					ConfirmDialog.show(this, ConfirmDialog.PROFILE_MUTE, null);
				}
			}
			return true;
		}
		// block user
		else if (item.getItemId() == R.id.profile_block) {
			if (relation != null && user != null) {
				if (relation.isBlocked()) {
					if (relationLoader.isIdle()) {
						RelationLoader.Param param = new RelationLoader.Param(user.getId(), RelationLoader.Param.UNBLOCK);
						relationLoader.execute(param, relationCallback);
					}
				} else {
					ConfirmDialog.show(this, ConfirmDialog.PROFILE_BLOCK, null);
				}
			}
			return true;
		}
		// open users list
		else if (item.getItemId() == R.id.profile_lists) {
			if (user != null) {
				Intent intent = new Intent(this, UserlistsActivity.class);
				intent.putExtra(UserlistsActivity.KEY_ID, user.getId());
				startActivity(intent);
			}
			return true;
		}
		// block user domain
		else if (item.getItemId() == R.id.profile_block_domain) {
			if (user != null) {
				ConfirmDialog.show(this, ConfirmDialog.DOMAIN_BLOCK_ADD, null);
			}
		}
		// report user
		else if (item.getItemId() == R.id.profile_report) {
			if (user != null) {
				reportDialog.show(user.getId());
			}
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			Intent returnData = new Intent();
			returnData.putExtra(KEY_USER, user);
			setResult(RETURN_USER_UPDATED, returnData);
			super.onBackPressed();
		}
	}


	@Override
	public void onTagClick(String text) {
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra(SearchActivity.KEY_QUERY, text);
		startActivity(intent);
	}


	@Override
	public void onLinkClick(String tag) {
		if (ConfirmDialog.show(this, ConfirmDialog.CONTINUE_BROWSER, null)) {
			urlToRedirect = tag;
		}
	}


	@Override
	public void onClick(View v) {
		if (user == null)
			return;
		// open following page
		if (v.getId() == R.id.page_profile_following) {
			Intent intent = new Intent(this, UsersActivity.class);
			intent.putExtra(UsersActivity.KEY_ID, user.getId());
			intent.putExtra(UsersActivity.KEY_MODE, UsersActivity.USERS_FOLLOWING);
			startActivity(intent);
		}
		// open follower page
		else if (v.getId() == R.id.page_profile_follower) {
			Intent intent = new Intent(this, UsersActivity.class);
			intent.putExtra(UsersActivity.KEY_ID, user.getId());
			intent.putExtra(UsersActivity.KEY_MODE, UsersActivity.USERS_FOLLOWER);
			startActivity(intent);
		}
		// open link added to profile
		else if (v.getId() == R.id.page_profile_links) {
			if (!user.getProfileUrl().isEmpty()) {
				if (ConfirmDialog.show(this, ConfirmDialog.CONTINUE_BROWSER, null)) {
					urlToRedirect = user.getProfileUrl();
				}
			}
		}
		// open profile image
		else if (v.getId() == R.id.page_profile_image) {
			if (!user.getOriginalProfileImageUrl().isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.KEY_IMAGE_DATA, user.getOriginalProfileImageUrl());
				startActivity(intent);
			}
		}
		// open banner image
		else if (v.getId() == R.id.page_profile_banner) {
			if (!user.getOriginalBannerImageUrl().isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.KEY_IMAGE_DATA, user.getOriginalBannerImageUrl());
				startActivity(intent);
			}
		}
		// open status editor
		else if (v.getId() == R.id.page_profile_post_button) {
			Intent intent = new Intent(this, StatusEditor.class);
			if (user != null && !user.isCurrentUser()) {
				// add username to status
				String prefix = user.getScreenname() + " ";
				intent.putExtra(StatusEditor.KEY_TEXT, prefix);
			}
			startActivity(intent);
		}
	}


	@Override
	public void onConfirm(int type, boolean remember) {
		if (user != null) {
			// confirmed unfollowing user
			if (type == ConfirmDialog.PROFILE_UNFOLLOW) {
				RelationLoader.Param param = new RelationLoader.Param(user.getId(), RelationLoader.Param.UNFOLLOW);
				relationLoader.execute(param, relationCallback);
			}
			// confirmed blocking user
			else if (type == ConfirmDialog.PROFILE_BLOCK) {
				RelationLoader.Param param = new RelationLoader.Param(user.getId(), RelationLoader.Param.BLOCK);
				relationLoader.execute(param, relationCallback);
			}
			// confirmed muting user
			else if (type == ConfirmDialog.PROFILE_MUTE) {
				RelationLoader.Param param = new RelationLoader.Param(user.getId(), RelationLoader.Param.MUTE);
				relationLoader.execute(param, relationCallback);
			}
			// confirmed domain block
			else if (type == ConfirmDialog.DOMAIN_BLOCK_ADD) {
				String url = Uri.parse(user.getProfileUrl()).getHost();
				DomainAction.Param param = new DomainAction.Param(DomainAction.Param.MODE_BLOCK, url);
				domainAction.execute(param, domainCallback);
			}
			// confirmed redirect to browser
			else if (type == ConfirmDialog.CONTINUE_BROWSER) {
				settings.setProxyWarning(!remember);
				LinkUtils.redirectToBrowser(this, urlToRedirect);
			}
		}
	}


	@Override
	public void onTabSelected() {
		adapter.scrollToTop();
		// remove lock when changing page
		if (body != null) {
			body.lock(false);
		}
	}


	@Override
	public void onSuccess() {
		// setup toolbar background
		if (settings.toolbarOverlapEnabled()) {
			bannerImage.post(new ToolbarUpdater(bannerImage, toolbarBackground));
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
	private void setUserResult(@NonNull UserLoader.Result result) {
		switch (result.mode) {
			case UserLoader.Result.DATABASE:
				if (result.user != null) {
					UserLoader.Param param = new UserLoader.Param(UserLoader.Param.ONLINE, result.user.getId());
					userLoader.execute(param, userCallback);
				}
				// fall through

			case UserLoader.Result.ONLINE:
				if (result.user != null) {
					setUser(result.user);
				}
				break;

			case UserLoader.Result.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
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
	private void setRelationResult(@NonNull RelationLoader.Result result) {
		switch (result.mode) {
			case RelationLoader.Result.BLOCK:
				Toast.makeText(getApplicationContext(), R.string.info_blocked, Toast.LENGTH_SHORT).show();
				break;

			case RelationLoader.Result.UNBLOCK:
				Toast.makeText(getApplicationContext(), R.string.info_user_unblocked, Toast.LENGTH_SHORT).show();
				break;

			case RelationLoader.Result.MUTE:
				Toast.makeText(getApplicationContext(), R.string.info_user_muted, Toast.LENGTH_SHORT).show();
				break;

			case RelationLoader.Result.UNMUTE:
				Toast.makeText(getApplicationContext(), R.string.info_user_unmuted, Toast.LENGTH_SHORT).show();
				break;

			case RelationLoader.Result.FOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_followed, Toast.LENGTH_SHORT).show();
				break;

			case RelationLoader.Result.UNFOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_unfollowed, Toast.LENGTH_SHORT).show();
				break;

			case RelationLoader.Result.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				break;
		}
		if (result.relation != null) {
			relation = result.relation;
			invalidateOptionsMenu();
		}
	}

	/**
	 * set domain block result
	 */
	private void setDomainResult(DomainAction.Result result) {
		if (result.mode == DomainAction.Result.MODE_BLOCK) {
			Toast.makeText(getApplicationContext(), R.string.info_domain_blocked, Toast.LENGTH_SHORT).show();
		} else if (result.mode == DomainAction.Result.ERROR) {
			ErrorUtils.showErrorMessage(this, result.exception);
		}
	}

	/**
	 * Set User Information
	 *
	 * @param user User data
	 */
	private void setUser(@NonNull User user) {
		this.user = user;
		Drawable placeholder = new ColorDrawable(IMAGE_PLACEHOLDER_COLOR);
		following.setText(StringUtils.NUMBER_FORMAT.format(user.getFollowing()));
		follower.setText(StringUtils.NUMBER_FORMAT.format(user.getFollower()));
		following.setVisibility(View.VISIBLE);
		follower.setVisibility(View.VISIBLE);
		user_createdAt.setVisibility(View.VISIBLE);
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
		user_createdAt.setText(SimpleDateFormat.getDateInstance().format(user.getTimestamp()));
		// set user description
		if (!user.getDescription().isEmpty()) {
			Spannable descriptionSpan = Tagger.makeTextWithLinks(user.getDescription(), settings.getHighlightColor(), this);
			if (user.getEmojis().length > 0) {
				descriptionSpan = EmojiUtils.removeTags(descriptionSpan);
			}
			description.setText(descriptionSpan);
			description.setVisibility(View.VISIBLE);
		} else {
			description.setVisibility(View.GONE);
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
			user_location.setVisibility(View.VISIBLE);
		} else {
			user_location.setVisibility(View.GONE);
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
			user_website.setVisibility(View.VISIBLE);
		} else {
			user_website.setVisibility(View.GONE);
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
				picasso.load(profileImageUrl).transform(roundCorner).placeholder(placeholder).error(R.drawable.no_image).into(profileImage);
			} else {
				profileImage.setImageDrawable(placeholder);
			}
		} else {
			profileImage.setImageDrawable(placeholder);
		}
		// initialize emoji loading for username/description
		if (settings.imagesEnabled() && user.getEmojis().length > 0) {
			if (!user.getUsername().isEmpty()) {
				SpannableString usernameSpan = new SpannableString(user.getUsername());
				TextEmojiLoader.Param param = new TextEmojiLoader.Param(user.getEmojis(), usernameSpan, getResources().getDimensionPixelSize(R.dimen.profile_icon_size));
				emojiLoader.execute(param, usernameUpdate);
			}
			if (!user.getDescription().trim().isEmpty()) {
				Spannable descriptionSpan = new SpannableString(user.getDescription());
				TextEmojiLoader.Param param = new TextEmojiLoader.Param(user.getEmojis(), descriptionSpan, getResources().getDimensionPixelSize(R.dimen.profile_icon_size));
				emojiLoader.execute(param, userDescriptionUpdate);
			}
		}
	}

	/**
	 * update username with emojis
	 */
	private void onUsernameUpdate(@NonNull TextEmojiLoader.Result result) {
		if (result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}

	/**
	 * update user description with emojis
	 */
	private void onUserDescriptionUpdate(@NonNull TextEmojiLoader.Result result) {
		if (result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			description.setText(spannable);
		}
	}
}