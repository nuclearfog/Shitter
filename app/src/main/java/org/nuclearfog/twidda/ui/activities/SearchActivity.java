package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TagAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LandscapePageTransformer;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Tag;
import org.nuclearfog.twidda.ui.adapter.viewpager.SearchAdapter;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.io.Serializable;

/**
 * search Activity for statuses and users
 *
 * @author nuclearfog
 */
public class SearchActivity extends AppCompatActivity implements OnClickListener, OnTabSelectedListener, OnQueryTextListener, AsyncCallback<TagAction.Result> {

	/**
	 * Key for the search query, required
	 * value type is String
	 */
	public static final String KEY_QUERY = "search_query";

	/**
	 * key to add trend information to search for
	 * value type is {@link Tag}
	 */
	public static final String KEY_DATA = "trend_data";

	public static final int RETURN_TREND = 0x2735;

	public static final int SEARCH_STR_MAX_LEN = 128;

	private TagAction tagAction;

	private SearchAdapter adapter;
	private GlobalSettings settings;
	private ViewPager2 viewPager;
	private Toolbar toolbar;

	private String search = "";
	@Nullable
	private Tag tag;


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
		tagAction = new TagAction(this);
		adapter = new SearchAdapter(this);

		String query = getIntent().getStringExtra(KEY_QUERY);
		Serializable data = getIntent().getSerializableExtra(KEY_DATA);
		if (data instanceof Tag) {
			tag = (Tag) data;
			search = tag.getName();
		} else if (query != null) {
			search = query;
			if (search.matches("^#\\S+") && !search.matches("^#\\d+")) {
				TagAction.Param param = new TagAction.Param(TagAction.Param.LOAD, search);
				tagAction.execute(param, this);
			}
		}
		if (settings.floatingButtonEnabled()) {
			floatingButton.setVisibility(View.VISIBLE);
		}
		tabSelector.setLargeIndicator(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		adapter.setSearch(search);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		viewPager.setPageTransformer(new LandscapePageTransformer());
		viewPager.setOffscreenPageLimit(3);
		viewPager.setAdapter(adapter);
		tabSelector.addTabIcons(R.array.search_tag_tab_icons);
		AppStyles.setTheme(root);

		tabSelector.addOnTabSelectedListener(this);
		floatingButton.setOnClickListener(this);
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			if (tag != null) {
				Intent intent = new Intent();
				intent.putExtra(KEY_DATA, tag);
				setResult(RETURN_TREND, intent);
			}
			super.onBackPressed();
		}
	}


	@Override
	protected void onDestroy() {
		tagAction.cancel();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		MenuItem search_item = menu.findItem(R.id.new_search);
		MenuItem filter_item = menu.findItem(R.id.search_filter);
		MenuItem tag_item = menu.findItem(R.id.search_tag);
		MenuItem post_item = menu.findItem(R.id.search_status);
		SearchView searchView = (SearchView) search_item.getActionView();

		boolean enableSearchFilter = settings.getLogin().getConfiguration().filterEnabled();
		post_item.setVisible(!settings.floatingButtonEnabled());
		filter_item.setVisible(enableSearchFilter);
		filter_item.setChecked(settings.filterResults() & enableSearchFilter);
		searchView.setQueryHint(search);
		if (tag != null && tag.getName().startsWith("#")) {
			tag_item.setVisible(true);
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
		MenuItem tag_item = menu.findItem(R.id.search_tag);
		// set menu option depending on trend follow status
		if (tag != null) {
			if (tag.isFollowed()) {
				tag_item.setTitle(R.string.menu_tag_unfollow);
			} else {
				tag_item.setTitle(R.string.menu_tag_follow);
			}
			return true;
		}
		return false;
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
		// follow/unfollow tag
		else if (item.getItemId() == R.id.search_tag) {
			if (tag != null && tagAction.isIdle()) {
				TagAction.Param param;
				if (tag.isFollowed())
					param = new TagAction.Param(TagAction.Param.UNFOLLOW, tag.getName());
				else
					param = new TagAction.Param(TagAction.Param.FOLLOW, tag.getName());
				tagAction.execute(param, this);
			}
			return true;
		}
		return false;
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
	public void onTabSelected() {
		invalidateOptionsMenu();
		adapter.scrollToTop();
	}


	@Override
	public void onResult(@NonNull TagAction.Result result) {
		if (result.tag != null) {
			this.tag = result.tag;
			invalidateMenu();
		}
		switch (result.action) {
			case TagAction.Result.FOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_tag_followed, Toast.LENGTH_SHORT).show();
				break;

			case TagAction.Result.UNFOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_tag_unfollowed, Toast.LENGTH_SHORT).show();
				break;

			case TagAction.Result.ERROR:
				if (result.exception == null || result.exception.getErrorCode() != ConnectionException.HTTP_FORBIDDEN)
					ErrorUtils.showErrorMessage(this, result.exception);
				break;
		}
	}
}