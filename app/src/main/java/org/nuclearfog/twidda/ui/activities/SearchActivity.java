package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.HashtagAction;
import org.nuclearfog.twidda.backend.async.HashtagAction.Param;
import org.nuclearfog.twidda.backend.async.HashtagAction.Result;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Hashtag;
import org.nuclearfog.twidda.ui.adapter.viewpager.SearchAdapter;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.io.Serializable;

/**
 * search Activity for statuses and users
 *
 * @author nuclearfog
 */
public class SearchActivity extends AppCompatActivity implements OnClickListener, OnTabSelectedListener, OnQueryTextListener, AsyncCallback<Result> {

	/**
	 * Key for the search query, required
	 * value type is String
	 */
	public static final String KEY_QUERY = "search_query";

	/**
	 * key to add trend information to search for
	 * value type is {@link Hashtag}
	 */
	public static final String KEY_DATA = "trend_data";

	public static final int RETURN_TREND = 0x2735;

	public static final int SEARCH_STR_MAX_LEN = 128;

	private HashtagAction hashtagAction;

	private SearchAdapter adapter;
	private GlobalSettings settings;
	private ViewPager2 viewPager;
	private Toolbar toolbar;

	private String search = "";
	@Nullable
	private Hashtag hashtag;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_tab_view);
		ViewGroup root = findViewById(R.id.page_tab_view_root);
		TabSelector tabSelector = findViewById(R.id.page_tab_view_tabs);
		View floatingButton = findViewById(R.id.page_tab_view_post_button);
		toolbar = findViewById(R.id.page_tab_view_toolbar);
		viewPager = findViewById(R.id.page_tab_view_pager);

		settings = GlobalSettings.get(this);
		hashtagAction = new HashtagAction(this);

		String query = getIntent().getStringExtra(KEY_QUERY);
		Serializable data = getIntent().getSerializableExtra(KEY_DATA);
		if (data instanceof Hashtag) {
			hashtag = (Hashtag) data;
			search = hashtag.getName();
		} else if (query != null) {
			search = query;
			if (search.startsWith("#") && search.matches("\\S+")) {
				Param param = new Param(Param.LOAD, search);
				hashtagAction.execute(param, this);
			}
		}
		if (!settings.floatingButtonEnabled()) {
			floatingButton.setVisibility(View.INVISIBLE);
		}
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		viewPager.setOffscreenPageLimit(3);
		adapter = new SearchAdapter(this, search);
		viewPager.setAdapter(adapter);
		if (adapter.getItemCount() == 3)
			tabSelector.addTabIcons(R.array.search_hashtag_tab_icons);
		else
			tabSelector.addTabIcons(R.array.search_tab_icons);
		AppStyles.setTheme(root);

		tabSelector.addOnTabSelectedListener(this);
		floatingButton.setOnClickListener(this);
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			if (hashtag != null) {
				Intent intent = new Intent();
				intent.putExtra(KEY_DATA, hashtag);
				setResult(RETURN_TREND, intent);
			}
			super.onBackPressed();
		}
	}


	@Override
	protected void onDestroy() {
		hashtagAction.cancel();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		MenuItem searchItem = menu.findItem(R.id.new_search);
		MenuItem searchFilter = menu.findItem(R.id.search_filter);
		MenuItem menuHashtag = menu.findItem(R.id.search_hashtag);
		SearchView searchView = (SearchView) searchItem.getActionView();

		boolean enableSearchFilter = settings.getLogin().getConfiguration().filterEnabled();
		searchFilter.setVisible(enableSearchFilter);
		searchFilter.setChecked(settings.filterResults() & enableSearchFilter);
		searchView.setQueryHint(search);
		if (hashtag != null && hashtag.getName().startsWith("#")) {
			menuHashtag.setVisible(true);
		}
		// set theme
		AppStyles.setTheme(searchView, Color.TRANSPARENT);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());

		searchView.setOnQueryTextListener(this);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuHashtag = menu.findItem(R.id.search_hashtag);
		// set menu option depending on trend follow status
		if (hashtag != null) {
			if (hashtag.following()) {
				menuHashtag.setTitle(R.string.menu_hashtag_unfollow);
			} else {
				menuHashtag.setTitle(R.string.menu_hashtag_follow);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// create new status
		if (item.getItemId() == R.id.search_status) {
			Intent intent = new Intent(this, StatusEditor.class);
			if (search.startsWith("#"))
				intent.putExtra(StatusEditor.KEY_TEXT, search + " ");
			startActivity(intent);
			return true;
		}
		// theme expanded search view
		else if (item.getItemId() == R.id.new_search) {
			SearchView searchView = (SearchView) item.getActionView();
			AppStyles.setTheme(searchView, Color.TRANSPARENT);
			return true;
		}
		// enable/disable search filter
		else if (item.getItemId() == R.id.search_filter) {
			boolean enable = !settings.filterResults();
			settings.setFilterResults(enable);
			item.setChecked(enable);
			return true;
		}
		// follow/unfollow hashtag
		else if (item.getItemId() == R.id.search_hashtag) {
			if (hashtag != null && hashtagAction.isIdle()) {
				Param param;
				if (hashtag.following())
					param = new Param(Param.UNFOLLOW, hashtag.getName());
				else
					param = new Param(Param.FOLLOW, hashtag.getName());
				hashtagAction.execute(param, this);
			}
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onQueryTextSubmit(String s) {
		if (s.length() <= SearchActivity.SEARCH_STR_MAX_LEN && !s.contains(":") && !s.contains("$")) {
			Intent search = new Intent(this, SearchActivity.class);
			search.putExtra(KEY_QUERY, s);
			startActivity(search);
			return true;
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_search, Toast.LENGTH_SHORT).show();
			return false;
		}
	}


	@Override
	public boolean onQueryTextChange(String s) {
		return false;
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.page_tab_view_post_button) {
			Intent intent = new Intent(this, StatusEditor.class);
			if (search.startsWith("#"))
				intent.putExtra(StatusEditor.KEY_TEXT, search + " ");
			startActivity(intent);
		}
	}


	@Override
	public void onTabSelected(int oldPosition) {
		invalidateOptionsMenu();
		adapter.scrollToTop(oldPosition);
	}


	@Override
	public void onResult(@NonNull Result result) {
		if (result.hashtag != null) {
			this.hashtag = result.hashtag;
			invalidateMenu();
		}
		switch (result.mode) {
			case Result.FOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_hashtag_followed, Toast.LENGTH_SHORT).show();
				break;

			case Result.UNFOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_hashtag_unfollowed, Toast.LENGTH_SHORT).show();
				break;

			case Result.ERROR:
				ErrorUtils.showErrorMessage(this, result.exception);
				break;
		}
	}
}