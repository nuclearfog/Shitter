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

import org.nuclearfog.twidda.backend.async.ListLoader;
import org.nuclearfog.twidda.backend.async.ListLoader.UserlistParam;
import org.nuclearfog.twidda.backend.async.ListLoader.UserlistResult;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;
import org.nuclearfog.twidda.ui.adapter.UserlistAdapter;
import org.nuclearfog.twidda.ui.adapter.UserlistAdapter.ListClickListener;

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
	public static final String KEY_FRAG_LIST_OWNER_ID = "list_owner_id";

	/**
	 * key to define the type of the list
	 * {@link #LIST_USER_OWNS,#LIST_USER_SUBSCR_TO}
	 */
	public static final String KEY_FRAG_LIST_LIST_TYPE = "list_type";

	/**
	 * value to show all user lists owned by a specified user
	 *
	 * @see #KEY_FRAG_LIST_LIST_TYPE
	 */
	public static final int LIST_USER_OWNS = 0x5F36F90D;

	/**
	 * value to show all user lists the specified user is added to
	 *
	 * @see #KEY_FRAG_LIST_LIST_TYPE
	 */
	public static final int LIST_USER_SUBSCR_TO = 0xAA7386AA;


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private ListLoader listTask;
	private UserlistAdapter adapter;

	private long id = 0;
	private int type = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle param = getArguments();
		if (param != null) {
			id = param.getLong(KEY_FRAG_LIST_OWNER_ID, -1);
			type = param.getInt(KEY_FRAG_LIST_LIST_TYPE);
		}
		listTask = new ListLoader(requireContext());
		adapter = new UserlistAdapter(requireContext(), this);
		setAdapter(adapter);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmtpy()) {
			setRefresh(true);
			load(-1L);
		}
	}


	@Override
	protected void onReset() {
		load(-1L);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		listTask.cancel();
		super.onDestroy();
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (intent != null) {
			// check if userlist was removed
			if (result.getResultCode() == UserlistActivity.RETURN_LIST_REMOVED) {
				long removedListId = intent.getLongExtra(UserlistActivity.RESULT_REMOVED_LIST_ID, 0);
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
	protected void onReload() {
		load(-1L);
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
	public boolean onPlaceholderClick(long cursor) {
		if (listTask.isIdle()) {
			load(cursor);
			return true;
		}
		return false;
	}


	@Override
	public void onResult(UserlistResult result) {
		switch (result.mode) {
			case UserlistResult.MEMBERSHIP:
			case UserlistResult.OWNERSHIP:
				if (result.userlists != null) {
					adapter.addItems(result.userlists);
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
	private void load(long cursor) {
		UserlistParam param;
		switch (type) {
			case LIST_USER_OWNS:
				param = new UserlistParam(UserlistParam.OWNERSHIP, id, cursor);
				break;

			case LIST_USER_SUBSCR_TO:
				param = new UserlistParam(UserlistParam.MEMBERSHIP, id, cursor);
				break;

			default:
				return;
		}
		listTask.execute(param, this);
	}
}