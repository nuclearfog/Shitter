package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.LandscapePageTransformer;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.adapter.viewpager.UserListsAdapter;
import org.nuclearfog.twidda.ui.dialogs.UserlistDialog;
import org.nuclearfog.twidda.ui.dialogs.UserlistDialog.UserlistUpdatedCallback;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

/**
 * Activity to show userlists of an user
 *
 * @author nuclearfog
 */
public class UserlistsActivity extends AppCompatActivity implements UserlistUpdatedCallback, OnTabSelectedListener {

	/**
	 * Key for the ID the list owner
	 * value type is Long
	 */
	public static final String KEY_ID = "userlist-owner-id";

	private ViewPager2 viewPager;

	private UserListsAdapter adapter;
	private GlobalSettings settings;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.page_tab_view);
		ViewGroup root = findViewById(R.id.page_tab_view_root);
		Toolbar toolbar = findViewById(R.id.page_tab_view_toolbar);
		TabSelector tabSelector = findViewById(R.id.page_tab_view_tabs);
		viewPager = findViewById(R.id.page_tab_view_pager);

		adapter = new UserListsAdapter(this);
		settings = GlobalSettings.get(this);

		long ownerId = getIntent().getLongExtra(KEY_ID, 0L);
		adapter.setId(ownerId);
		if (settings.getLogin().getConfiguration().isUserlistMembershipSupported()) {
			tabSelector.addTabIcons(R.array.userlist_tab_ownership_membership_icons);
			adapter.setPageCount(2);
		} else {
			tabSelector.addTabIcons(R.array.userlist_tab_ownership_icons);
			adapter.setPageCount(1);
		}
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(2);
		viewPager.setPageTransformer(new LandscapePageTransformer());
		tabSelector.setLargeIndicator(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		toolbar.setTitle(R.string.list_appbar);
		setSupportActionBar(toolbar);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		AppStyles.setTheme(root);

		tabSelector.addOnTabSelectedListener(this);
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			super.onBackPressed();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.lists, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// open list editor
		if (item.getItemId() == R.id.list_create) {
			UserlistDialog.show(this, null);
			return true;
		}
		// open mute/block list
		else if (item.getItemId() == R.id.list_blocklists) {
			Intent usersIntent = new Intent(this, UsersActivity.class);
			usersIntent.putExtra(UsersActivity.KEY_MODE, UsersActivity.USERS_EXCLUDED);
			startActivity(usersIntent);
			return true;
		}
		return false;
	}


	@Override
	public void onTabSelected() {
		adapter.scrollToTop();
	}


	@Override
	public void onUserlistUpdate(UserList userlist) {
		adapter.notifySettingsChanged();
	}
}