package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.ListUpdater;
import org.nuclearfog.twidda.backend.async.ListUpdater.ListUpdateResult;
import org.nuclearfog.twidda.backend.helper.UserListUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;

/**
 * Activity for the list editor
 *
 * @author nuclearfog
 */
public class UserlistEditor extends AppCompatActivity implements OnClickListener, OnConfirmListener, OnProgressStopListener, AsyncCallback<ListUpdateResult> {

	/**
	 * Key for the list ID if an existing list should be updated
	 * value type is Long
	 */
	public static final String KEY_LIST_EDITOR_DATA = "list_edit_data";

	/**
	 * Key for updated list information
	 * value type is {@link UserList}
	 */
	public static final String KEY_UPDATED_USERLIST = "userlist-update";

	/**
	 * Return code used when an existing userlist was changed
	 */
	public static final int RETURN_LIST_CHANGED = 0x1A5518E1;

	/**
	 * Return code used then a new userlist was created
	 */
	public static final int RETURN_LIST_CREATED = 0xE8715442;

	private ProgressDialog loadingCircle;
	private ConfirmDialog confirmDialog;

	private ListUpdater updaterAsync;
	private EditText titleInput, subTitleInput;
	private CompoundButton visibilitySwitch;
	@Nullable
	private UserList userList;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.popup_userlist);
		ViewGroup root = findViewById(R.id.list_popup_root);
		ImageView background = findViewById(R.id.userlist_popup_background);
		Button updateButton = findViewById(R.id.userlist_create_list);
		TextView popupTitle = findViewById(R.id.popup_list_title);
		TextView visibilityLabel = findViewById(R.id.userlist_switch_text);
		titleInput = findViewById(R.id.list_edit_title);
		subTitleInput = findViewById(R.id.list_edit_descr);
		visibilitySwitch = findViewById(R.id.list_edit_public_sw);

		loadingCircle = new ProgressDialog(this);
		confirmDialog = new ConfirmDialog(this);
		updaterAsync = new ListUpdater(this);

		GlobalSettings settings = GlobalSettings.getInstance(this);
		AppStyles.setEditorTheme(root, background);

		Object data = getIntent().getSerializableExtra(KEY_LIST_EDITOR_DATA);
		if (data instanceof UserList) {
			userList = (UserList) data;
			titleInput.setText(userList.getTitle());
			subTitleInput.setText(userList.getDescription());
			visibilitySwitch.setChecked(!userList.isPrivate());
			popupTitle.setText(R.string.menu_edit_list);
			updateButton.setText(R.string.update_list);
		}
		if (!settings.getLogin().getConfiguration().userlistVisibilitySupported()) {
			visibilitySwitch.setVisibility(View.INVISIBLE);
			visibilityLabel.setVisibility(View.INVISIBLE);
		}
		updateButton.setOnClickListener(this);
		loadingCircle.addOnProgressStopListener(this);
		confirmDialog.setConfirmListener(this);
	}


	@Override
	public void onBackPressed() {
		String title = titleInput.getText().toString();
		String descr = subTitleInput.getText().toString();
		// Check for changes, leave if there aren't any
		if (userList != null && visibilitySwitch.isChecked() == !userList.isPrivate()
				&& title.equals(userList.getTitle()) && descr.equals(userList.getDescription())) {
			super.onBackPressed();
		} else if (title.isEmpty() && descr.isEmpty()) {
			super.onBackPressed();
		} else {
			confirmDialog.show(ConfirmDialog.LIST_EDITOR_LEAVE);
		}
	}


	@Override
	protected void onDestroy() {
		loadingCircle.dismiss();
		updaterAsync.cancel();
		super.onDestroy();
	}


	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.userlist_create_list) {
			if (updaterAsync.isIdle()) {
				updateList();
			}
		}
	}


	@Override
	public void stopProgress() {
		updaterAsync.cancel();
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
		// retry updating list
		if (type == ConfirmDialog.LIST_EDITOR_ERROR) {
			updateList();
		}
		// leave editor
		else if (type == ConfirmDialog.LIST_EDITOR_LEAVE) {
			finish();
		}
	}


	@Override
	public void onResult(ListUpdateResult result) {
		if (result.userlist != null) {
			if (result.updated) {
				Toast.makeText(getApplicationContext(), R.string.info_list_updated, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.info_list_created, Toast.LENGTH_SHORT).show();
			}
			Intent intent = new Intent();
			intent.putExtra(KEY_UPDATED_USERLIST, result.userlist);
			setResult(RETURN_LIST_CHANGED, intent);
			finish();
		} else {
			String message = ErrorHandler.getErrorMessage(this, result.exception);
			confirmDialog.show(ConfirmDialog.LIST_EDITOR_ERROR, message);
			loadingCircle.dismiss();
		}
	}

	/**
	 * check input and create/update list
	 */
	private void updateList() {
		String titleStr = titleInput.getText().toString();
		String descrStr = subTitleInput.getText().toString();
		boolean isPublic = visibilitySwitch.isChecked();
		if (titleStr.trim().isEmpty()) {
			Toast.makeText(getApplicationContext(), R.string.error_list_title_empty, Toast.LENGTH_SHORT).show();
		} else {
			UserListUpdate mHolder;
			if (userList != null) {
				// update existing list
				mHolder = new UserListUpdate(titleStr, descrStr, isPublic, userList.getId());
			} else {
				// create new one
				mHolder = new UserListUpdate(titleStr, descrStr, isPublic);
			}
			updaterAsync.execute(mHolder, this);
			loadingCircle.show();
		}
	}
}