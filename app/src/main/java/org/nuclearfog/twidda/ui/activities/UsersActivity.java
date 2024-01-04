package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserFilterAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.viewpager.UserAdapter;
import org.nuclearfog.twidda.ui.fragments.UserFragment;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.util.regex.Pattern;

/**
 * Activity to show one or more lists of users
 *
 * @author nuclearfog
 */
public class UsersActivity extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener, AsyncCallback<UserFilterAction.Result> {

	/**
	 * type of users to get from the source
	 * {@link #USERS_FOLLOWING ,#USERS_FOLLOWER ,#USERS_REPOST ,#USERLIST_FAVORIT,#USERLIST_EXCLUDED_USERS,#USERLIST_REQUESTS}
	 */
	public static final String KEY_MODE = "userlist_mode";

	/**
	 * ID of a userlist, an user or a status to get the users from
	 * value type is Long
	 */
	public static final String KEY_ID = "userlist_id";

	/**
	 * user following, requires user ID
	 *
	 * @see #KEY_MODE
	 */
	public static final int USERS_FOLLOWING = 0xDF893242;

	/**
	 * follower of an user, requires user ID
	 *
	 * @see #KEY_MODE
	 */
	public static final int USERS_FOLLOWER = 0xA89F5968;

	/**
	 * user reposting a status, requires status ID
	 *
	 * @see #KEY_MODE
	 */
	public static final int USERS_REPOST = 0x19F582E;

	/**
	 * user favoriting/liking a status, requires status ID
	 *
	 * @see #KEY_MODE
	 */
	public static final int USERS_FAVORIT = 0x9bcc3f99;

	/**
	 * setup list to show excluded (muted, blocked) users
	 *
	 * @see #KEY_MODE
	 */
	public static final int USERS_EXCLUDED = 0x896a786;

	/**
	 * regex pattern to validate username
	 */
	private static final Pattern USERNAME_PATTERN = Pattern.compile("@?\\w+(@\\w+\\.\\w+)?");

	private GlobalSettings settings;
	private UserFilterAction filterLoader;
	private UserAdapter adapter;

	private ViewPager2 viewPager;

	private int mode;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(Bundle savedInst) {
		super.onCreate(savedInst);
		setContentView(R.layout.page_tab_view);
		ViewGroup root = findViewById(R.id.page_tab_view_root);
		Toolbar toolbar = findViewById(R.id.page_tab_view_toolbar);
		View fragmentContainer = findViewById(R.id.page_tab_view_fragment_container);
		TabSelector tabSelector = findViewById(R.id.page_tab_view_tabs);
		viewPager = findViewById(R.id.page_tab_view_pager);

		FragmentTransaction fragmentTransaction;
		Bundle param = new Bundle();
		filterLoader = new UserFilterAction(this);
		settings = GlobalSettings.get(this);
		adapter = new UserAdapter(this);
		viewPager.setOffscreenPageLimit(3);

		mode = getIntent().getIntExtra(KEY_MODE, 0);
		long id = getIntent().getLongExtra(KEY_ID, 0L);
		adapter.setId(id);

		switch (mode) {
			case USERS_FOLLOWING:
				toolbar.setTitle(R.string.userlist_following);
				adapter.setType(UserAdapter.FOLLOWING);
				if (settings.getLogin().getId() == id) {
					adapter.setPageCount(2);
					tabSelector.addTabIcons(R.array.user_following);
					viewPager.setAdapter(adapter);
				} else {
					viewPager.setVisibility(View.GONE);
					tabSelector.setVisibility(View.GONE);
					fragmentContainer.setVisibility(View.VISIBLE);

					param.putLong(UserFragment.KEY_ID, id);
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWING);
					fragmentTransaction = getSupportFragmentManager().beginTransaction();
					fragmentTransaction.replace(R.id.page_tab_view_fragment_container, UserFragment.class, param);
					fragmentTransaction.commit();
				}
				break;

			case USERS_FOLLOWER:
				toolbar.setTitle(R.string.userlist_follower);
				adapter.setType(UserAdapter.FOLLOWER);
				if (settings.getLogin().getId() == id && settings.getLogin().getConfiguration().isOutgoingFollowRequestSupported()) {
					adapter.setPageCount(2);
					tabSelector.addTabIcons(R.array.user_follower);
					viewPager.setAdapter(adapter);
				} else {
					viewPager.setVisibility(View.GONE);
					tabSelector.setVisibility(View.GONE);
					fragmentContainer.setVisibility(View.VISIBLE);

					param.putLong(UserFragment.KEY_ID, id);
					param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FOLLOWER);
					fragmentTransaction = getSupportFragmentManager().beginTransaction();
					fragmentTransaction.replace(R.id.page_tab_view_fragment_container, UserFragment.class, param);
					fragmentTransaction.commit();
				}
				break;

