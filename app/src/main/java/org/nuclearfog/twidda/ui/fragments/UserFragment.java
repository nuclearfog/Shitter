package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
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

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserlistManager;
import org.nuclearfog.twidda.backend.async.UsersLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.lists.Users;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.adapter.recyclerview.UserAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.UserAdapter.UserClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

import java.io.Serializable;

/**
 * fragment class to show a list of users
 *
 * @author nuclearfog
 */
public class UserFragment extends ListFragment implements UserClickListener, OnConfirmListener, AsyncCallback<UsersLoader.Result>, ActivityResultCallback<ActivityResult> {

	/**
	 * key to set the type of user list to show
	 * possible value types are
	 * {@link #MODE_FOLLOWER ,#USER_FRAG_FOLLOWING,#USER_FRAG_REPOST ,#USER_FRAG_FAVORIT},
	 * {@link #MODE_SEARCH ,#USER_FRAG_LIST_SUBSCRIBER,#USER_FRAG_LIST_MEMBERS,#USER_FRAG_BLOCKED_USERS},
	 * {@link #MODE_MUTES ,#USER_FRAG_FOLLOW_INCOMING,#USER_FRAG_FOLLOW_OUTGOING}
	 */
	public static final String KEY_MODE = "user_mode";

	/**
	 * key to define search string like username
	 * value type is string
	 */
	public static final String KEY_SEARCH = "user_search";

	/**
	 * key to define user, status or list ID
	 * value type is long
	 */
	public static final String KEY_ID = "user_id";

	/**
	 * key enable function to remove users from list
	 * value type is boolean
	 */
	public static final String KEY_DELETE = "user_en_del";

	/**
	 * Bundle key to save list items
	 * value type is {@link Users}
	 */
	private static final String KEY_SAVE = "user_data";

	/**
	 * value to configure to show users following the authenticating user
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_FOLLOWER = 0xE45DD2;

	/**
	 * value to configure to show users followed by the authenticating user
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_FOLLOWING = 0x64D432EB;

	/**
	 * value to configure to show users reposting a status
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_REPOSTER = 0x2AC31E6B;

	/**
	 * value to configure to show users favoring a status
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_FAVORITER = 0xA7FB2BB4;

	/**
	 * value to configure to search users matching a search string
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_SEARCH = 0x162C3599;

	/**
	 * value to configure to show subscribers of an userlist
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_LIST_SUBSCRIBER = 0x21DCF91C;

	/**
	 * value to configure to show members of an userlist
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_LIST_MEMBER = 0x9A00B3A5;

	/**
	 * value to configure a list of blocked users
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_BLOCKS = 0x83D186AD;

	/**
	 * value to configure a list of muted users
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_MUTES = 0x5246DC35;

	/**
	 * value to configure a list of users with incoming following request
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_FOLLOW_INCOMING = 0x89e5255a;

	/**
	 * value to configure a list of users with outgoing following request
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_FOLLOW_OUTGOING = 0x72544f17;


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
	private AsyncCallback<UserlistManager.Result> userlistUpdate = this::updateUsers;

	private ConfirmDialog confirmDialog;
	private UsersLoader userLoader;
	private UserlistManager userlistManager;
	private UserAdapter adapter;

	private User selectedUser;
	private String search = "";
	private long id = 0;
	private int mode = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		userLoader = new UsersLoader(requireContext());
		userlistManager = new UserlistManager(requireContext());
		confirmDialog = new ConfirmDialog(requireActivity(), this);
		adapter = new UserAdapter(this);
		setAdapter(adapter);

		Bundle param = getArguments();
		if (param != null) {
			mode = param.getInt(KEY_MODE, 0);
			id = param.getLong(KEY_ID, 0L);
			search = param.getString(KEY_SEARCH, "");
			boolean delUser = param.getBoolean(KEY_DELETE, false);
			adapter.enableDeleteButton(delUser);
		}
		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Users) {
				adapter.setItems((Users) data);
			}
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			setRefresh(true);
			load(UsersLoader.Param.NO_CURSOR, UserAdapter.CLEAR_LIST);
		}
	}


	@Override
	public void onDestroy() {
		userLoader.cancel();
		userlistManager.cancel();
		super.onDestroy();
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (result.getResultCode() == ProfileActivity.RETURN_USER_UPDATED && intent != null) {
			Object object = intent.getSerializableExtra(ProfileActivity.KEY_USER);
			if (object instanceof User) {
				User update = (User) object;
				adapter.updateItem(update);
			}
		}
	}


	@Override
	protected void onReset() {
		adapter.clear();
		userLoader = new UsersLoader(requireContext());
		userlistManager = new UserlistManager(requireContext());
		load(UsersLoader.Param.NO_CURSOR, UserAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	protected void onReload() {
		load(UsersLoader.Param.NO_CURSOR, UserAdapter.CLEAR_LIST);
	}


	@Override
	public void onUserClick(User user) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), ProfileActivity.class);
			intent.putExtra(ProfileActivity.KEY_USER, user);
			activityResultLauncher.launch(intent);
		}
	}


	@Override
	public boolean onPlaceholderClick(long cursor, int index) {
		if (!isRefreshing() && userLoader.isIdle()) {
			load(cursor, index);
			return true;
		}
		return false;
	}


	@Override
	public void onDelete(User user) {
		if (userlistManager.isIdle() && !confirmDialog.isShowing()) {
			confirmDialog.show(ConfirmDialog.LIST_REMOVE_USER);
			this.selectedUser = user;
		}
	}


	@Override
	public void onResult(@NonNull UsersLoader.Result result) {
		if (result.users != null) {
			adapter.addItems(result.users, result.index);
		} else {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
			adapter.disableLoading();
		}
		setRefresh(false);
	}


	@Override
	public void onConfirm(int type, boolean remember) {
		// remove user from list
		if (type == ConfirmDialog.LIST_REMOVE_USER) {
			if (selectedUser != null) {
				UserlistManager.Param param = new UserlistManager.Param(UserlistManager.Param.REMOVE, id, selectedUser.getScreenname());
				userlistManager.execute(param, userlistUpdate);
			}
		}
	}

	/**
	 * callback for userlist changes
	 */
	private void updateUsers(UserlistManager.Result result) {
		if (result.mode == UserlistManager.Result.DEL_USER) {
			Context context = getContext();
			if (selectedUser != null && context != null) {
				String info = getString(R.string.info_user_removed, selectedUser.getScreenname());
				Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
				adapter.removeItem(selectedUser);
			}
		}
	}

