package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Activity to show userlists of an user
 *
 * @author nuclearfog
 */
public class UserlistsActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

	/**
	 * Key for the ID the list owner
	 * value type is Long
	 */
	public static final String KEY_USERLIST_OWNER_ID = "userlist-owner-id";

	/**
	 * alternative key for the screen name of the owner
	 * value type is String
	 */
	public static final String KEY_USERLIST_OWNER_NAME = "userlist-owner-name";

	/**
	 * request code for {@link UserlistEditor} OnTabSelectedListener
	 */
	private static final int REQUEST_CREATE_LIST = 0x9D8E;


	private FragmentAdapter adapter;
	private GlobalSettings settings;
	private ViewPager pager;
	private TabLayout tabLayout;

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
		pager = findViewById(R.id.list_pager);
		tabLayout = findViewById(R.id.list_tab);

		toolbar.setTitle(R.string.list_appbar);
		setSupportActionBar(toolbar);
		adapter = new FragmentAdapter(getSupportFragmentManager());
		tabLayout.setupWithViewPager(pager);

		settings = GlobalSettings.getInstance(this);
		AppStyles.setTheme(root, settings.getBackgroundColor());
		pager.setAdapter(adapter);
		pager.setOffscreenPageLimit(2);
		tabLayout.setupWithViewPager(pager);
		tabLayout.addOnTabSelectedListener(this);

		long ownerId = getIntent().getLongExtra(KEY_USERLIST_OWNER_ID, -1);
		String ownerName = getIntent().getStringExtra(KEY_USERLIST_OWNER_NAME);

		isHome = ownerId == settings.getLogin().getId();
		adapter.setupListPage(ownerId, ownerName);
		AppStyles.setTabIcons(tabLayout, settings, R.array.userlist_tab_icons);
	}


	@Override
	protected void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
		super.onActivityResult(reqCode, returnCode, intent);
		if (reqCode == REQUEST_CREATE_LIST && returnCode == UserlistEditor.RETURN_LIST_CREATED) {
			adapter.notifySettingsChanged();
		}
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
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.lists, m);
		m.findItem(R.id.list_create).setVisible(isHome);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.list_create) {
			Intent createList = new Intent(this, UserlistEditor.class);
			startActivityForResult(createList, REQUEST_CREATE_LIST);
		}
		return super.onOptionsItemSelected(item);
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
}