			case USERS_REPOST:
				viewPager.setVisibility(View.GONE);
				tabSelector.setVisibility(View.GONE);
				fragmentContainer.setVisibility(View.VISIBLE);

				toolbar.setTitle(R.string.toolbar_userlist_repost);
				param.putLong(UserFragment.KEY_ID, id);
				param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_REPOSTER);
				fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.page_tab_view_fragment_container, UserFragment.class, param);
				fragmentTransaction.commit();
				break;

			case USERS_FAVORIT:
				viewPager.setVisibility(View.GONE);
				tabSelector.setVisibility(View.GONE);
				fragmentContainer.setVisibility(View.VISIBLE);

				toolbar.setTitle(settings.likeEnabled() ? R.string.toolbar_status_liker : R.string.toolbar_status_favoriter);
				param.putLong(UserFragment.KEY_ID, id);
				param.putInt(UserFragment.KEY_MODE, UserFragment.MODE_FAVORITER);
				fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.page_tab_view_fragment_container, UserFragment.class, param);
				fragmentTransaction.commit();
				break;

			case USERS_EXCLUDED:
				toolbar.setTitle(R.string.menu_excluded_users);
				adapter.setType(UserAdapter.BLOCKS);
				adapter.setPageCount(3);
				viewPager.setAdapter(adapter);
				tabSelector.addTabIcons(R.array.user_domain_exclude);
				break;
		}
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);

		tabSelector.addOnTabSelectedListener(this);
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getVisibility() == View.VISIBLE && viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			super.onBackPressed();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		if (mode == USERS_EXCLUDED) {
			getMenuInflater().inflate(R.menu.users, m);
			MenuItem search = m.findItem(R.id.menu_user_add);
			SearchView searchView = (SearchView) search.getActionView();
			searchView.setOnQueryTextListener(this);
			AppStyles.setTheme(searchView, Color.TRANSPARENT);
			AppStyles.setMenuIconColor(m, settings.getIconColor());
			return true;
		}
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu m) {
		if (mode == USERS_EXCLUDED) {
			SearchView searchView = (SearchView) m.findItem(R.id.menu_user_add).getActionView();
			if (viewPager.getCurrentItem() == 0) {
				searchView.setQueryHint(getString(R.string.menu_hint_mute_user));
			} else if (viewPager.getCurrentItem() == 1) {
				searchView.setQueryHint(getString(R.string.menu_hint_block_user));
			} else if (viewPager.getCurrentItem() == 2) {
				searchView.setQueryHint(getString(R.string.menu_hint_block_domain));
			}
			return true;
		}
		return super.onPrepareOptionsMenu(m);
	}


	@Override
	public void onTabSelected() {
		adapter.scrollToTop();
		// reset menu
		invalidateOptionsMenu();
	}


	@Override
	public boolean onQueryTextSubmit(String query) {
		if (filterLoader.isIdle()) {
			if (viewPager.getCurrentItem() == 0) {
				if (USERNAME_PATTERN.matcher(query).matches()) {
					UserFilterAction.Param param = new UserFilterAction.Param(UserFilterAction.Param.MUTE_USER, query);
					filterLoader.execute(param, this);
					return true;
				}
				Toast.makeText(getApplicationContext(), R.string.error_username_format, Toast.LENGTH_SHORT).show();
			} else if (viewPager.getCurrentItem() == 1) {
				if (USERNAME_PATTERN.matcher(query).matches()) {
					UserFilterAction.Param param = new UserFilterAction.Param(UserFilterAction.Param.BLOCK_USER, query);
					filterLoader.execute(param, this);
					return true;
				}
				Toast.makeText(getApplicationContext(), R.string.error_username_format, Toast.LENGTH_SHORT).show();
			} else if (viewPager.getCurrentItem() == 2) {
				if (Patterns.WEB_URL.matcher(query).matches()) {
					UserFilterAction.Param param = new UserFilterAction.Param(UserFilterAction.Param.BLOCK_DOMAIN, Uri.parse(query).getHost());
					filterLoader.execute(param, this);
					return true;
				}
				Toast.makeText(getApplicationContext(), R.string.error_domain_format, Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}


	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}


	@Override
	public void onResult(@NonNull UserFilterAction.Result result) {
		switch (result.mode) {
			case UserFilterAction.Result.MUTE_USER:
				Toast.makeText(getApplicationContext(), R.string.info_user_muted, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case UserFilterAction.Result.BLOCK_DOMAIN:
			case UserFilterAction.Result.BLOCK_USER:
				Toast.makeText(getApplicationContext(), R.string.info_blocked, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			default:
			case UserFilterAction.Result.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				break;
		}
	}
}