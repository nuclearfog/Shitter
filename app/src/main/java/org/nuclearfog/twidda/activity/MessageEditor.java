package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.MessageUpdater;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.MessageHolder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_NO_TITLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_IMG_S;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.MSG_POPUP_LEAVE;

/**
 * Direct message popup activity
 *
 * @author nuclearfog
 */
public class MessageEditor extends MediaActivity implements OnClickListener, OnDismissListener, OnDialogClick {

    /**
     * key for the screen name if any
     */
    public static final String KEY_DM_PREFIX = "dm_prefix";

    private MessageUpdater messageAsync;

    private EditText receiver, message;
    private ImageButton media, preview;
    private Dialog loadingCircle, leaveDialog;

    @Nullable
    private String mediaPath;


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_dm);
        View root = findViewById(R.id.dm_popup);
        ImageButton send = findViewById(R.id.dm_send);
        media = findViewById(R.id.dm_media);
        preview = findViewById(R.id.dm_preview);
        receiver = findViewById(R.id.dm_receiver);
        message = findViewById(R.id.dm_text);
        loadingCircle = new Dialog(this, R.style.LoadingDialog);
        View load = View.inflate(this, R.layout.item_load, null);
        View cancelButton = load.findViewById(R.id.kill_button);

        String prefix = getIntent().getStringExtra(KEY_DM_PREFIX);
        if (prefix != null) {
            receiver.append(prefix);
        }
        send.setImageResource(R.drawable.right);
        media.setImageResource(R.drawable.image_add);
        preview.setImageResource(R.drawable.image);
        leaveDialog = DialogBuilder.create(this, MSG_POPUP_LEAVE, this);
        loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
        loadingCircle.setCanceledOnTouchOutside(false);
        loadingCircle.setContentView(load);
        cancelButton.setVisibility(VISIBLE);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        AppStyles.setTheme(settings, root, settings.getPopupColor());

        send.setOnClickListener(this);
        media.setOnClickListener(this);
        preview.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        loadingCircle.setOnDismissListener(this);
    }


    @Override
    public void onBackPressed() {
        if (receiver.getText().length() == 0 && message.getText().length() == 0 && mediaPath == null) {
            super.onBackPressed();
        } else if (!leaveDialog.isShowing()) {
            leaveDialog.show();
        }
    }


    @Override
    protected void onDestroy() {
        if (messageAsync != null && messageAsync.getStatus() == RUNNING)
            messageAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    protected void onAttachLocation(@Nullable Location location) {
    }


    @Override
    protected void onMediaFetched(int resultType, String path) {
        if (resultType == REQUEST_IMAGE) {
            preview.setVisibility(VISIBLE);
            media.setVisibility(GONE);
            mediaPath = path;
        }
    }


    @Override
    public void onClick(View v) {
        // send direct message
        if (v.getId() == R.id.dm_send) {
            String username = receiver.getText().toString();
            String message = this.message.getText().toString();
            if (!username.trim().isEmpty() && (!message.trim().isEmpty() || mediaPath != null)) {
                MessageHolder messageHolder = new MessageHolder(username, message, mediaPath);
                messageAsync = new MessageUpdater(this, messageHolder);
                messageAsync.execute();
            } else {
                Toast.makeText(this, R.string.error_dm, LENGTH_SHORT).show();
            }
        }
        // get media
        else if (v.getId() == R.id.dm_media) {
            getMedia(REQUEST_IMAGE);
        }
        // open media
        else if (v.getId() == R.id.dm_preview) {
            Intent image = new Intent(this, MediaViewer.class);
            image.putExtra(KEY_MEDIA_LINK, new String[]{mediaPath});
            image.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMG_S);
            startActivity(image);
        }
        // stop updating
        else if (v.getId() == R.id.kill_button) {
            loadingCircle.dismiss();
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        if (messageAsync != null && messageAsync.getStatus() == RUNNING) {
            messageAsync.cancel(true);
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        if (type == MSG_POPUP_LEAVE) {
            finish();
        }
    }

    /**
     * enable or disable loading dialog
     *
     * @param enable true to enable dialog
     */
    public void setLoading(boolean enable) {
        if (enable) {
            loadingCircle.show();
        } else {
            loadingCircle.dismiss();
        }
    }

    /**
     * called when direct message is sent
     */
    public void onSuccess() {
        Toast.makeText(this, R.string.info_dm_send, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * called when an error occurs
     *
     * @param error Engine Exception
     */
    public void onError(EngineException error) {
        ErrorHandler.handleFailure(this, error);
    }
}