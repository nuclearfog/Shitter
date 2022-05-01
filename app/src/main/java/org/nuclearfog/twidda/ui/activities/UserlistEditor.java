package org.nuclearfog.twidda.ui.activities;

import static android.os.AsyncTask.Status.RUNNING;

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
import org.nuclearfog.twidda.backend.async.ListUpdater;
import org.nuclearfog.twidda.backend.api.holder.UserlistUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;
import org.nuclearfog.twidda.model.UserList;

/**
 * Activity for the list editor
 *
 * @author nuclearfog
 */
public class UserlistEditor extends AppCompatActivity implements OnClickListener, OnConfirmListener, OnProgressStopListener {

    /**
     * Key for the list ID of the list if an existing list should be updated
     */
    public static final String KEY_LIST_EDITOR_DATA = "list_edit_data";

    /**
     * Key for updated list information
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
    private CompoundButton visibility;
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
        titleInput = findViewById(R.id.list_edit_title);
        subTitleInput = findViewById(R.id.list_edit_descr);
        visibility = findViewById(R.id.list_edit_public_sw);

        loadingCircle = new ProgressDialog(this);
        confirmDialog = new ConfirmDialog(this);

        AppStyles.setEditorTheme(root, background);

        Object data = getIntent().getSerializableExtra(KEY_LIST_EDITOR_DATA);
        if (data instanceof UserList) {
            userList = (UserList) data;
            titleInput.setText(userList.getTitle());
            subTitleInput.setText(userList.getDescription());
            visibility.setChecked(!userList.isPrivate());
            popupTitle.setText(R.string.menu_edit_list);
            updateButton.setText(R.string.update_list);
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
        if (userList != null && visibility.isChecked() == !userList.isPrivate()
                && title.equals(userList.getTitle()) && descr.equals(userList.getDescription())) {
            super.onBackPressed();
        } else if (title.isEmpty() && descr.isEmpty()) {
            super.onBackPressed();
        } else {
            confirmDialog.show(DialogType.LIST_EDITOR_LEAVE);
        }
    }


    @Override
    protected void onDestroy() {
        loadingCircle.dismiss();
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.userlist_create_list) {
            if (updaterAsync == null || updaterAsync.getStatus() != RUNNING) {
                updateList();
            }
        }
    }


    @Override
    public void stopProgress() {
        if (updaterAsync != null && updaterAsync.getStatus() == RUNNING) {
            updaterAsync.cancel(true);
        }
    }


    @Override
    public void onConfirm(DialogType type) {
        // retry updating list
        if (type == DialogType.LIST_EDITOR_ERROR) {
            updateList();
        }
        // leave editor
        else if (type == DialogType.LIST_EDITOR_LEAVE) {
            finish();
        }
    }

    /**
     * called when a list was updated successfully
     */
    public void onSuccess(UserList result) {
        if (userList != null) {
            Toast.makeText(this, R.string.info_list_updated, Toast.LENGTH_SHORT).show();
            Intent data = new Intent();
            data.putExtra(KEY_UPDATED_USERLIST, result);
            setResult(RETURN_LIST_CHANGED, data);
        } else {
            // it's a new list, if no list is defined
            Toast.makeText(this, R.string.info_list_created, Toast.LENGTH_SHORT).show();
            setResult(RETURN_LIST_CREATED);
        }
        finish();
    }

    /**
     * called when an error occurs while updating a list
     *
     * @param err twitter exception
     */
    public void onError(@Nullable ErrorHandler.TwitterError err) {
        String message = ErrorHandler.getErrorMessage(this, err);
        confirmDialog.setMessage(message);
        confirmDialog.show(DialogType.LIST_EDITOR_ERROR);
        loadingCircle.dismiss();
    }

    /**
     * check input and create/update list
     */
    private void updateList() {
        String titleStr = titleInput.getText().toString();
        String descrStr = subTitleInput.getText().toString();
        boolean isPublic = visibility.isChecked();
        if (titleStr.trim().isEmpty()) {
            Toast.makeText(this, R.string.error_list_title_empty, Toast.LENGTH_SHORT).show();
        } else {
            UserlistUpdate mHolder;
            if (userList != null) {
                // update existing list
                mHolder = new UserlistUpdate(titleStr, descrStr, isPublic, userList.getId());
            } else {
                // create new one
                mHolder = new UserlistUpdate(titleStr, descrStr, isPublic, UserlistUpdate.NEW_LIST);
            }
            updaterAsync = new ListUpdater(this, mHolder);
            updaterAsync.execute();
            loadingCircle.show();
        }
    }
}