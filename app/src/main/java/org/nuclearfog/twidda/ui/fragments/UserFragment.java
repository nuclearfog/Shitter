package org.nuclearfog.twidda.ui.fragments;

import static org.nuclearfog.twidda.backend.async.UsersLoader.NO_CURSOR;
import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_USER;

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

import org.nuclearfog.twidda.backend.utils.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.ui.adapter.UserAdapter;
import org.nuclearfog.twidda.ui.adapter.UserAdapter.UserClickListener;
import org.nuclearfog.twidda.backend.async.UsersLoader;
import org.nuclearfog.twidda.backend.async.UsersLoader.UserResult;
import org.nuclearfog.twidda.backend.async.UsersLoader.UserParam;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;

/**
 * fragment class to show a list of users
 *
 * @author nuclearfog
 */
public class UserFragment extends ListFragment implements UserClickListener, AsyncCallback<UserResult>, ActivityResultCallback<ActivityResult> {

	/**
	 * key to set the type of user list to show
	 * possible value types are
	 * {@link #USER_FRAG_FOLLOWER,#USER_FRAG_FOLLOWING,#USER_FRAG_REPOST ,#USER_FRAG_FAVORIT},
	 * {@link #USER_FRAG_SEARCH,#USER_FRAG_LIST_SUBSCRIBER,#USER_FRAG_LIST_MEMBERS,#USER_FRAG_BLOCKED_USERS},
	 * {@link #USER_FRAG_MUTED_USERS,#USER_FRAG_FOLLOW_INCOMING,#USER_FRAG_FOLLOW_OUTGOING}
	 */
	public static final String KEY_FRAG_USER_MODE = "user_mode";

	/**
	 * key to define search string like username
	 * value type is string
	 */
	public static final String KEY_FRAG_USER_SEARCH = "user_search";

	/**
	 * key to define user, status or list ID
	 * value type is long
	 */
	public static final String KEY_FRAG_USER_ID = "user_id";

	/**
	 * key enable function to remove users from list
	 * value type is boolean
	 */
	public static final String KEY_FRAG_DEL_USER = "user_en_del";

	/**
	 * value to configure to show users following the authenticating user
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_FOLLOWER = 0xE45DD2;

	/**
	 * value to configure to show users followed by the authenticating user
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_FOLLOWING = 0x64D432EB;

	/**
	 * value to configure to show users reposting a status
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_REPOST = 0x2AC31E6B;

	/**
	 * value to configure to show users favoring a status
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_FAVORIT = 0xA7FB2BB4;

	/**
	 * value to configure to search users matching a search string
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_SEARCH = 0x162C3599;

	/**
	 * value to configure to show subscribers of an userlist
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_LIST_SUBSCRIBER = 0x21DCF91C;

	/**
	 * value to configure to show members of an userlist
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_LIST_MEMBERS = 0x9A00B3A5;

	/**
	 * value to configure a list of blocked users
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_BLOCKED_USERS = 0x83D186AD;

	/**
	 * value to configure a list of muted users
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_MUTED_USERS = 0x5246DC35;

	/**
	 * value to configure a list of users with incoming following request
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_FOLLOW_INCOMING = 0x89e5255a;

	/**
	 * value to configure a list of users with outgoing following request
	 *
	 * @see #KEY_FRAG_USER_MODE
	 */
	public static final int USER_FRAG_FOLLOW_OUTGOING = 0x72544f17;


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private UsersLoader userAsync;
	private UserAdapter adapter;

	private String search = "";
	private long id = 0;
	private int mode = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle param = getArguments();
		boolean delUser = false;
		if (param != null) {
			mode = param.getInt(KEY_FRAG_USER_MODE, 0);
			id = param.getLong(KEY_FRAG_USER_ID, 0);
			search = param.getString(KEY_FRAG_USER_SEARCH, "");
			delUser = param.getBoolean(KEY_FRAG_DEL_USER, false);
		}
		adapter = new UserAdapter(requireContext(), this, delUser);
		setAdapter(adapter);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (userAsync == null) {
			load(NO_CURSOR);
		}
	}


	@Override
	protected void onReset() {
		load(NO_CURSOR);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		if (userAsync != null && !userAsync.idle())
			userAsync.kill();
		super.onDestroy();
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (result.getResultCode() == ProfileActivity.RETURN_USER_UPDATED && intent != null) {
			Object object = intent.getSerializableExtra(ProfileActivity.KEY_USER_UPDATE);
			if (object instanceof User) {
				User update = (User) object;
				adapter.updateItem(update);
			}
		}
	}


	@Override
	protected void onReload() {
		load(NO_CURSOR);
	}


	@Override
	public void onUserClick(User user) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), ProfileActivity.class);
			intent.putExtra(KEY_PROFILE_USER, user);
			activityResultLauncher.launch(intent);
		}
	}


	@Override
	public boolean onPlaceholderClick(long cursor) {
		if (userAsync != null && userAsync.idle()) {
			load(cursor);
			return true;
		}
		return false;
	}


	@Override
	public void onDelete(User user) {
		if (getActivity() instanceof UserlistActivity) {
			// call parent activity to handle user delete
			UserlistActivity callback = (UserlistActivity) getActivity();
			callback.onDelete(user);
		}
	}


	@Override
	public void onResult(UserResult res) {
		setRefresh(false);
		if (res.users != null) {
			adapter.addItems(res.users);
		} else {
			String message = ErrorHandler.getErrorMessage(requireContext(), res.exception);
			Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
			adapter.disableLoading();
		}
	}

	/**
	 * remove specific user from fragment list
	 *
	 * @param user user to remove
	 */
	public void removeUser(User user) {
		adapter.removeItem(user);
	}


	/**
	 * load content into the list
	 *
	 * @param cursor cursor of the list or {@link UsersLoader#NO_CURSOR} if there is none
	 */
	private void load(long cursor) {
		UserParam param;
		userAsync = new UsersLoader(requireContext());
		switch (mode) {
			case USER_FRAG_FOLLOWER:
				param = new UserParam(UsersLoader.FOLLOWS, id, cursor, search);
				break;

			case USER_FRAG_FOLLOWING:
				param = new UserParam(UsersLoader.FRIENDS, id, cursor, search);
				break;

			case USER_FRAG_REPOST:
				param = new UserParam(UsersLoader.REPOST, id, cursor, search);
				break;

			case USER_FRAG_FAVORIT:
				param = new UserParam(UsersLoader.FAVORIT, id, cursor, search);
				break;

			case USER_FRAG_SEARCH:
				param = new UserParam(UsersLoader.SEARCH, id, cursor, search);
				break;

			case USER_FRAG_LIST_SUBSCRIBER:
				param = new UserParam(UsersLoader.SUBSCRIBER, id, cursor, search);
				break;

			case USER_FRAG_LIST_MEMBERS:
				param = new UserParam(UsersLoader.LISTMEMBER, id, cursor, search);
				break;

			case USER_FRAG_BLOCKED_USERS:
				param = new UserParam(UsersLoader.BLOCK, id, cursor, search);
				break;

			case USER_FRAG_MUTED_USERS:
				param = new UserParam(UsersLoader.MUTE, id, cursor, search);
				break;

			case USER_FRAG_FOLLOW_OUTGOING:
				param = new UserParam(UsersLoader.INCOMING_REQ, id, cursor, search);
				break;

			case USER_FRAG_FOLLOW_INCOMING:
				param = new UserParam(UsersLoader.OUTGOING_REQ, id, cursor, search);
				break;

			default:
				return;
		}
		userAsync.execute(param, this);
		if (cursor == NO_CURSOR) {
			setRefresh(true);
		}
	}
}