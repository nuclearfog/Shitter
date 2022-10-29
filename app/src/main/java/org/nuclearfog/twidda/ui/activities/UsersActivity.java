package org.nuclearfog.twidda.ui.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.backend.async.FilterLoader.BLOCK_USER;
import static org.nuclearfog.twidda.backend.async.FilterLoader.MUTE_USER;
import static org.nuclearfog.twidda.backend.async.FilterLoader.REFRESH;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.FilterLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Activity to show lists of Twitter users
 *
 * @author nuclearfog
 */
public class UsersActivity extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener {

	/**
	 * type of users to get from twitter
	 * {@link #USERLIST_FRIENDS,#USERLIST_FOLLOWER,#USERLIST_RETWEETS,#USERLIST_FAVORIT,#USERLIST_EXCLUDED_USERS,#USERLIST_REQUESTS}
	 */
	public static final String KEY_USERDETAIL_MODE = "userlist_mode";

	/**
	 * ID of a userlist, an user or a tweet to get the users from
	 * value type is Long
	 */
	public static final String KEY_USERDETAIL_ID = "userlist_id";

	/**
	 * friends of an user, requires user ID
	 *
	 * @see #KEY_USERDETAIL_MODE
	 */
	public static final int USERLIST_FRIENDS = 0xDF893242;

	/**
	 * follower of an user, requires user ID
	 *
	 * @see #KEY_USERDETAIL_MODE
	 */
	public static final int USERLIST_FOLLOWER = 0xA89F5968;

	/**
	 * user retweeting a tweet, requires tweet ID
	 *
	 * @see #KEY_USERDETAIL_MODE
	 */
	public static final int USERLIST_RETWEETS = 0x19F582E;

	/**
	 * user favoriting/liking a tweet, requires tweet ID
	 *
	 * @see #KEY_USERDETAIL_MODE
	 */
	public static final int USERLIST_FAVORIT = 0x9bcc3f99;

	/**
	 * setup list to show excluded (muted, blocked) users
	 *
	 * @see #KEY_USERDETAIL_MODE
	 */
	public static final int USERLIST_EXCLUDED_USERS = 0x896a786;

	/**
	 * setup list to show incoming & outgoing follow requests
	 *
	 * @see #KEY_USERDETAIL_MODE
	 */
	public static final int USERLIST_REQUESTS = 0x0948693;

	private GlobalSettings settings;
	private FilterLoader userExclTask;
	private FragmentAdapter adapter;

	private Toolbar toolbar;
	private TabLayout tablayout;
	private ViewPager pager;

	private int mode;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(Bundle savedInst) {
		super.onCreate(savedInst);
		setContentView(R.layout.page_user_exclude);
		ViewGroup root = findViewById(R.id.userexclude_root);
		toolbar = findViewById(R.id.userexclude_toolbar);
		tablayout = findViewById(R.id.userexclude_tab);
		pager = findViewById(R.id.userexclude_pager);

		adapter = new FragmentAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);

		settings = GlobalSettings.getInstance(this);

		mode = getIntent().getIntExtra(KEY_USERDETAIL_MODE, 0);
		long id = getIntent().getLongExtra(KEY_USERDETAIL_ID, -1);

