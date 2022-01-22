package org.nuclearfog.twidda.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.*;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.MessageUpdater;
import org.nuclearfog.twidda.backend.api.holder.DirectmessageUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.dialog.ProgressDialog;
import org.nuclearfog.twidda.dialog.ProgressDialog.OnProgressStopListener;

/**
 * Direct message popup activity
 *
 * @author nuclearfog
 */
public class MessageEditor extends MediaActivity implements OnClickListener, OnConfirmListener, OnProgressStopListener {

    /**
     * key for the screen name if any
     */
    public static final String KEY_DM_PREFIX = "dm_prefix";

    private MessageUpdater messageAsync;

    private ProgressDialog loadingCircle;
    private ConfirmDialog confirmDialog;

    private EditText receiver, message;
    private ImageButton media, preview;

    @Nullable
    private Uri mediaUri;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppStyles.setFontScale(newBase));
    }


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_dm);
        ViewGroup root = findViewById(R.id.dm_popup);
        ImageButton send = findViewById(R.id.dm_send);
        ImageView background = findViewById(R.id.dm_background);
        media = findViewById(R.id.dm_media);
        preview = findViewById(R.id.dm_preview);
        receiver = findViewById(R.id.dm_receiver);
        message = findViewById(R.id.dm_text);

        loadingCircle = new ProgressDialog(this);
        confirmDialog = new ConfirmDialog(this);

        String prefix = getIntent().getStringExtra(KEY_DM_PREFIX);
        if (prefix != null) {
            receiver.append(prefix);
        }
        send.setImageResource(R.drawable.right);
        media.setImageResource(R.drawable.attachment);
        preview.setImageResource(R.drawable.image);
        preview.setVisibility(GONE);
        AppStyles.setEditorTheme(root, background);

        send.setOnClickListener(this);
        media.setOnClickListener(this);
        preview.setOnClickListener(this);
        loadingCircle.addOnProgressStopListener(this);
        confirmDialog.setConfirmListener(this);
    }


    @Override
    public void onBackPressed() {
        if (receiver.getText().length() == 0 && message.getText().length() == 0 && mediaUri == null) {
            super.onBackPressed();
        } else {
            confirmDialog.show(DialogType.MESSAGE_EDITOR_LEAVE);
        }
    }


    @Override
    protected void onDestroy() {
        if (messageAsync != null && messageAsync.getStatus() == RUNNING)
            messageAsync.cancel(true);
        loadingCircle.dismiss();
        super.onDestroy();
    }


    @Override
    protected void onAttachLocation(@Nullable Location location) {
    }


    @Override
    protected void onMediaFetched(int resultType, @NonNull Uri uri) {
        if (resultType == REQUEST_IMAGE) {
            preview.setVisibility(VISIBLE);
            media.setVisibility(GONE);
            mediaUri = uri;
        }
    }


    @Override
    public void onClick(View v) {
        // send direct message
        if (v.getId() == R.id.dm_send) {
            if (messageAsync == null || messageAsync.getStatus() != RUNNING) {
                sendMessage();
            }
        }
        // get media
        else if (v.getId() == R.id.dm_media) {
            getMedia(REQUEST_IMAGE);
        }
        // open media
        else if (v.getId() == R.id.dm_preview) {
            Intent image = new Intent(this, ImageViewer.class);
            image.putExtra(ImageViewer.IMAGE_URIS, new Uri[]{mediaUri});
            image.putExtra(ImageViewer.IMAGE_DOWNLOAD, false);
            startActivity(image);
        }
    }


    @Override
    public void stopProgress() {
        if (messageAsync != null && messageAsync.getStatus() == RUNNING) {
            messageAsync.cancel(true);
        }
    }


    @Override
    public void onConfirm(DialogType type) {
        // retry sending message
        if (type == DialogType.MESSAGE_EDITOR_ERROR) {
            sendMessage();
        }
        // leave message editor
        else if (type == DialogType.MESSAGE_EDITOR_LEAVE) {
            finish();
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
    public void onError(@Nullable ErrorHandler.TwitterError error) {
        if (!confirmDialog.isShowing()) {
            String message = ErrorHandler.getErrorMessage(this, error);
            confirmDialog.setMessage(message);
            confirmDialog.show(DialogType.MESSAGE_EDITOR_ERROR);
        }
        loadingCircle.dismiss();
    }

    /**
     * check inputs and send message
     */
    private void sendMessage() {
        String username = receiver.getText().toString();
        String message = this.message.getText().toString();
        if (!username.trim().isEmpty() && (!message.trim().isEmpty() || mediaUri != null)) {
            DirectmessageUpdate holder = new DirectmessageUpdate(username, message);
            if (mediaUri != null)
                holder.addMedia(getApplicationContext(), mediaUri);
            messageAsync = new MessageUpdater(this, holder);
            messageAsync.execute();
            if (!loadingCircle.isShowing()) {
                loadingCircle.show();
            }
        } else {
            Toast.makeText(this, R.string.error_dm, LENGTH_SHORT).show();
        }
    }
}