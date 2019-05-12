package org.nuclearfog.twidda.window;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask.Status;
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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MessagePopup extends AppCompatActivity implements OnClickListener {

    private MessageUpload messageAsync;
    private EditText receiver, text;
    private String mediaPath = "";


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_dm);
        String username = "";
        Bundle param = getIntent().getExtras();
        if (param != null) {
            username = param.getString("username");
        }

        View root = findViewById(R.id.dm_popup);
        View send = findViewById(R.id.dm_send);
        View media = findViewById(R.id.dm_media);
        receiver = findViewById(R.id.dm_receiver);
        text = findViewById(R.id.dm_text);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getPopupColor());

        receiver.append(username);
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
                    if (messageAsync != null && messageAsync.getStatus() == Status.RUNNING)
                        messageAsync.cancel(true);
                    finish();
                }
            });
            closeDialog.show();
        }
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode, returnCode, i);
        if (returnCode == RESULT_OK && i.getData() != null) {
            String[] mode = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(i.getData(), mode, null, null, null);
            if (c != null && c.moveToFirst()) {
                int index = c.getColumnIndex(mode[0]);
                mediaPath = c.getString(index);
                c.close();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PERMISSION_GRANTED)
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
                Toast.makeText(this, R.string.error_dm, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.dm_media) {
            if (mediaPath.trim().isEmpty())
                getMedia();
            else {
                Intent image = new Intent(this, ImageDetail.class);
                image.putExtra("link", new String[]{mediaPath});
                image.putExtra("storable", false);
                startActivity(image);
            }
        }
    }


    private void getMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (check == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 0);
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 0);
        }
    }
}