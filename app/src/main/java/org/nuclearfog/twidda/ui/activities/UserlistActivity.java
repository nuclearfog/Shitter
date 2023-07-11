package org.nuclearfog.twidda.ui.activities;

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
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserlistAction;
import org.nuclearfog.twidda.backend.async.UserlistAction.ListActionParam;
import org.nuclearfog.twidda.backend.async.UserlistAction.ListActionResult;
import org.nuclearfog.twidda.backend.async.UserlistManager;
import org.nuclearfog.twidda.backend.async.UserlistManager.ListManagerParam;
import org.nuclearfog.twidda.backend.async.UserlistManager.ListManagerResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.adapter.fragments.UserlistAdapter;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.views.TabSelector;
import org.nuclearfog.twidda.ui.views.TabSelector.OnTabSelectedListener;

import java.util.regex.Pattern;

/**
 * This activity shows content of an user list
 * like timeline, list member and subscriber
 *
 * @author nuclearfog
 */
public class UserlistActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, OnTabSelectedListener, OnQueryTextListener, OnConfirmListener {

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

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private AsyncCallback<ListActionResult> userlistSet = this::setList;
	private AsyncCallback<ListManagerResult> userlistUpdate = this::updateList;


	private UserlistAdapter adapter;
	private UserlistAction listLoaderAsync;
	private UserlistManager listManagerAsync;

	private GlobalSettings settings;

	private ConfirmDialog confirmDialog;

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
		setContentView(R.layout.page_listdetail);
		ViewGroup root = findViewById(R.id.listdetail_root);
		TabSelector tabSelector = findViewById(R.id.listdetail_tab);
		toolbar = findViewById(R.id.listdetail_toolbar);
		viewPager = findViewById(R.id.listdetail_pager);

		settings = GlobalSettings.get(this);
		confirmDialog = new ConfirmDialog(this, this);
		listLoaderAsync = new UserlistAction(this);
		listManagerAsync = new UserlistManager(this);

		Object data = getIntent().getSerializableExtra(KEY_DATA);
		if (data instanceof UserList) {
			userList = (UserList) data;
			toolbar.setTitle(userList.getTitle());
			toolbar.setSubtitle(userList.getDescription());
			adapter = new UserlistAdapter(this, userList);
		}
		viewPager.setOffscreenPageLimit(3);
		viewPager.setAdapter(adapter);
		tabSelector.addViewPager(viewPager);
		setSupportActionBar(toolbar);

		AppStyles.setTheme(root);
		tabSelector.addTabIcons(R.array.list_tab_icons);

		tabSelector.addOnTabSelectedListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (userList != null) {
			// update list information
			ListActionParam param = new ListActionParam(ListActionParam.LOAD, userList.getId());
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
		MenuItem editList = m.findItem(R.id.menu_list_edit);
		MenuItem deleteList = m.findItem(R.id.menu_delete_list);
		MenuItem followList = m.findItem(R.id.menu_follow_list);
		MenuItem search = m.findItem(R.id.menu_list_add_user);
		SearchView searchUser = (SearchView) search.getActionView();
		AppStyles.setTheme(searchUser, Color.TRANSPARENT);
		if (userList != null) {
			if (userList.isEdiatable()) {
				searchUser.setQueryHint(getString(R.string.menu_add_user));
				searchUser.setOnQueryTextListener(this);
				editList.setVisible(true);
				deleteList.setVisible(true);
				search.setVisible(true);
			} else {
				followList.setVisible(true);
				if (userList.isFollowing()) {
					followList.setTitle(R.string.menu_unfollow_list);
				} else {
					followList.setTitle(R.string.menu_list_follow);
				}
			}
			return true;
		}
		return super.onPrepareOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (userList != null && listLoaderAsync.isIdle()) {
			// open user list editor
			if (item.getItemId() == R.id.menu_list_edit) {
				Intent editList = new Intent(this, UserlistEditor.class);
				editList.putExtra(UserlistEditor.KEY_DATA, userList);
				activityResultLauncher.launch(editList);
				return true;
			}
			// delete user list
			else if (item.getItemId() == R.id.menu_delete_list) {
				confirmDialog.show(ConfirmDialog.LIST_DELETE);
				return true;
			}
			// follow user list
			else if (item.getItemId() == R.id.menu_follow_list) {
				if (userList.isFollowing()) {
					confirmDialog.show(ConfirmDialog.LIST_UNFOLLOW);
				} else {
					ListActionParam param = new ListActionParam(ListActionParam.FOLLOW, userList.getId());
					listLoaderAsync.execute(param, userlistSet);
				}
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
	public void onActivityResult(ActivityResult result) {
		if (result.getData() != null) {
			if (result.getResultCode() == UserlistEditor.RETURN_LIST_CHANGED) {
				Object data = result.getData().getSerializableExtra(UserlistEditor.KEY_DATA);
				if (data instanceof UserList) {
					userList = (UserList) data;
					toolbar.setTitle(userList.getTitle());
					toolbar.setSubtitle(userList.getDescription());
					invalidateOptionsMenu();
				}
			}
		}
	}


	@Override
	public void onConfirm(int type) {
		// delete user list
		if (type == ConfirmDialog.LIST_DELETE && userList != null) {
			if (listLoaderAsync.isIdle()) {
				ListActionParam param = new ListActionParam(ListActionParam.DELETE, userList.getId());
				listLoaderAsync.execute(param, userlistSet);
			}
		}
		// unfollow user list
		else if (type == ConfirmDialog.LIST_UNFOLLOW) {
			if (listLoaderAsync.isIdle() && userList != null) {
				ListActionParam param = new ListActionParam(ListActionParam.UNFOLLOW, userList.getId());
				listLoaderAsync.execute(param, userlistSet);
			}
		}
	}


	@Override
	public void onTabSelected(int oldPosition) {
		adapter.scrollToTop(oldPosition);
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
				ListManagerParam param = new ListManagerParam(ListManagerParam.ADD, userList.getId(), query);
				listManagerAsync.execute(param, userlistUpdate);
				return true;
			}
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_username_format, Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	/**
	 * update userlist member
	 */
	private void updateList(@NonNull ListManagerResult result) {
		switch (result.mode) {
			case ListManagerResult.ADD_USER:
				String name;
				if (!result.name.startsWith("@"))
					name = '@' + result.name;
				else
					name = result.name;
				String info = getString(R.string.info_user_added_to_list, name);
				Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case ListManagerResult.ERROR:
				ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
				break;
		}
	}

	/**
	 * update userlist content
	 */
	private void setList(@NonNull ListActionResult result) {
		switch (result.mode) {
			case ListActionResult.LOAD:
				if (result.userlist != null) {
					toolbar.setTitle(result.userlist.getTitle());
					toolbar.setSubtitle(result.userlist.getDescription());
					invalidateOptionsMenu();
				}
				break;

			case ListActionResult.FOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_list_followed, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case ListActionResult.UNFOLLOW:
				Toast.makeText(getApplicationContext(), R.string.info_list_unfollowed, Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				break;

			case ListActionResult.DELETE:
				Intent intent = new Intent();
				intent.putExtra(KEY_ID, result.id);
				setResult(RETURN_LIST_REMOVED, intent);
				Toast.makeText(getApplicationContext(), R.string.info_list_removed, Toast.LENGTH_SHORT).show();
				finish();
				break;

			case ListActionResult.ERROR:
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