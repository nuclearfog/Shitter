package org.nuclearfog.twidda.window;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.MessageUpload;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.window.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.window.MediaViewer.MediaType.IMAGE_STORAGE;


public class MessagePopup extends AppCompatActivity implements OnClickListener {

    public static final String KEY_DM_ADDITION = "addition";
    private static final String[] PERM_READ = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] PICK_IMAGE = {MediaStore.Images.Media.DATA};
    private static final int REQ_PERM_READ = 4;

    private MessageUpload messageAsync;
    private EditText receiver, text;
    private String mediaPath = "";


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_dm);
        String addtion = "";
        Bundle param = getIntent().getExtras();
        if (param != null) {
            addtion = param.getString(KEY_DM_ADDITION, "");
        }

        View root = findViewById(R.id.dm_popup);
        View send = findViewById(R.id.dm_send);
        View media = findViewById(R.id.dm_media);
        receiver = findViewById(R.id.dm_receiver);
        text = findViewById(R.id.dm_text);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getPopupColor());

        receiver.append(addtion);
        send.setOnClickListener(this);
        media.setOnClickListener(this);
    }


    @Override
    public void onBackPressed() {
        if (text.getText().toString().isEmpty() && mediaPath.isEmpty()) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder closeDialog = new AlertDialog.Builder(this);
            closeDialog.setMessage(R.string.cancel_message);
            closeDialog.setNegativeButton(R.string.no_confirm, null);
            closeDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (messageAsync != null && messageAsync.getStatus() == RUNNING)
                        messageAsync.cancel(true);
                    finish();
                }
            });
            closeDialog.show();
        }
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
                messageAsync = new MessageUpload(this);
                messageAsync.execute(username, message, mediaPath);
            } else {
                Toast.makeText(this, R.string.error_dm, LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.dm_media) {
            if (mediaPath.trim().isEmpty())
                getMedia();
            else {
                Intent image = new Intent(this, MediaViewer.class);
                image.putExtra(KEY_MEDIA_LINK, new String[]{mediaPath});
                image.putExtra(KEY_MEDIA_TYPE, IMAGE_STORAGE);
                startActivity(image);
            }
        }
    }


    private void getMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQ_PERM_READ);
            } else {
                requestPermissions(PERM_READ, REQ_PERM_READ);
            }
        } else {
            Intent galleryIntent = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, REQ_PERM_READ);
        }
    }
}