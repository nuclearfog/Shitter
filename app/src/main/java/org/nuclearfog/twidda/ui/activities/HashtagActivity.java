package org.nuclearfog.twidda.ui.activities;

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
import org.nuclearfog.twidda.backend.async.HashtagAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.viewpager.HashtagAdapter;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

/**
 * Activity class used to show hashtag following/featuring
 *
 * @author nuclearfog
 */
public class HashtagActivity extends AppCompatActivity implements OnQueryTextListener, OnTabSelectedListener, AsyncCallback<HashtagAction.Result> {

	private GlobalSettings settings;
	private HashtagAction hashtagAction;
	private HashtagAdapter adapter;

	private ViewPager2 viewPager;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_tab_view);
		ViewGroup root = findViewById(R.id.page_tab_view_root);
		Toolbar toolbar = findViewById(R.id.page_tab_view_toolbar);
		TabSelector tabSelector = findViewById(R.id.page_tab_view_tabs);
		viewPager = findViewById(R.id.page_tab_view_pager);

		hashtagAction = new HashtagAction(this);
		settings = GlobalSettings.get(this);
		adapter = new HashtagAdapter(this);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(3);

		tabSelector.addTabIcons(R.array.userlist_hashtag_icons);
		tabSelector.addTabLabels(R.array.hashtag_labels);

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);

		tabSelector.addOnTabSelectedListener(this);
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.hashtags, menu);
		MenuItem search = menu.findItem(R.id.menu_hashtag_add);
		SearchView searchView = (SearchView) search.getActionView();
		searchView.setQueryHint(getString(R.string.menu_hashtag_add));
		searchView.setOnQueryTextListener(this);
		AppStyles.setTheme(searchView, Color.TRANSPARENT);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem search = menu.findItem(R.id.menu_hashtag_add);
		search.collapseActionView();
		search.setVisible(viewPager.getCurrentItem() != 2);
		return true;
	}


	@Override
	public boolean onQueryTextSubmit(String query) {
		if (hashtagAction.isIdle()) {
			if (viewPager.getCurrentItem() == 0) {
				Toast.makeText(getApplicationContext(), R.string.info_hashtag_following, Toast.LENGTH_SHORT).show();
				HashtagAction.Param param = new HashtagAction.Param(HashtagAction.Param.FOLLOW, query);
				hashtagAction.execute(param, this);
				return true;
			} else if (viewPager.getCurrentItem() == 1) {
				Toast.makeText(getApplicationContext(), R.string.info_hashtag_featuring, Toast.LENGTH_SHORT).show();
				HashtagAction.Param param = new HashtagAction.Param(HashtagAction.Param.FEATURE, query);
				hashtagAction.execute(param, this);
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
	public void onResult(@NonNull HashtagAction.Result result) {
		switch (result.mode) {
			case HashtagAction.Result.FEATURE:
				Toast.makeText(getApplicationContext(), R.string.info_hashtag_featured, Toast.LENGTH_SHORT).show();
				adapter.notifySettingsChanged();
				invalidateOptionsMenu();
				break;

			case HashtagAction.Result.FOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_hashtag_followed, Toast.LENGTH_SHORT).show();
				adapter.notifySettingsChanged();
				invalidateOptionsMenu();
				break;

			case HashtagAction.Result.ERROR:
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