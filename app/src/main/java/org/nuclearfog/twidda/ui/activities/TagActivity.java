package org.nuclearfog.twidda.ui.activities;

import android.content.res.Configuration;
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
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.TagAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.viewpager.TagAdapter;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

/**
 * Activity class used to show tag following/featuring
 *
 * @author nuclearfog
 */
public class TagActivity extends AppCompatActivity implements OnQueryTextListener, OnTabSelectedListener, AsyncCallback<TagAction.Result> {

	private GlobalSettings settings;
	private TagAction tagAction;
	private TagAdapter adapter;

	private ViewPager2 viewPager;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_tab_view);
		ViewGroup root = findViewById(R.id.page_tab_view_root);
		Toolbar toolbar = findViewById(R.id.page_tab_view_toolbar);
		TabSelector tabSelector = findViewById(R.id.page_tab_view_tabs);
		viewPager = findViewById(R.id.page_tab_view_pager);

		tagAction = new TagAction(this);
		settings = GlobalSettings.get(this);
		adapter = new TagAdapter(this);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(3);

		tabSelector.setLargeIndicator(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		tabSelector.addTabIcons(R.array.tabs_tag_icons);
		tabSelector.addTabLabels(R.array.tag_labels);

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);

		tabSelector.addOnTabSelectedListener(this);
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.tags, menu);
		MenuItem search = menu.findItem(R.id.menu_tag_add);
		SearchView searchView = (SearchView) search.getActionView();
		searchView.setQueryHint(getString(R.string.menu_add_tag));
		searchView.setOnQueryTextListener(this);
		AppStyles.setTheme(searchView, Color.TRANSPARENT);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem search = menu.findItem(R.id.menu_tag_add);
		search.collapseActionView();
		search.setVisible(viewPager.getCurrentItem() != 2);
		return true;
	}


	@Override
	public boolean onQueryTextSubmit(String query) {
		if (tagAction.isIdle()) {
			if (viewPager.getCurrentItem() == 0) {
				Toast.makeText(getApplicationContext(), R.string.info_tag_following, Toast.LENGTH_SHORT).show();
				TagAction.Param param = new TagAction.Param(TagAction.Param.FOLLOW, query);
				tagAction.execute(param, this);
				return true;
			} else if (viewPager.getCurrentItem() == 1) {
				Toast.makeText(getApplicationContext(), R.string.info_tag_featuring, Toast.LENGTH_SHORT).show();
				TagAction.Param param = new TagAction.Param(TagAction.Param.FEATURE, query);
				tagAction.execute(param, this);
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}


	@Override
	public void onResult(@NonNull TagAction.Result result) {
		switch (result.mode) {
			case TagAction.Result.FEATURE:
				Toast.makeText(getApplicationContext(), R.string.info_tag_featured, Toast.LENGTH_SHORT).show();
				adapter.notifySettingsChanged();
				invalidateOptionsMenu();
				break;

			case TagAction.Result.FOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_tag_followed, Toast.LENGTH_SHORT).show();
				adapter.notifySettingsChanged();
				invalidateOptionsMenu();
				break;

			case TagAction.Result.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				break;
		}
	}


	@Override
	public void onTabSelected() {
		adapter.scrollToTop();
		// reset menu
		invalidateOptionsMenu();
	}
}