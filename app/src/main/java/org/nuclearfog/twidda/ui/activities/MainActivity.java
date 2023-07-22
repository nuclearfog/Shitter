package org.nuclearfog.twidda.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor;
import org.nuclearfog.twidda.backend.async.UserLoader;
import org.nuclearfog.twidda.backend.async.UserLoader.UserParam;
import org.nuclearfog.twidda.backend.async.UserLoader.UserResult;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.viewpager.HomeAdapter;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

/**
 * Main Activity of the App
 *
 * @author nuclearfog
 */
public class MainActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, OnTabSelectedListener,
		OnQueryTextListener, OnNavigationItemSelectedListener, OnClickListener, AsyncExecutor.AsyncCallback<UserLoader.UserResult> {

	public static final String KEY_SELECT_NOTIFICATION = "main_notification";

	/**
	 * color of the profile image placeholder
	 */
	private static final int IMAGE_PLACEHOLDER_COLOR = 0x2F000000;

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	@Nullable
	private Intent loginIntent;
	@Nullable
	private HomeAdapter adapter;
	private GlobalSettings settings;
	private UserLoader userLoader;
	private Picasso picasso;

	private Dialog loadingCircle;

	private DrawerLayout drawerLayout;
	private TabSelector tabSelector;
	private ViewPager2 viewPager;
	private ImageView profileImage;
	private TextView username, screenname;
	private TextView followingCount, followerCount;

	@Nullable
	private User currentUser;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_main);
		NavigationView navigationView = findViewById(R.id.home_navigator);
		ViewGroup header = (ViewGroup) navigationView.getHeaderView(0);
		Toolbar toolbar = findViewById(R.id.home_toolbar);
		View floatingButton = findViewById(R.id.home_post);
		drawerLayout = findViewById(R.id.main_layout);
		viewPager = findViewById(R.id.home_pager);
		tabSelector = findViewById(R.id.home_tab);
		profileImage = header.findViewById(R.id.navigation_profile_image);
		username = header.findViewById(R.id.navigation_profile_username);
		screenname = header.findViewById(R.id.navigation_profile_screenname);
		followerCount = header.findViewById(R.id.navigation_profile_follower);
		followingCount = header.findViewById(R.id.navigation_profile_following);

		userLoader = new UserLoader(this);
		loadingCircle = new ProgressDialog(this, null);
		settings = GlobalSettings.get(this);
		picasso = PicassoBuilder.get(this);
		tabSelector.addViewPager(viewPager);
		viewPager.setOffscreenPageLimit(4);

		toolbar.setTitle("");
		toolbar.setNavigationIcon(R.drawable.menu);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(header);
		navigationView.post(new Runnable() {
			@Override
			public void run() {
				AppStyles.setTheme(navigationView);
			}
		});

		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.open();
			}
		});
		tabSelector.addOnTabSelectedListener(this);
		navigationView.setNavigationItemSelectedListener(this);
		floatingButton.setOnClickListener(this);
		header.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		// open login page if there isn't any account selected
		if (!settings.isLoggedIn() && loginIntent == null) {
			loginIntent = new Intent(this, LoginActivity.class);
			activityResultLauncher.launch(loginIntent);
		}
		// initialize lists
		else {
			if (adapter == null) {
				adapter = new HomeAdapter(this);
				viewPager.setAdapter(adapter);
				setStyle();
				if (getIntent().getBooleanExtra(KEY_SELECT_NOTIFICATION, false)) {
					// select notification page if user clicks on notification
					viewPager.setCurrentItem(adapter.getItemCount() - 1, false);
				}
			}
			if (currentUser == null) {
				UserParam param = new UserParam(UserParam.DATABASE, settings.getLogin().getId());
				userLoader.execute(param, this);
			}
		}
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			if (drawerLayout.isOpen()) {
				drawerLayout.close();
			} else {
				super.onBackPressed();
			}
		}
	}


	@Override
	protected void onDestroy() {
		loadingCircle.dismiss();
		super.onDestroy();
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		invalidateMenu();
		switch (result.getResultCode()) {
			case ProfileActivity.RETURN_USER_UPDATED:
				if (result.getData() != null) {
					Object object = result.getData().getSerializableExtra(ProfileActivity.KEY_USER);
					if (object instanceof User) {
						setCurrentUser((User) object);
					}
				}
				break;

			case LoginActivity.RETURN_LOGIN_CANCELED:
				finish();
				break;

			case SettingsActivity.RETURN_APP_LOGOUT:
				viewPager.setAdapter(null);
				setCurrentUser(null);
				adapter = null;
				break;

			case SettingsActivity.RETURN_FONT_SCALE_CHANGED:
				AppStyles.updateFontScale(this);
				// fall through

			case AccountActivity.RETURN_ACCOUNT_CHANGED:
			case LoginActivity.RETURN_LOGIN_SUCCESSFUL:
				loginIntent = null;
				// fall through

			default:
			case SettingsActivity.RETURN_SETTINGS_CHANGED:
			case AccountActivity.RETURN_SETTINGS_CHANGED:
				if (adapter != null)
					adapter.notifySettingsChanged();
				setStyle();
				setCurrentUser(currentUser);
				break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		MenuItem search = menu.findItem(R.id.menu_search);
		SearchView searchView = (SearchView) search.getActionView();
		searchView.setOnQueryTextListener(this);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem search = menu.findItem(R.id.menu_search);
		search.collapseActionView();
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// theme expanded search view
		if (item.getItemId() == R.id.menu_search) {
			SearchView searchView = (SearchView) item.getActionView();
			AppStyles.setTheme(searchView, Color.TRANSPARENT);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		// open filter page
		if (item.getItemId() == R.id.menu_navigator_filter) {
			Intent intent = new Intent(this, FilterActivity.class);
			startActivity(intent);
			return true;
		}
		// open status editor
		else if (item.getItemId() == R.id.menu_navigator_status) {
			Intent intent = new Intent(this, StatusEditor.class);
			startActivity(intent);
			return true;
		}
		// open app settings
		else if (item.getItemId() == R.id.menu_navigator_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			activityResultLauncher.launch(intent);
			return true;
		}
		// open account manager
		else if (item.getItemId() == R.id.menu_navigator_account) {
			Intent intent = new Intent(this, AccountActivity.class);
			activityResultLauncher.launch(intent);
			return true;
		}
		// open user lists
		else if (item.getItemId() == R.id.menu_navigator_lists) {
			Intent intent = new Intent(this, UserlistsActivity.class);
			intent.putExtra(UserlistsActivity.KEY_ID, settings.getLogin().getId());
			startActivity(intent);
		}
		return false;
	}


	@Override
	public boolean onQueryTextSubmit(String s) {
		if (s.length() <= SearchActivity.SEARCH_STR_MAX_LEN && !s.contains(":") && !s.contains("$")) {
			Intent search = new Intent(this, SearchActivity.class);
			search.putExtra(SearchActivity.KEY_QUERY, s);
			startActivity(search);
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_search, Toast.LENGTH_SHORT).show();
		}
		return false;
	}


	@Override
	public boolean onQueryTextChange(String s) {
		return false;
	}


	@Override
	public void onTabSelected(int oldPosition) {
		if (adapter != null) {
			adapter.scrollToTop(oldPosition);
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.navogation_header_root) {
			Intent intent = new Intent(this, ProfileActivity.class);
			if (currentUser != null)
				intent.putExtra(ProfileActivity.KEY_USER, currentUser);
			else
				intent.putExtra(ProfileActivity.KEY_ID, settings.getLogin().getId());
			activityResultLauncher.launch(intent);
		} else if (v.getId() == R.id.home_post) {
			Intent intent = new Intent(this, StatusEditor.class);
			startActivity(intent);
		}
	}


	@Override
	public void onResult(@NonNull UserResult userResult) {
		if (userResult.user != null) {
			setCurrentUser(userResult.user);
		}
	}

	/**
	 *
	 */
	private void setStyle() {
		AppStyles.setTheme(drawerLayout);
		tabSelector.addTabIcons(settings.getLogin().getConfiguration().getHomeTabIcons());
		tabSelector.updateTheme();
	}

	/**
	 * set current user information to navigation drawer
	 *
	 * @param user user information
	 */
	private void setCurrentUser(@Nullable User user) {
		currentUser = user;
		if (user != null) {
			followingCount.setText(StringUtils.NUMBER_FORMAT.format(user.getFollowing()));
			followerCount.setText(StringUtils.NUMBER_FORMAT.format(user.getFollower()));
			screenname.setText(user.getScreenname());
			if (!user.getUsername().trim().isEmpty() && user.getEmojis().length > 0) {
				Spannable usernameSpan = new SpannableString(user.getUsername());
				usernameSpan = EmojiUtils.removeTags(usernameSpan);
				username.setText(usernameSpan);
			} else {
				username.setText(user.getUsername());
			}
			Drawable placeholder = new ColorDrawable(IMAGE_PLACEHOLDER_COLOR);
			if (!user.getProfileImageThumbnailUrl().isEmpty()) {
				picasso.load(user.getProfileImageThumbnailUrl()).placeholder(placeholder).into(profileImage);
			} else {
				profileImage.setImageDrawable(placeholder);
			}
		} else {
			profileImage.setImageResource(0);
			followingCount.setText("");
			followerCount.setText("");
			screenname.setText("");
			username.setText("");
		}
	}
}