package org.nuclearfog.twidda.ui.activities;

import static org.nuclearfog.twidda.ui.activities.UsersActivity.KEY_USERS_MODE;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.USERS_EXCLUDED;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.adapter.FragmentAdapter;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

/**
 * Activity to show userlists of an user
 *
 * @author nuclearfog
 */
public class UserlistsActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, OnTabSelectedListener {

	/**
	 * Key for the ID the list owner
	 * value type is Long
	 */
	public static final String KEY_USERLIST_OWNER_ID = "userlist-owner-id";


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private FragmentAdapter adapter;
	private GlobalSettings settings;
	private ViewPager2 viewPager;

	private boolean isHome = false;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_list);
		ViewGroup root = findViewById(R.id.list_view);
		Toolbar toolbar = findViewById(R.id.list_toolbar);
		TabSelector tabSelector = findViewById(R.id.list_tab);
		viewPager = findViewById(R.id.list_pager);

		settings = GlobalSettings.getInstance(this);
		adapter = new FragmentAdapter(this);

		toolbar.setTitle(R.string.list_appbar);
		setSupportActionBar(toolbar);

		long ownerId = getIntent().getLongExtra(KEY_USERLIST_OWNER_ID, 0L);
		isHome = ownerId == settings.getLogin().getId();

		adapter.setupListPage(ownerId);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(2);
		tabSelector.addViewPager(viewPager);
		tabSelector.addTabIcons(R.array.userlist_tab_icons);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		AppStyles.setTheme(root);

		tabSelector.addOnTabSelectedListener(this);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getResultCode() == UserlistEditor.RETURN_LIST_CREATED) {
			adapter.notifySettingsChanged();
		}
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
		m.findItem(R.id.list_create).setVisible(isHome);
		m.findItem(R.id.list_blocklists).setVisible(isHome);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// open list editor
		if (item.getItemId() == R.id.list_create) {
			Intent createList = new Intent(this, UserlistEditor.class);
			activityResultLauncher.launch(createList);
			return true;
		}
		// open mute/block list
		else if (item.getItemId() == R.id.list_blocklists) {
			Intent usersIntent = new Intent(this, UsersActivity.class);
			usersIntent.putExtra(KEY_USERS_MODE, USERS_EXCLUDED);
			startActivity(usersIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onTabSelected(int oldPosition) {
		adapter.scrollToTop(oldPosition);
	}
}