package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.UserListUpdater;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.ListHolder;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.nuclearfog.twidda.activity.TwitterList.RET_LIST_CREATED;

public class ListPopup extends AppCompatActivity implements OnClickListener {

    public static final String KEY_LIST_ID = "list_id";
    public static final String KEY_LIST_TITLE = "list_title";
    public static final String KEY_LIST_DESCR = "list_description";
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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title.setText(extras.getString(KEY_LIST_TITLE, ""));
            description.setText(extras.getString(KEY_LIST_DESCR, ""));
            visibility.setChecked(extras.getBoolean(KEY_LIST_VISIB, true));
        }
        update.setOnClickListener(this);
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


    public void startLoading() {
        progressCircle.setVisibility(VISIBLE);
    }


    public void onSuccess() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(KEY_LIST_ID)) {
            Toast.makeText(this, R.string.info_list_updated, Toast.LENGTH_SHORT).show();
        } else {
            // if no list ID is defined, it's a new list
            Toast.makeText(this, R.string.info_list_created, Toast.LENGTH_SHORT).show();
            setResult(RET_LIST_CREATED);
        }
        finish();
    }


    public void onError(EngineException err) {
        if (err != null)
            ErrorHandler.handleFailure(this, err);
        progressCircle.setVisibility(INVISIBLE);
    }
}