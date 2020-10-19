package org.nuclearfog.twidda.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.UserListUpdater;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.ListHolder;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.activity.TwitterList.RET_LIST_CREATED;

/**
 * Popup activity for the list editor
 */
public class ListPopup extends AppCompatActivity implements OnClickListener {

    /**
     * Key for the list ID of the list if an existing list should be updated
     */
    public static final String KEY_LIST_ID = "list_id";

    /**
     * Key for the title of the list
     */
    public static final String KEY_LIST_TITLE = "list_title";

    /**
     * Key for the list description
     */
    public static final String KEY_LIST_DESCR = "list_description";

    /**
     * Key for the visibility of the list
     */
    public static final String KEY_LIST_VISIB = "list_visibility";

    private UserListUpdater updaterAsync;
    private EditText title, description;
    private CompoundButton visibility;
    private View progressCircle;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_userlist);
        View root = findViewById(R.id.list_popup_root);
        Button update = findViewById(R.id.userlist_create_list);
        title = findViewById(R.id.list_edit_title);
        description = findViewById(R.id.list_edit_descr);
        visibility = findViewById(R.id.list_edit_public_sw);
        progressCircle = findViewById(R.id.list_popup_loading);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getPopupColor());
        FontTool.setViewFont(settings, root);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title.setText(extras.getString(KEY_LIST_TITLE, ""));
            description.setText(extras.getString(KEY_LIST_DESCR, ""));
            visibility.setChecked(extras.getBoolean(KEY_LIST_VISIB, true));
        }
        update.setOnClickListener(this);
    }


    @Override
    public void onBackPressed() {
        Bundle extras = getIntent().getExtras();
        String titleStr = "";
        String descrStr = "";
        if (extras != null) {
            titleStr = extras.getString(KEY_LIST_TITLE, titleStr);
            descrStr = extras.getString(KEY_LIST_DESCR, descrStr);
        }
        if (titleStr.equals(title.getText().toString()) && descrStr.equals(description.getText().toString())) {
            super.onBackPressed();
        } else {
            Builder alertDialog = new Builder(this, R.style.ConfirmDialog);
            alertDialog.setMessage(R.string.confirm_discard);
            alertDialog.setNegativeButton(R.string.confirm_no, null);
            alertDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.show();
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.userlist_create_list) {
            String titleStr = title.getText().toString();
            String descrStr = description.getText().toString();
            boolean isPublic = visibility.isChecked();
            if (titleStr.trim().isEmpty() || descrStr.trim().isEmpty()) {
                Toast.makeText(this, R.string.userlist_error_empty_text, Toast.LENGTH_SHORT).show();
            } else if (updaterAsync == null || updaterAsync.getStatus() != RUNNING) {
                Bundle extras = getIntent().getExtras();
                ListHolder mHolder;
                if (extras != null && extras.containsKey(KEY_LIST_ID)) {
                    long id = extras.getLong(KEY_LIST_ID);
                    mHolder = new ListHolder(titleStr, descrStr, isPublic, id);
                } else {
                    mHolder = new ListHolder(titleStr, descrStr, isPublic);
                }
                updaterAsync = new UserListUpdater(this);
                updaterAsync.execute(mHolder);
            }
        }
    }

    /**
     * called when an update starts
     */
    public void startLoading() {
        progressCircle.setVisibility(VISIBLE);
    }

    /**
     * called when a list was updated successfully
     */
    public void onSuccess() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(KEY_LIST_ID)) {
            Toast.makeText(this, R.string.info_list_updated, Toast.LENGTH_SHORT).show();
        } else {
            // it's a new list, if no list ID is defined
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
        if (err != null)
            ErrorHandler.handleFailure(this, err);
        progressCircle.setVisibility(INVISIBLE);
    }
}