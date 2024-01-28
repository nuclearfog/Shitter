package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.UserlistUpdater;
import org.nuclearfog.twidda.backend.helper.update.UserListUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.adapter.listview.DropdownAdapter;

/**
 * dialog used to create or update an userlist
 *
 * @author nuclearfog
 */
public class UserlistDialog extends Dialog implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener, TextWatcher, AsyncCallback<UserlistUpdater.Result> {

	private TextView title_dialog;
	private EditText title_input;
	private CompoundButton exclusive;
	private Spinner policy;
	private Button apply;

	private DropdownAdapter adapter;
	private GlobalSettings settings;
	private UserlistUpdater listUpdater;
	private UserlistUpdatedCallback callback;

	private UserListUpdate update = new UserListUpdate();

	/**
	 *
	 */
	public UserlistDialog(Activity activity, UserlistUpdatedCallback callback) {
		super(activity, R.style.DefaultDialog);
		listUpdater = new UserlistUpdater(activity.getApplicationContext());
		settings = GlobalSettings.get(activity.getApplicationContext());
		adapter = new DropdownAdapter(activity.getApplicationContext());
		this.callback = callback;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.dialog_userlist);
		ViewGroup rootView = findViewById(R.id.dialog_userlist_root);
		apply = findViewById(R.id.dialog_userlist_apply);
		View button_cancel = findViewById(R.id.dialog_userlist_cancel);
		title_dialog = findViewById(R.id.dialog_userlist_title_dialog);
		title_input = findViewById(R.id.dialog_userlist_title_input);
		exclusive = findViewById(R.id.dialog_userlist_exclusive);
		policy = findViewById(R.id.dialog_userlist_replies_selector);

		adapter.setItems(R.array.userlist_policy);
		policy.setAdapter(adapter);
		AppStyles.setTheme(rootView, settings.getPopupColor());

		apply.setOnClickListener(this);
		button_cancel.setOnClickListener(this);
		policy.setOnItemSelectedListener(this);
		exclusive.setOnCheckedChangeListener(this);
		title_input.addTextChangedListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_userlist_apply) {
			if (title_input.length() == 0) {
				Toast.makeText(getContext(), R.string.error_list_title_empty, Toast.LENGTH_SHORT).show();
			} else if (listUpdater.isIdle()) {
				listUpdater.execute(update, this);
			}
		} else if (v.getId() == R.id.dialog_userlist_cancel) {
			dismiss();
		}
	}


	@Override
	public void show() {
		super.show();
		title_dialog.setText(R.string.userlist_create_new_list);
		apply.setText(R.string.userlist_create);
		title_input.setText("");
		policy.setSelection(0);
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.dialog_userlist_exclusive) {
			update.setExclusive(isChecked);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.dialog_userlist_replies_selector) {
			if (position == 0) {
				update.setPolicy(UserList.NONE);
			} else if (position == 1) {
				update.setPolicy(UserList.FOLLOWED);
			} else if (position == 2) {
				update.setPolicy(UserList.LIST);
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	@Override
	public void afterTextChanged(Editable s) {
		update.setTitle(s.toString());
	}


	@Override
	public void onResult(@NonNull UserlistUpdater.Result result) {
		if (result.mode == UserlistUpdater.Result.CREATED) {
			Toast.makeText(getContext(), R.string.info_list_created, Toast.LENGTH_SHORT).show();
			callback.onUserlistUpdate(result.userlist);
			dismiss();
		} else if (result.mode == UserlistUpdater.Result.UPDATED) {
			Toast.makeText(getContext(), R.string.info_list_updated, Toast.LENGTH_SHORT).show();
			callback.onUserlistUpdate(result.userlist);
			dismiss();
		} else if (result.mode == UserlistUpdater.Result.ERROR) {
			ErrorUtils.showErrorMessage(getContext(), result.exception);
		}
	}

	/**
	 * show dialog
	 *
	 * @param userlist existing userlist information or null to create a new list
	 */
	public void show(@NonNull UserList userlist) {
		super.show();
		title_dialog.setText(R.string.userlist_update_list);
		apply.setText(R.string.userlist_update);
		title_input.setText(userlist.getTitle());
		update.setId(userlist.getId());
		if (userlist.getReplyPolicy() == UserList.NONE) {
			policy.setSelection(0);
		} else if (userlist.getReplyPolicy() == UserList.FOLLOWED) {
			policy.setSelection(1);
		} else if (userlist.getReplyPolicy() == UserList.LIST) {
			policy.setSelection(2);
		}
	}

	/**
	 * set userlist information
	 */
	public void show(@NonNull UserListUpdate update) {
		super.show();
		this.update = update;
		if (update.getPolicy() == UserList.NONE) {
			policy.setSelection(0);
		} else if (update.getPolicy() == UserList.FOLLOWED) {
			policy.setSelection(1);
		} else if (update.getPolicy() == UserList.LIST) {
			policy.setSelection(2);
		}
		title_input.setText(update.getTitle());
		exclusive.setChecked(update.isExclusive());
	}

	/**
	 *
	 */
	public UserListUpdate getContent() {
		return update;
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