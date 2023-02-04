package org.nuclearfog.twidda.ui.activities;

import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.ui.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.LinkLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;

/**
 * Main Activity of the App
 *
 * @author nuclearfog
 */
public class MainActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, OnTabSelectedListener, OnQueryTextListener {

	/**
	 * key used to set the tab page
	 * vale type is Integer
	 */
	public static final String KEY_TAB_PAGE = "tab_pos";

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private FragmentAdapter adapter;
	private GlobalSettings settings;
	private Intent loginIntent;

	private Dialog loadingCircle;
	private TabLayout tabLayout;
	private ViewPager pager;
	private Toolbar toolbar;
	private ViewGroup root;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_main);
		toolbar = findViewById(R.id.home_toolbar);
		pager = findViewById(R.id.home_pager);
		tabLayout = findViewById(R.id.home_tab);
		root = findViewById(R.id.main_layout);
		loadingCircle = new ProgressDialog(this);

		settings = GlobalSettings.getInstance(this);
		tabLayout.setupWithViewPager(pager);
		pager.setOffscreenPageLimit(4);
		adapter = new FragmentAdapter(this, getSupportFragmentManager());

		AppStyles.setTheme(root);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		tabLayout.addOnTabSelectedListener(this);
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
		else if (adapter.isEmpty()) {
			setupAdapter(true);
			// check if there is a Twitter link
			if (getIntent().getData() != null) {
				LinkLoader linkLoader = new LinkLoader(this);
				linkLoader.execute(getIntent().getData());
				loadingCircle.show();
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
		switch (result.getResultCode()) {
			case LoginActivity.RETURN_LOGIN_CANCELED:
				finish();
				break;

			case LoginActivity.RETURN_LOGIN_SUCCESSFUL:
			case AccountActivity.RETURN_ACCOUNT_CHANGED:
				setupAdapter(true);
				break;

			case SettingsActivity.RETURN_APP_LOGOUT:
				adapter.clear();
				pager.setAdapter(adapter);
				loginIntent = new Intent(this, LoginActivity.class);
				activityResultLauncher.launch(loginIntent);
				break;

			default:
			case SettingsActivity.RETURN_SETTINGS_CHANGED:
			case AccountActivity.RETURN_SETTINGS_CHANGED:
				adapter.notifySettingsChanged();
				setupAdapter(false);
				break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.home, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		MenuItem search = m.findItem(R.id.menu_search);
		MenuItem localTl = m.findItem(R.id.menu_local_timeline);
		SearchView searchView = (SearchView) search.getActionView();
		localTl.setVisible(settings.getLogin().getConfiguration() == Configuration.MASTODON);
		localTl.setChecked(settings.useLocalTimeline());
		searchView.setOnQueryTextListener(this);
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// open home profile
		if (item.getItemId() == R.id.menu_profile) {
			Intent user = new Intent(this, ProfileActivity.class);
			user.putExtra(KEY_PROFILE_ID, settings.getLogin().getId());
			startActivity(user);
		}
		// open status editor
		else if (item.getItemId() == R.id.menu_post) {
			Intent intent = new Intent(this, StatusEditor.class);
			startActivity(intent);
		}
		// open app settings
		else if (item.getItemId() == R.id.menu_settings) {
			Intent settings = new Intent(this, SettingsActivity.class);
			activityResultLauncher.launch(settings);
		}
		// theme expanded search view
		else if (item.getItemId() == R.id.menu_search) {
			SearchView searchView = (SearchView) item.getActionView();
			AppStyles.setTheme(searchView, Color.TRANSPARENT);
		}
		// open message editor
		else if (item.getItemId() == R.id.menu_message) {
			Intent intent = new Intent(this, MessageEditor.class);
			startActivity(intent);
		}
		// open account manager
		else if (item.getItemId() == R.id.menu_account) {
			Intent accountManager = new Intent(this, AccountActivity.class);
			activityResultLauncher.launch(accountManager);
		}
		// enable disable Mastodon local timeline
		else if (item.getItemId() == R.id.menu_local_timeline) {
			boolean toggle = !settings.useLocalTimeline();
			settings.setLocalTimeline(toggle);
			adapter.notifySettingsChanged();
			item.setChecked(toggle);
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		if (tabLayout.getSelectedTabPosition() > 0) {
			pager.setCurrentItem(0);
		} else {
			super.onBackPressed();
		}
	}


	@Override
	public boolean onQueryTextSubmit(String s) {
		if (s.length() <= SearchActivity.SEARCH_STR_MAX_LEN && !s.contains(":") && !s.contains("$")) {
			Intent search = new Intent(this, SearchActivity.class);
			search.putExtra(KEY_SEARCH_QUERY, s);
			startActivity(search);
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_twitter_search, Toast.LENGTH_SHORT).show();
		}
		return false;
	}


	@Override
	public boolean onQueryTextChange(String s) {
		return false;
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

	/**
	 * called from {@link LinkLoader} when link information were successfully loaded
	 *
	 * @param holder holder with activity information and extras
	 */
	public void onSuccess(@NonNull LinkLoader.DataHolder holder) {
		loadingCircle.dismiss();
		if (holder.activity == MainActivity.class) {
			int page = holder.data.getInt(KEY_TAB_PAGE, 0);
			pager.setCurrentItem(page);
		} else {
			Intent intent = new Intent(this, holder.activity);
			intent.putExtras(holder.data);
			startActivity(intent);
		}
	}

	/**
	 * called from {@link LinkLoader} when an error occurs
	 */
	public void onError(@Nullable ConnectionException exception) {
		if (exception != null) {
			String message = ErrorHandler.getErrorMessage(this, exception);
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_open_link, Toast.LENGTH_SHORT).show();
		}
		loadingCircle.dismiss();
	}

	/**
	 * initialize pager content
	 */
	private void setupAdapter(boolean resetFragments) {
		AppStyles.setTheme(root);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		if (resetFragments) {
			adapter.setupForHomePage();
			pager.setAdapter(adapter);
		}
		switch (settings.getLogin().getConfiguration()) {
			case TWITTER1:
			case TWITTER2:
				AppStyles.setTabIcons(tabLayout, settings, R.array.home_twitter_icons);
				break;

			case MASTODON:
				AppStyles.setTabIcons(tabLayout, settings, R.array.home_mastodon_icons);
				break;
		}
	}
}