	/**
	 * load content into the list
	 *
	 * @param cursor cursor of the list
	 */
	private void load(long cursor, int index) {
		UsersLoader.Param param;
		switch (mode) {
			case MODE_FOLLOWER:
				param = new UsersLoader.Param(UsersLoader.Param.FOLLOWS, index, id, cursor, search);
				break;

			case MODE_FOLLOWING:
				param = new UsersLoader.Param(UsersLoader.Param.FRIENDS, index, id, cursor, search);
				break;

			case MODE_REPOSTER:
				param = new UsersLoader.Param(UsersLoader.Param.REPOST, index, id, cursor, search);
				break;

			case MODE_FAVORITER:
				param = new UsersLoader.Param(UsersLoader.Param.FAVORIT, index, id, cursor, search);
				break;

			case MODE_SEARCH:
				param = new UsersLoader.Param(UsersLoader.Param.SEARCH, index, id, cursor, search);
				break;

			case MODE_LIST_SUBSCRIBER:
				param = new UsersLoader.Param(UsersLoader.Param.SUBSCRIBER, index, id, cursor, search);
				break;

			case MODE_LIST_MEMBER:
				param = new UsersLoader.Param(UsersLoader.Param.LISTMEMBER, index, id, cursor, search);
				break;

			case MODE_BLOCKS:
				param = new UsersLoader.Param(UsersLoader.Param.BLOCK, index, id, cursor, search);
				break;

			case MODE_MUTES:
				param = new UsersLoader.Param(UsersLoader.Param.MUTE, index, id, cursor, search);
				break;

			case MODE_FOLLOW_OUTGOING:
				param = new UsersLoader.Param(UsersLoader.Param.REQUEST_OUT, index, id, cursor, search);
				break;

			case MODE_FOLLOW_INCOMING:
				param = new UsersLoader.Param(UsersLoader.Param.REQUEST_IN, index, id, cursor, search);
				break;

			default:
				return;
		}
		userLoader.execute(param, this);
	}
}