package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ListUpdater;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.ListHolder;
import org.nuclearfog.twidda.backend.items.UserList;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.activity.ListDetail.RET_LIST_CHANGED;
import static org.nuclearfog.twidda.activity.ListDetail.RET_LIST_DATA;
import static org.nuclearfog.twidda.activity.UserLists.RET_LIST_CREATED;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.LISTPOPUP_LEAVE;

/**
 * Popup activity for the list editor
 */
public class ListPopup extends AppCompatActivity implements OnClickListener, OnDialogClick {

    /**
     * Key for the list ID of the list if an existing list should be updated
     */
    public static final String KEY_LIST_EDITOR_DATA = "list_edit_data";

    private ListUpdater updaterAsync;
    private EditText titleInput, subTitleInput;
    private CompoundButton visibility;
    private View progressCircle;
    private Dialog leaveDialog;
    @Nullable
    private UserList userList;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_userlist);
        View root = findViewById(R.id.list_popup_root);
        Button updateButton = findViewById(R.id.userlist_create_list);
        TextView popupTitle = findViewById(R.id.popup_list_title);
        titleInput = findViewById(R.id.list_edit_title);
        subTitleInput = findViewById(R.id.list_edit_descr);
        visibility = findViewById(R.id.list_edit_public_sw);
        progressCircle = findViewById(R.id.list_popup_loading);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        AppStyles.setTheme(settings, root, settings.getPopupColor());

        Object data = getIntent().getSerializableExtra(KEY_LIST_EDITOR_DATA);
        if (data instanceof UserList) {
            userList = (UserList) data;
            titleInput.setText(userList.getTitle());
            subTitleInput.setText(userList.getDescription());
            visibility.setChecked(!userList.isPrivate());
            popupTitle.setText(R.string.menu_edit_list);
            updateButton.setText(R.string.update_list);
        }
        leaveDialog = DialogBuilder.create(this, LISTPOPUP_LEAVE, this);
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
            String titleStr = titleInput.getText().toString();
            String descrStr = subTitleInput.getText().toString();
            boolean isPublic = visibility.isChecked();
            if (titleStr.trim().isEmpty()) {
                Toast.makeText(this, R.string.error_list_title_empty, Toast.LENGTH_SHORT).show();
            } else if (updaterAsync == null || updaterAsync.getStatus() != RUNNING) {
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
                progressCircle.setVisibility(VISIBLE);
            }
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        finish();
    }

    /**
     * called when a list was updated successfully
     */
    public void onSuccess(UserList result) {
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
    public void onError(EngineException err) {
        ErrorHandler.handleFailure(this, err);
        progressCircle.setVisibility(INVISIBLE);
    }
}