		switch (mode) {
			case USERLIST_FRIENDS:
				adapter.setupFollowingPage(id);
				pager.setOffscreenPageLimit(1);
				tablayout.setVisibility(View.GONE);
				toolbar.setTitle(R.string.userlist_following);
				break;

			case USERLIST_FOLLOWER:
				adapter.setupFollowerPage(id);
				pager.setOffscreenPageLimit(1);
				tablayout.setVisibility(View.GONE);
				toolbar.setTitle(R.string.userlist_follower);
				break;

			case USERLIST_RETWEETS:
				adapter.setupRetweeterPage(id);
				pager.setOffscreenPageLimit(1);
				tablayout.setVisibility(View.GONE);
				toolbar.setTitle(R.string.toolbar_userlist_retweet);
				break;

			case USERLIST_FAVORIT:
				int title = settings.likeEnabled() ? R.string.toolbar_tweet_liker : R.string.toolbar_tweet_favoriter;
				adapter.setFavoriterPage(id);
				pager.setOffscreenPageLimit(1);
				tablayout.setVisibility(View.GONE);
				toolbar.setTitle(title);
				break;

			case USERLIST_EXCLUDED_USERS:
				adapter.setupMuteBlockPage();
				pager.setOffscreenPageLimit(2);
				tablayout.setupWithViewPager(pager);
				tablayout.addOnTabSelectedListener(this);
				AppStyles.setTabIcons(tablayout, settings, R.array.user_exclude_icons);
				toolbar.setTitle(R.string.menu_toolbar_excluded_users);
				break;

			case USERLIST_REQUESTS:
				adapter.setupFollowRequestPage();
				pager.setOffscreenPageLimit(2);
				tablayout.setupWithViewPager(pager);
				tablayout.addOnTabSelectedListener(this);
				AppStyles.setTabIcons(tablayout, settings, R.array.user_requests_icon);
				toolbar.setTitle(R.string.menu_toolbar_request);
				break;
		}
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root, settings.getBackgroundColor());
	}


	@Override
	public void onBackPressed() {
		if (tablayout.getVisibility() == View.VISIBLE && tablayout.getSelectedTabPosition() > 0) {
			pager.setCurrentItem(0);
		} else {
			super.onBackPressed();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		if (mode == USERLIST_EXCLUDED_USERS) {
			getMenuInflater().inflate(R.menu.excludelist, m);
			MenuItem search = m.findItem(R.id.menu_exclude_user);
			SearchView searchView = (SearchView) search.getActionView();
			searchView.setOnQueryTextListener(this);
			AppStyles.setTheme(searchView, Color.TRANSPARENT);
			AppStyles.setMenuIconColor(m, settings.getIconColor());
			AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
			return super.onCreateOptionsMenu(m);
		}
		return false;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu m) {
		if (mode == USERLIST_EXCLUDED_USERS) {
			SearchView searchView = (SearchView) m.findItem(R.id.menu_exclude_user).getActionView();
			if (tablayout.getSelectedTabPosition() == 0) {
				String hint = getString(R.string.menu_hint_mute_user);
				searchView.setQueryHint(hint);
			} else if (tablayout.getSelectedTabPosition() == 1) {
				String hint = getString(R.string.menu_hint_block_user);
				searchView.setQueryHint(hint);
			}
			return super.onPrepareOptionsMenu(m);
		}
		return false;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_exclude_refresh) {
			if (userExclTask == null || userExclTask.getStatus() != RUNNING) {
				Toast.makeText(this, R.string.info_refreshing_exclude_list, Toast.LENGTH_SHORT).show();
				userExclTask = new FilterLoader(this, REFRESH);
				userExclTask.execute();
			}
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onTabSelected(Tab tab) {
		// reset menu
		invalidateOptionsMenu();
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
	public boolean onQueryTextSubmit(String query) {
		if (userExclTask == null || userExclTask.getStatus() != RUNNING) {
			if (tablayout.getSelectedTabPosition() == 0) {
				userExclTask = new FilterLoader(this, MUTE_USER);
				userExclTask.execute(query);
				return true;
			}
			if (tablayout.getSelectedTabPosition() == 1) {
				userExclTask = new FilterLoader(this, BLOCK_USER);
				userExclTask.execute(query);
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	/**
	 * called from {@link FilterLoader} if task finished successfully
	 */
	public void onSuccess(int mode) {
		switch (mode) {
			case MUTE_USER:
				Toast.makeText(this, R.string.info_user_muted, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case BLOCK_USER:
				Toast.makeText(this, R.string.info_user_blocked, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case REFRESH:
				Toast.makeText(this, R.string.info_exclude_list_updated, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	/**
	 * called from {@link FilterLoader} if an error occurs
	 */
	public void onError(ConnectionException err) {
		ErrorHandler.handleFailure(this, err);
	}
}