package org.nuclearfog.twidda.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.MessageUploader;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.MessageHolder;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_IMG_STORAGE;


public class MessagePopup extends AppCompatActivity implements OnClickListener {

    public static final String KEY_DM_PREFIX = "dm_prefix";
    private static final String[] PERM_READ = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] PICK_IMAGE = {MediaStore.Images.Media.DATA};
    private static final int REQ_PERM_READ = 4;

    private MessageUploader messageAsync;
    private EditText receiver, text;
    private String mediaPath = "";


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_dm);
        String addtion = "";
        Bundle param = getIntent().getExtras();
        if (param != null && param.containsKey(KEY_DM_PREFIX)) {
            addtion = param.getString(KEY_DM_PREFIX);
        }

        View root = findViewById(R.id.dm_popup);
        View send = findViewById(R.id.dm_send);
        View media = findViewById(R.id.dm_media);
        receiver = findViewById(R.id.dm_receiver);
        text = findViewById(R.id.dm_text);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getPopupColor());
        FontTool.setViewFontAndColor(settings, root);

        receiver.append(addtion);
        send.setOnClickListener(this);
        media.setOnClickListener(this);
    }


    @Override
    public void onBackPressed() {
        if (text.getText().toString().isEmpty() && mediaPath.isEmpty()) {
            super.onBackPressed();
        } else {
            Builder closeDialog = new Builder(this, R.style.InfoDialog);
            closeDialog.setMessage(R.string.confirm_cancel_message);
            closeDialog.setNegativeButton(R.string.confirm_no, null);
            closeDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            closeDialog.show();
        }
    }


    @Override
    protected void onDestroy() {
        if (messageAsync != null && messageAsync.getStatus() == RUNNING)
            messageAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent intent) {
        super.onActivityResult(reqCode, returnCode, intent);
        if (reqCode == REQ_PERM_READ && returnCode == RESULT_OK) {
            if (intent != null && intent.getData() != null) {
                Cursor c = getContentResolver().query(intent.getData(), PICK_IMAGE, null, null, null);
                if (c != null && c.moveToFirst()) {
                    int index = c.getColumnIndex(PICK_IMAGE[0]);
                    mediaPath = c.getString(index);
                    c.close();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_PERM_READ && grantResults[0] == PERMISSION_GRANTED)
            getMedia();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dm_send) {
            String username = receiver.getText().toString();
            String message = text.getText().toString();
            if (!username.trim().isEmpty() && (!message.trim().isEmpty() || !mediaPath.isEmpty())) {
                MessageHolder messageHolder = new MessageHolder(username, message, mediaPath);
                messageAsync = new MessageUploader(this, messageHolder);
                messageAsync.execute();
            } else {
                Toast.makeText(this, R.string.error_dm, LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.dm_media) {
            if (mediaPath.trim().isEmpty())
                getMedia();
            else {
                Intent image = new Intent(this, MediaViewer.class);
                image.putExtra(KEY_MEDIA_LINK, new String[]{mediaPath});
                image.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMG_STORAGE);
                startActivity(image);
            }
        }
    }


    private void getMedia() {
        boolean accessGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PERMISSION_DENIED) {
                requestPermissions(PERM_READ, REQ_PERM_READ);
                accessGranted = false;
            }
        }
        if (accessGranted) {
            Intent galleryIntent = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
            if (galleryIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(galleryIntent, REQ_PERM_READ);
            else
                Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
        }
    }
}