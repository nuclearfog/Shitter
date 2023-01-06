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
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.LinkLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;

/**
 * Main Activity of the App
 *
 * @author nuclearfog
 */
public class MainActivity extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener {

	/**
	 * key used to set the tab page
	 * vale type is Integer
	 */
	public static final String KEY_TAB_PAGE = "tab_pos";

	/**
	 * request code to start {@link LoginActivity}
	 */
	private static final int REQUEST_APP_LOGIN = 0x6A89;

	/**
	 * Request code to start {@link AccountActivity}
	 */
	private static final int REQUEST_ACCOUNT_CHANGE = 0x345;

	/**
	 * Request code to start {@link SettingsActivity}
	 */
	private static final int REQUEST_APP_SETTINGS = 0x54AD;

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
		adapter = new FragmentAdapter(this, getSupportFragmentManager());
		pager.setOffscreenPageLimit(3);
		pager.setAdapter(adapter);
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
			// prevent creating login activity twice
			loginIntent = new Intent(this, LoginActivity.class);
			startActivityForResult(loginIntent, REQUEST_APP_LOGIN);
		}
		// initialize lists
		else if (adapter.isEmpty()) {
			adapter.setupForHomePage();
			if (settings.getLogin().getApiType() == Account.API_TWITTER) {
				AppStyles.setTabIcons(tabLayout, settings, R.array.home_twitter_icons);
			} else if (settings.getLogin().getApiType() == Account.API_MASTODON) {
				AppStyles.setTabIcons(tabLayout, settings, R.array.home_mastodon_icons);
			}
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
	protected void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
		super.onActivityResult(reqCode, returnCode, intent);
		switch (reqCode) {
			case REQUEST_APP_LOGIN:
				// check if app login cancelled
				if (returnCode == RESULT_CANCELED) {
					finish();
				}
				// check if account changed
				else if (returnCode == LoginActivity.RETURN_LOGIN_SUCCESSFUL) {
					adapter.setupForHomePage();
				}
				break;

			case REQUEST_ACCOUNT_CHANGE:
				// check if account or theme changed
				if (returnCode == AccountActivity.RETURN_ACCOUNT_CHANGED || returnCode == AccountActivity.RETURN_SETTINGS_CHANGED) {
					adapter.clear();
					pager.setAdapter(adapter);
					adapter.notifySettingsChanged();
				}
				break;

			case REQUEST_APP_SETTINGS:
				// check if an account was removed
				if (returnCode == SettingsActivity.RETURN_APP_LOGOUT) {
					// clear old login fragments
					adapter.clear();
					pager.setAdapter(adapter);
				}
				// reset fragments to apply changes
				else {
					adapter.notifySettingsChanged();
				}
				break;
		}
		AppStyles.setTheme(root);
		if (settings.getLogin().getApiType() == Account.API_TWITTER) {
			AppStyles.setTabIcons(tabLayout, settings, R.array.home_twitter_icons);
		} else if (settings.getLogin().getApiType() == Account.API_MASTODON) {
			AppStyles.setTabIcons(tabLayout, settings, R.array.home_mastodon_icons);
		}
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.home, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		MenuItem search = m.findItem(R.id.menu_search);
		SearchView searchView = (SearchView) search.getActionView();
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
			startActivityForResult(settings, REQUEST_APP_SETTINGS);
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
			startActivityForResult(accountManager, REQUEST_ACCOUNT_CHANGE);
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
			Toast.makeText(this, R.string.error_twitter_search, Toast.LENGTH_SHORT).show();
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
	public void onSuccess(@Nullable LinkLoader.DataHolder holder) {
		loadingCircle.dismiss();
		if (holder != null) {
			if (holder.activity == MainActivity.class) {
				int page = holder.data.getInt(KEY_TAB_PAGE, 0);
				pager.setCurrentItem(page);
			} else {
				Intent intent = new Intent(this, holder.activity);
				intent.putExtras(holder.data);
				startActivity(intent);
			}
		} else {
			Toast.makeText(this, R.string.error_open_link, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * called from {@link LinkLoader} when an error occurs
	 */
	public void onError(ConnectionException error) {
		ErrorHandler.handleFailure(this, error);
		loadingCircle.dismiss();
	}
}