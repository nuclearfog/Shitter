package org.nuclearfog.twidda.ui.activities;

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
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.fragments.FragmentAdapter;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

/**
 * Main Activity of the App
 *
 * @author nuclearfog
 */
public class MainActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, OnTabSelectedListener, OnQueryTextListener {

	public static final String KEY_SELECT_NOTIFICATION = "main_notification";

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private FragmentAdapter adapter;
	private GlobalSettings settings;

	@Nullable
	private Intent loginIntent;

	private Dialog loadingCircle;
	private TabSelector tabSelector;
	private ViewPager2 viewPager;
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
		viewPager = findViewById(R.id.home_pager);
		tabSelector = findViewById(R.id.home_tab);
		root = findViewById(R.id.main_layout);
		loadingCircle = new ProgressDialog(this, null);

		settings = GlobalSettings.get(this);
		tabSelector.addViewPager(viewPager);
		viewPager.setOffscreenPageLimit(4);
		adapter = new FragmentAdapter(this);
		viewPager.setAdapter(adapter);

		AppStyles.setTheme(root);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		tabSelector.addOnTabSelectedListener(this);
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
			setupAdapter();
			if (getIntent().getBooleanExtra(KEY_SELECT_NOTIFICATION, false)) {
				// select notification page if user clicks on notification
				viewPager.setCurrentItem(adapter.getItemCount() - 1, false);
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
			case LoginActivity.RETURN_LOGIN_CANCELED:
				finish();
				break;

			case SettingsActivity.RETURN_APP_LOGOUT:
				adapter.clear();
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
				setupAdapter();
				break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		MenuItem search = menu.findItem(R.id.menu_search);
		MenuItem filter = menu.findItem(R.id.menu_filter);
		SearchView searchView = (SearchView) search.getActionView();
		filter.setVisible(settings.getLogin().getConfiguration().isFilterSupported());
		searchView.setOnQueryTextListener(this);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem message = menu.findItem(R.id.menu_message);
		MenuItem search = menu.findItem(R.id.menu_search);
		message.setVisible(settings.getLogin().getConfiguration().directmessageSupported());
		search.collapseActionView();
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// open home profile
		if (item.getItemId() == R.id.menu_profile) {
			Intent intent = new Intent(this, ProfileActivity.class);
			intent.putExtra(ProfileActivity.KEY_ID, settings.getLogin().getId());
			startActivity(intent);
			return true;
		}
		// open filter page
		else if (item.getItemId() == R.id.menu_filter) {
			Intent intent = new Intent(this, FilterActivity.class);
			startActivity(intent);
			return true;
		}
		// open status editor
		else if (item.getItemId() == R.id.menu_post) {
			Intent intent = new Intent(this, StatusEditor.class);
			startActivity(intent);
			return true;
		}
		// open app settings
		else if (item.getItemId() == R.id.menu_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			activityResultLauncher.launch(intent);
			return true;
		}
		// theme expanded search view
		else if (item.getItemId() == R.id.menu_search) {
			SearchView searchView = (SearchView) item.getActionView();
			AppStyles.setTheme(searchView, Color.TRANSPARENT);
			return true;
		}
		// open message editor
		else if (item.getItemId() == R.id.menu_message) {
			Intent intent = new Intent(this, MessageEditor.class);
			startActivity(intent);
			return true;
		}
		// open account manager
		else if (item.getItemId() == R.id.menu_account) {
			Intent intent = new Intent(this, AccountActivity.class);
			activityResultLauncher.launch(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		adapter.scrollToTop(oldPosition);
	}

	/**
	 * initialize pager content
	 */
	private void setupAdapter() {
		AppStyles.setTheme(root);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		adapter.setupForHomePage();
		tabSelector.addTabIcons(settings.getLogin().getConfiguration().getHomeTabIcons());
		tabSelector.updateTheme();
	}
}