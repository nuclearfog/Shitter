package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ListUpdater;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.ListHolder;
import org.nuclearfog.twidda.backend.model.TwitterList;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.dialog.ProgressDialog;
import org.nuclearfog.twidda.dialog.ProgressDialog.OnProgressStopListener;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.ListDetail.RET_LIST_CHANGED;
import static org.nuclearfog.twidda.activity.ListDetail.RET_LIST_DATA;
import static org.nuclearfog.twidda.activity.UserLists.RET_LIST_CREATED;

/**
 * Activity for the list editor
 *
 * @author nuclearfog
 */
public class ListEditor extends AppCompatActivity implements OnClickListener, OnConfirmListener, OnProgressStopListener {

    /**
     * Key for the list ID of the list if an existing list should be updated
     */
    public static final String KEY_LIST_EDITOR_DATA = "list_edit_data";

    private ListUpdater updaterAsync;
    private EditText titleInput, subTitleInput;
    private CompoundButton visibility;
    private Dialog leaveDialog, loadingCircle;
    private AlertDialog errorDialog;
    @Nullable
    private TwitterList userList;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_userlist);
        View root = findViewById(R.id.list_popup_root);
        ImageView background = findViewById(R.id.userlist_popup_background);
        Button updateButton = findViewById(R.id.userlist_create_list);
        TextView popupTitle = findViewById(R.id.popup_list_title);
        titleInput = findViewById(R.id.list_edit_title);
        subTitleInput = findViewById(R.id.list_edit_descr);
        visibility = findViewById(R.id.list_edit_public_sw);

        loadingCircle = new ProgressDialog(this, this);
        leaveDialog = new ConfirmDialog(this, DialogType.LIST_EDITOR_LEAVE, this);
        errorDialog = new ConfirmDialog(this, DialogType.LIST_EDITOR_ERROR, this);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        AppStyles.setEditorTheme(settings, root, background);

        Object data = getIntent().getSerializableExtra(KEY_LIST_EDITOR_DATA);
        if (data instanceof TwitterList) {
            userList = (TwitterList) data;
            titleInput.setText(userList.getTitle());
            subTitleInput.setText(userList.getDescription());
            visibility.setChecked(!userList.isPrivate());
            popupTitle.setText(R.string.menu_edit_list);
            updateButton.setText(R.string.update_list);
        }
        updateButton.setOnClickListener(this);
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
            if (!leaveDialog.isShowing()) {
                leaveDialog.show();
            }
        }
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
    public void onSuccess(TwitterList result) {
        if (userList != null) {
            Toast.makeText(this, R.string.info_list_updated, Toast.LENGTH_SHORT).show();
            Intent data = new Intent();
            data.putExtra(RET_LIST_DATA, result);
            setResult(RET_LIST_CHANGED, data);
        } else {
            // it's a new list, if no list is defined
            Toast.makeText(this, R.string.info_list_created, Toast.LENGTH_SHORT).show();
            setResult(RET_LIST_CREATED);
        }
        finish();
    }

    /**
     * called when an error occurs while updating a list
     *
     * @param err twitter exception
     */
    public void onError(@Nullable EngineException err) {
        if (!errorDialog.isShowing()) {
            String message = ErrorHandler.getErrorMessage(this, err);
            errorDialog.setMessage(message);
            errorDialog.show();
        }
        if (loadingCircle.isShowing()) {
            loadingCircle.dismiss();
        }
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
            ListHolder mHolder;
            if (userList != null) {
                // update existing list
                mHolder = new ListHolder(titleStr, descrStr, isPublic, userList.getId());
            } else {
                // create new one
                mHolder = new ListHolder(titleStr, descrStr, isPublic);
            }
            updaterAsync = new ListUpdater(this);
            updaterAsync.execute(mHolder);
            loadingCircle.show();
        }
    }
}