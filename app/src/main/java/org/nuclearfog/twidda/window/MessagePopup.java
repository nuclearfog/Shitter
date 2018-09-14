package org.nuclearfog.twidda.window;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ImagePopup;
import org.nuclearfog.twidda.backend.MessageUpload;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MessagePopup extends AppCompatActivity implements View.OnClickListener {

    private EditText receiver, text;
    private String mediaPath = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.messagewindow);
        String username = "";
        b = getIntent().getExtras();
        if (b != null && b.containsKey("username"))
            username = b.getString("username");

        GlobalSettings settings = GlobalSettings.getInstance(this);

        View root = findViewById(R.id.dm_popup);
        Button send = findViewById(R.id.dm_send);
        Button media = findViewById(R.id.dm_media);
        receiver = findViewById(R.id.dm_receiver);
        text = findViewById(R.id.dm_text);

        root.setBackgroundColor(settings.getTweetColor());
        receiver.append(username);
        send.setOnClickListener(this);
        media.setOnClickListener(this);
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
            if (!username.isEmpty() && (!message.isEmpty() || !mediaPath.isEmpty())) {
                MessageUpload upload = new MessageUpload(this);
                upload.execute(username, message, mediaPath);
            } else {
                Toast.makeText(this, R.string.error_dm, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.dm_media) {
            if (mediaPath.trim().isEmpty())
                getMedia();
            else
                new ImagePopup(this).execute(mediaPath);
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