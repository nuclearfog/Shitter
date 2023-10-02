package org.nuclearfog.twidda.ui.activities;

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
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserlistAction;
import org.nuclearfog.twidda.backend.async.UserlistManager;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.adapter.viewpager.UserlistAdapter;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.UserlistDialog;
import org.nuclearfog.twidda.ui.dialogs.UserlistDialog.UserlistUpdatedCallback;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.util.regex.Pattern;

/**
 * This activity shows content of an user list
 * like timeline, list member and subscriber
 *
 * @author nuclearfog
 */
public class UserlistActivity extends AppCompatActivity implements OnTabSelectedListener, OnQueryTextListener, OnConfirmListener, UserlistUpdatedCallback {

	/**
	 * key to add list information
	 * value type is {@link UserList}
	 */
	public static final String KEY_DATA = "list_data";

	/**
	 * result key to return the ID of a removed list
	 * value type is {@link UserList}
	 */
	public static final String KEY_ID = "removed-list-id";

	/**
	 * return code when an user list was deleted
	 */
	public static final int RETURN_LIST_REMOVED = 0xDAD518B4;

	/**
	 * return code when an user list was deleted
	 */
	public static final int RETURN_LIST_UPDATED = 0x5D0F5E8D;

	/**
	 * regex pattern to validate username
	 * e.g. username, @username or @username@instance.social
	 */
	private static final Pattern USERNAME_PATTERN = Pattern.compile("@?\\w{1,20}(@[\\w.]{1,50})?");

	private AsyncCallback<UserlistAction.Result> userlistSet = this::setList;
	private AsyncCallback<UserlistManager.Result> userlistUpdate = this::updateList;


	private UserlistAdapter adapter;
	private UserlistAction listLoaderAsync;
	private UserlistManager listManagerAsync;

	private GlobalSettings settings;

	private ConfirmDialog confirmDialog;
	private UserlistDialog userlistDialog;

	private ViewPager2 viewPager;
	private Toolbar toolbar;

	@Nullable
	private UserList userList;


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
		toolbar = findViewById(R.id.page_tab_view_toolbar);
		viewPager = findViewById(R.id.page_tab_view_pager);

		settings = GlobalSettings.get(this);
		confirmDialog = new ConfirmDialog(this, this);
		userlistDialog = new UserlistDialog(this, this);
		listLoaderAsync = new UserlistAction(this);
		listManagerAsync = new UserlistManager(this);

		Object data = getIntent().getSerializableExtra(KEY_DATA);
		if (data instanceof UserList) {
			userList = (UserList) data;
			toolbar.setTitle(userList.getTitle());
			adapter = new UserlistAdapter(this, userList.getId(), settings.getLogin().getConfiguration().isUserlistSubscriberSupported());
			viewPager.setAdapter(adapter);
			if (adapter.getItemCount() == 2) {
				tabSelector.addTabIcons(R.array.list_tab_icons);
			} else if (adapter.getItemCount() == 3) {
				tabSelector.addTabIcons(R.array.list_subscriber_tab_icons);
			}
			viewPager.setOffscreenPageLimit(3);
			setSupportActionBar(toolbar);
			AppStyles.setTheme(root);

			tabSelector.addOnTabSelectedListener(this);
		}
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (userList != null) {
			// update list information
			UserlistAction.Param param = new UserlistAction.Param(UserlistAction.Param.LOAD, userList.getId());
			listLoaderAsync.execute(param, userlistSet);
		}
	}


	@Override
	protected void onDestroy() {
		listLoaderAsync.cancel();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.userlist, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu m) {
		MenuItem search = m.findItem(R.id.menu_list_add_user);
		SearchView searchUser = (SearchView) search.getActionView();
		AppStyles.setTheme(searchUser, Color.TRANSPARENT);
		searchUser.setQueryHint(getString(R.string.menu_add_user));
		searchUser.setOnQueryTextListener(this);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (userList != null && listLoaderAsync.isIdle()) {
			// open user list editor
			if (item.getItemId() == R.id.menu_list_edit) {
				userlistDialog.show(userList);
				return true;
			}
			// delete user list
			else if (item.getItemId() == R.id.menu_delete_list) {
				confirmDialog.show(ConfirmDialog.LIST_DELETE);
				return true;
			}
			// theme expanded search view
			else if (item.getItemId() == R.id.menu_list_add_user) {
				SearchView searchView = (SearchView) item.getActionView();
				AppStyles.setTheme(searchView, Color.TRANSPARENT);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() > 0) {
			viewPager.setCurrentItem(0);
		} else {
			Intent result = new Intent();
			result.putExtra(KEY_DATA, userList);
			setResult(RETURN_LIST_UPDATED, result);
			super.onBackPressed();
		}
	}


	@Override
	public void onConfirm(int type, boolean remember) {
		// delete user list
		if (type == ConfirmDialog.LIST_DELETE && userList != null) {
			if (listLoaderAsync.isIdle()) {
				UserlistAction.Param param = new UserlistAction.Param(UserlistAction.Param.DELETE, userList.getId());
				listLoaderAsync.execute(param, userlistSet);
			}
		}
	}


	@Override
	public void onTabSelected() {
		adapter.scrollToTop();
	}


	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}


	@Override
	public boolean onQueryTextSubmit(String query) {
		if (userList == null)
			return false;
		if (USERNAME_PATTERN.matcher(query).matches()) {
			if (listManagerAsync.isIdle()) {
				Toast.makeText(getApplicationContext(), R.string.info_adding_user_to_list, Toast.LENGTH_SHORT).show();
				UserlistManager.Param param = new UserlistManager.Param(UserlistManager.Param.ADD, userList.getId(), query);
				listManagerAsync.execute(param, userlistUpdate);
				return true;
			}
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_username_format, Toast.LENGTH_SHORT).show();
		}
		return false;
	}


	@Override
	public void onUserlistUpdate(UserList userlist) {
		this.userList = userlist;
		toolbar.setTitle(userList.getTitle());
		invalidateOptionsMenu();
	}

	/**
	 * update userlist member
	 */
	private void updateList(@NonNull UserlistManager.Result result) {
		switch (result.mode) {
			case UserlistManager.Result.ADD_USER:
				String name;
				if (!result.name.startsWith("@"))
					name = '@' + result.name;
				else
					name = result.name;
				String info = getString(R.string.info_user_added_to_list, name);
				Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case UserlistManager.Result.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				break;
		}
	}

	/**
	 * update userlist content
	 */
	private void setList(@NonNull UserlistAction.Result result) {
		switch (result.mode) {
			case UserlistAction.Result.LOAD:
				if (result.userlist != null) {
					toolbar.setTitle(result.userlist.getTitle());
					invalidateOptionsMenu();
				}
				break;

			case UserlistAction.Result.DELETE:
				Intent intent = new Intent();
				intent.putExtra(KEY_ID, result.id);
				setResult(RETURN_LIST_REMOVED, intent);
				Toast.makeText(getApplicationContext(), R.string.info_list_removed, Toast.LENGTH_SHORT).show();
				finish();
				break;

			case UserlistAction.Result.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				if (result.exception != null && result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
					// List does not exist
					intent = new Intent();
					intent.putExtra(KEY_ID, result.id);
					setResult(RETURN_LIST_REMOVED, intent);
					finish();
				}
				break;
		}
		if (result.userlist != null) {
			this.userList = result.userlist;
		}
	}
}