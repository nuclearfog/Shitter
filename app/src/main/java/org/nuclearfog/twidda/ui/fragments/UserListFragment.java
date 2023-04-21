package org.nuclearfog.twidda.ui.fragments;

import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_USER;
import static org.nuclearfog.twidda.ui.activities.UserlistActivity.KEY_LIST_DATA;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ListLoader;
import org.nuclearfog.twidda.backend.async.ListLoader.UserlistParam;
import org.nuclearfog.twidda.backend.async.ListLoader.UserlistResult;
import org.nuclearfog.twidda.backend.helper.UserLists;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;
import org.nuclearfog.twidda.ui.adapter.UserlistAdapter;
import org.nuclearfog.twidda.ui.adapter.UserlistAdapter.ListClickListener;

import java.io.Serializable;

/**
 * Fragment class to show userlists
 *
 * @author nuclearfog
 */
public class UserListFragment extends ListFragment implements ListClickListener, AsyncCallback<UserlistResult>, ActivityResultCallback<ActivityResult> {

	/**
	 * Key for the owner ID
	 * value type is Long
	 */
	public static final String KEY_FRAGMENT_USERLIST_OWNER_ID = "userlist_owner_id";

	/**
	 * key to define the type of the list
	 * {@link #LIST_USER_OWNS,#LIST_USER_SUBSCR_TO}
	 */
	public static final String KEY_FRAGMENT_USERLIST_TYPE = "userlist_type";

	/**
	 * internal Bundle key used to save adapter items
	 * value type is {@link UserList[]}
	 */
	private static final String KEY_FRAGMENT_USERLIST_SAVE = "userlist_save";

	/**
	 * value to show all user lists owned by a specified user
	 *
	 * @see #KEY_FRAGMENT_USERLIST_TYPE
	 */
	public static final int LIST_USER_OWNS = 0x5F36F90D;

	/**
	 * value to show all user lists the specified user is added to
	 *
	 * @see #KEY_FRAGMENT_USERLIST_TYPE
	 */
	public static final int LIST_USER_SUBSCR_TO = 0xAA7386AA;

	/**
	 * "index" used to replace the whole list with new items
	 */
	private static final int CLEAR_LIST = -1;


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private ListLoader userlistLoader;
	private UserlistAdapter adapter;

	private long id = 0;
	private int type = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		userlistLoader = new ListLoader(requireContext());
		adapter = new UserlistAdapter(requireContext(), this);
		setAdapter(adapter);

		Bundle param = getArguments();
		if (param != null) {
			id = param.getLong(KEY_FRAGMENT_USERLIST_OWNER_ID, -1L);
			type = param.getInt(KEY_FRAGMENT_USERLIST_TYPE);
		}
		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_FRAGMENT_USERLIST_SAVE);
			if (data instanceof UserLists) {
				adapter.replaceItems((UserLists) data);
			}
		}
		setRefresh(true);
		load(-1L, CLEAR_LIST);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_FRAGMENT_USERLIST_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		userlistLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(0L, CLEAR_LIST);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		setRefresh(true);
		load(0L, CLEAR_LIST);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (intent != null) {
			// check if userlist was removed
			if (result.getResultCode() == UserlistActivity.RETURN_LIST_REMOVED) {
				long removedListId = intent.getLongExtra(UserlistActivity.RESULT_REMOVED_LIST_ID, 0L);
				adapter.removeItem(removedListId);
			}
			// check if userlist was updated
			else if (result.getResultCode() == UserlistActivity.RETURN_LIST_UPDATED) {
				Object object = intent.getSerializableExtra(UserlistActivity.RESULT_UPDATE_LIST);
				if (object instanceof UserList) {
					UserList update = (UserList) object;
					adapter.updateItem(update);
				}
			}
		}
	}


	@Override
	public void onListClick(UserList listItem) {
		Intent listIntent = new Intent(requireContext(), UserlistActivity.class);
		listIntent.putExtra(KEY_LIST_DATA, listItem);
		activityResultLauncher.launch(listIntent);
	}


	@Override
	public void onProfileClick(User user) {
		Intent profile = new Intent(requireContext(), ProfileActivity.class);
		profile.putExtra(KEY_PROFILE_USER, user);
		startActivity(profile);
	}


	@Override
	public boolean onPlaceholderClick(long cursor, int index) {
		if (userlistLoader.isIdle()) {
			load(cursor, index);
			return true;
		}
		return false;
	}


	@Override
	public void onResult(@NonNull UserlistResult result) {
		switch (result.mode) {
			case UserlistResult.MEMBERSHIP:
			case UserlistResult.OWNERSHIP:
				if (result.userlists != null) {
					adapter.addItems(result.userlists, result.index);
				}
				break;

			case UserlistResult.ERROR:
				String message = ErrorHandler.getErrorMessage(getContext(), result.exception);
				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
				adapter.disableLoading();
				break;
		}
		setRefresh(false);
	}

	/**
	 * load content into the list
	 */
	private void load(long cursor, int index) {
		UserlistParam param;
		switch (type) {
			case LIST_USER_OWNS:
				param = new UserlistParam(UserlistParam.OWNERSHIP, index, id, cursor);
				break;

			case LIST_USER_SUBSCR_TO:
				param = new UserlistParam(UserlistParam.MEMBERSHIP, index, id, cursor);
				break;

			default:
				return;
		}
		userlistLoader.execute(param, this);
	}
}