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
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserFilterAction;
import org.nuclearfog.twidda.backend.async.UserFilterAction.FilterParam;
import org.nuclearfog.twidda.backend.async.UserFilterAction.FilterResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.viewpager.UserAdapter;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.util.regex.Pattern;

/**
 * Activity to show one or more lists of users
 *
 * @author nuclearfog
 */
public class UsersActivity extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener, AsyncCallback<FilterResult> {

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

	private TabSelector tabSelector;
	private ViewPager2 viewPager;

	private int mode;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(Bundle savedInst) {
		super.onCreate(savedInst);
		setContentView(R.layout.page_users);
		ViewGroup root = findViewById(R.id.page_users_root);
		Toolbar toolbar = findViewById(R.id.page_users_toolbar);
		tabSelector = findViewById(R.id.page_exclude_tab);
		viewPager = findViewById(R.id.page_users_pager);

		filterLoader = new UserFilterAction(this);
		settings = GlobalSettings.get(this);
		viewPager.setOffscreenPageLimit(3);

		mode = getIntent().getIntExtra(KEY_MODE, 0);
		long id = getIntent().getLongExtra(KEY_ID, 0L);

		switch (mode) {
			case USERS_FOLLOWING:
				toolbar.setTitle(R.string.userlist_following);
				adapter = new UserAdapter(this, id, UserAdapter.FOLLOWING);
				viewPager.setAdapter(adapter);
				if (adapter.getItemCount() > 1) {
					tabSelector.addTabIcons(R.array.user_following);
					tabSelector.addOnTabSelectedListener(this);
				} else {
					tabSelector.setVisibility(View.GONE);
				}
				break;

			case USERS_FOLLOWER:
				toolbar.setTitle(R.string.userlist_follower);
				adapter = new UserAdapter(this, id, UserAdapter.FOLLOWER);
				viewPager.setAdapter(adapter);
				tabSelector.setVisibility(View.GONE);
				if (adapter.getItemCount() > 1) {
					tabSelector.addTabIcons(R.array.user_follower);
					tabSelector.addOnTabSelectedListener(this);
				} else {
					tabSelector.setVisibility(View.GONE);
				}
				break;

			case USERS_REPOST:
				toolbar.setTitle(R.string.toolbar_userlist_repost);
				adapter = new UserAdapter(this, id, UserAdapter.REPOSTER);
				viewPager.setAdapter(adapter);
				tabSelector.setVisibility(View.GONE);
				break;

			case USERS_FAVORIT:
				int title = settings.likeEnabled() ? R.string.toolbar_status_liker : R.string.toolbar_status_favoriter;
				toolbar.setTitle(title);
				adapter = new UserAdapter(this, id, UserAdapter.FAVORITER);
				viewPager.setAdapter(adapter);
				tabSelector.setVisibility(View.GONE);
				break;

			case USERS_EXCLUDED:
				toolbar.setTitle(R.string.menu_toolbar_excluded_users);
				adapter = new UserAdapter(this, id, UserAdapter.BLOCKS);
				viewPager.setAdapter(adapter);
				tabSelector.addTabIcons(R.array.user_domain_exclude);
				tabSelector.addOnTabSelectedListener(this);
				break;

			default:
				finish();
				return;
		}
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
	}


	@Override
	public void onBackPressed() {
		if (tabSelector.getVisibility() == View.VISIBLE && viewPager.getCurrentItem() > 0) {
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
	public void onTabSelected(int oldPosition) {
		adapter.scrollToTop(oldPosition);
		// reset menu
		invalidateOptionsMenu();
	}


	@Override
	public boolean onQueryTextSubmit(String query) {
		if (!filterLoader.isIdle())
			return false;
		if (viewPager.getCurrentItem() == 0) {
			if (USERNAME_PATTERN.matcher(query).matches()) {
				FilterParam param = new FilterParam(FilterParam.MUTE_USER, query);
				filterLoader.execute(param, this);
				return true;
			}
			Toast.makeText(getApplicationContext(), R.string.error_username_format, Toast.LENGTH_SHORT).show();
		} else if (viewPager.getCurrentItem() == 1) {
			if (USERNAME_PATTERN.matcher(query).matches()) {
				FilterParam param = new FilterParam(FilterParam.BLOCK_USER, query);
				filterLoader.execute(param, this);
				return true;
			}
			Toast.makeText(getApplicationContext(), R.string.error_username_format, Toast.LENGTH_SHORT).show();
		} else if (viewPager.getCurrentItem() == 2) {
			if (Patterns.WEB_URL.matcher(query).matches()) {
				FilterParam param = new FilterParam(FilterParam.BLOCK_DOMAIN, Uri.parse(query).getHost());
				filterLoader.execute(param, this);
				return true;
			}
			Toast.makeText(getApplicationContext(), R.string.error_domain_format, Toast.LENGTH_SHORT).show();
		}
		return false;
	}


	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}


	@Override
	public void onResult(@NonNull FilterResult result) {
		switch (result.mode) {
			case FilterResult.MUTE_USER:
				Toast.makeText(getApplicationContext(), R.string.info_user_muted, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case FilterResult.BLOCK_DOMAIN:
			case FilterResult.BLOCK_USER:
				Toast.makeText(getApplicationContext(), R.string.info_blocked, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			default:
			case FilterResult.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				break;
		}
	}
}