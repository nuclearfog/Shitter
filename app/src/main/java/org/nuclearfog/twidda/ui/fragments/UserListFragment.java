package org.nuclearfog.twidda.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserlistLoader;
import org.nuclearfog.twidda.backend.async.UserlistLoader.UserlistParam;
import org.nuclearfog.twidda.backend.async.UserlistLoader.UserlistResult;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.model.lists.UserLists;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;
import org.nuclearfog.twidda.ui.adapter.recyclerview.UserlistAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.UserlistAdapter.ListClickListener;

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
	public static final String KEY_ID = "userlist_owner_id";

	/**
	 * key to define the type of the list
	 * {@link #MODE_OWNERSHIP ,#LIST_USER_SUBSCR_TO}
	 */
	public static final String KEY_MODE = "userlist_type";

	/**
	 * internal Bundle key used to save adapter items
	 * value type is {@link UserLists}
	 */
	private static final String KEY_SAVE = "userlist_save";

	/**
	 * value to show all user lists owned by a specified user
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_OWNERSHIP = 0x5F36F90D;

	/**
	 * value to show all user lists the specified user is added to
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_MEMBERSHIP = 0xAA7386AA;


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private UserlistLoader userlistLoader;
	private UserlistAdapter adapter;

	private long id = 0;
	private int type = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		userlistLoader = new UserlistLoader(requireContext());
		adapter = new UserlistAdapter(this);
		setAdapter(adapter);

		Bundle param = getArguments();
		if (param != null) {
			id = param.getLong(KEY_ID, -1L);
			type = param.getInt(KEY_MODE);
		}
		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof UserLists) {
				adapter.replaceItems((UserLists) data);
				return;
			}
		}
		setRefresh(true);
		load(UserlistParam.NO_CURSOR, UserlistAdapter.CLEAR_LIST);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		userlistLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(UserlistParam.NO_CURSOR, UserlistAdapter.CLEAR_LIST);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		userlistLoader = new UserlistLoader(requireContext());
		load(UserlistParam.NO_CURSOR, UserlistAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (intent != null) {
			// check if userlist was removed
			if (result.getResultCode() == UserlistActivity.RETURN_LIST_REMOVED) {
				long removedListId = intent.getLongExtra(UserlistActivity.KEY_ID, 0L);
				adapter.removeItem(removedListId);
			}
			// check if userlist was updated
			else if (result.getResultCode() == UserlistActivity.RETURN_LIST_UPDATED) {
				Object object = intent.getSerializableExtra(UserlistActivity.KEY_DATA);
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
		listIntent.putExtra(UserlistActivity.KEY_DATA, listItem);
		activityResultLauncher.launch(listIntent);
	}


	@Override
	public void onProfileClick(User user) {
		Intent profile = new Intent(requireContext(), ProfileActivity.class);
		profile.putExtra(ProfileActivity.KEY_USER, user);
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
				if (getContext() != null) {
					ErrorUtils.showErrorMessage(getContext(), result.exception);
				}
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
			case MODE_OWNERSHIP:
				param = new UserlistParam(UserlistParam.OWNERSHIP, index, id, cursor);
				break;

			case MODE_MEMBERSHIP:
				param = new UserlistParam(UserlistParam.MEMBERSHIP, index, id, cursor);
				break;

			default:
				return;
		}
		userlistLoader.execute(param, this);
	}
}