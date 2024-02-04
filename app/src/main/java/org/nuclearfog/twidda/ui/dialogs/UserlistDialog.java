package org.nuclearfog.twidda.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserlistUpdater;
import org.nuclearfog.twidda.backend.helper.update.UserListUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;
import org.nuclearfog.twidda.ui.views.InputView;
import org.nuclearfog.twidda.ui.views.InputView.OnTextChangeListener;

/**
 * dialog used to create or update an userlist
 *
 * @author nuclearfog
 */
public class UserlistDialog extends DialogFragment implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener, OnTextChangeListener, AsyncCallback<UserlistUpdater.Result> {

	/**
	 *
	 */
	private static final String TAG = "UserListDialog";

	/**
	 * Bundle key used to set/restore userlsit configuration
	 *
	 * @value type is {@link UserList} or {@link UserListUpdate}
	 */
	private static final String KEY_USERLIST = "userlist";

	private InputView title_input;

	private UserlistUpdater listUpdater;

	private UserListUpdate userlist = new UserListUpdate();

	/**
	 *
	 */
	public UserlistDialog() {
		setStyle(STYLE_NO_TITLE, R.style.DefaultDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_userlist, container, false);
		Button apply = view.findViewById(R.id.dialog_userlist_apply);
		View button_cancel = view.findViewById(R.id.dialog_userlist_cancel);
		TextView title_dialog = view.findViewById(R.id.dialog_userlist_title_dialog);
		title_input = view.findViewById(R.id.dialog_userlist_title_input);
		CompoundButton exclusive = view.findViewById(R.id.dialog_userlist_exclusive);
		Spinner policy = view.findViewById(R.id.dialog_userlist_replies_selector);

		listUpdater = new UserlistUpdater(requireContext());
		GlobalSettings settings = GlobalSettings.get(requireContext());
		DropdownAdapter adapter = new DropdownAdapter(requireContext());

		adapter.setItems(R.array.userlist_policy);
		policy.setAdapter(adapter);
		AppStyles.setTheme((ViewGroup) view, settings.getPopupColor());

		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_USERLIST);
			if (data instanceof UserListUpdate) {
				userlist = (UserListUpdate) data;
			} else if (data instanceof UserList) {
				userlist = new UserListUpdate((UserList) data);
			}
		}
		if (userlist.getId() != 0L) {
			title_dialog.setText(R.string.userlist_update_list);
			apply.setText(R.string.userlist_update);
		} else {
			title_dialog.setText(R.string.userlist_create_new_list);
			apply.setText(R.string.userlist_create);
		}
		title_input.setText(userlist.getTitle());
		exclusive.setChecked(userlist.isExclusive());
		if (userlist.getPolicy() == UserList.NONE) {
			policy.setSelection(0);
		} else if (userlist.getPolicy() == UserList.FOLLOWED) {
			policy.setSelection(1);
		} else if (userlist.getPolicy() == UserList.LIST) {
			policy.setSelection(2);
		}
		apply.setOnClickListener(this);
		button_cancel.setOnClickListener(this);
		policy.setOnItemSelectedListener(this);
		exclusive.setOnCheckedChangeListener(this);
		title_input.setOnTextChangeListener(this);
		return view;
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_USERLIST, userlist);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroyView() {
		listUpdater.cancel();
		super.onDestroyView();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_userlist_apply) {
			if (userlist.getTitle().trim().isEmpty()) {
				title_input.setError(title_input.getContext().getString(R.string.error_list_title_empty));
			} else if (listUpdater.isIdle()) {
				listUpdater.execute(userlist, this);
				title_input.setError(null);
			}
		} else if (v.getId() == R.id.dialog_userlist_cancel) {
			dismiss();
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_userlist_exclusive) {
			userlist.setExclusive(isChecked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.dialog_userlist_replies_selector) {
			if (position == 0) {
				userlist.setPolicy(UserList.NONE);
			} else if (position == 1) {
				userlist.setPolicy(UserList.FOLLOWED);
			} else if (position == 2) {
				userlist.setPolicy(UserList.LIST);
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void onTextChanged(InputView inputView, String text) {
		if (inputView.getId() == R.id.dialog_userlist_title_input) {
			userlist.setTitle(text);
		}
	}


	@Override
	public void onResult(@NonNull UserlistUpdater.Result result) {
		Context context = getContext();
		if (result.mode == UserlistUpdater.Result.CREATED) {
			if (context != null)
				Toast.makeText(context, R.string.info_list_created, Toast.LENGTH_SHORT).show();
			if (getActivity() instanceof UserlistUpdatedCallback)
				((UserlistUpdatedCallback) getActivity()).onUserlistUpdate(result.userlist);
			dismiss();
		} else if (result.mode == UserlistUpdater.Result.UPDATED) {
			if (context != null)
				Toast.makeText(context, R.string.info_list_updated, Toast.LENGTH_SHORT).show();
			if (getActivity() instanceof UserlistUpdatedCallback)
				((UserlistUpdatedCallback) getActivity()).onUserlistUpdate(result.userlist);
			dismiss();
		} else if (result.mode == UserlistUpdater.Result.ERROR) {
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}

	/**
	 *
	 */
	public static void show(FragmentActivity activity, UserList userlist) {
		Fragment dialogFragment = activity.getSupportFragmentManager().findFragmentByTag(TAG);
		if (dialogFragment == null) {
			UserlistDialog dialog = new UserlistDialog();
			Bundle param = new Bundle();
			param.putSerializable(KEY_USERLIST, userlist);
			dialog.setArguments(param);
			dialog.show(activity.getSupportFragmentManager(), TAG);
		}
	}

	/**
	 * Callback interface used to update userlist information
	 */
	public interface UserlistUpdatedCallback {

		/**
		 * called if the userlsit is sucessfully updated
		 *
		 * @param userlist new/updated userlist
		 */
		void onUserlistUpdate(UserList userlist);
	}
}