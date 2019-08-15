package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ProfileEditor;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.backend.ProfileEditor.Mode.READ_DATA;
import static org.nuclearfog.twidda.backend.ProfileEditor.Mode.WRITE_DATA;


public class ProfileEdit extends AppCompatActivity implements OnClickListener {

    private static final String[] PERM_READ = {READ_EXTERNAL_STORAGE};
    private static final String[] MEDIA_MODE = {MediaStore.Images.Media.DATA};
    private static final int REQ_PERM = 3;
    private static final int REQ_PB = 4;
    private ProfileEditor editorAsync;
    private Button txtImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_editprofile);
        View root = findViewById(R.id.page_edit);
        txtImg = findViewById(R.id.edit_upload);
        Toolbar toolbar = findViewById(R.id.editprofile_toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        txtImg.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (editorAsync == null) {
            editorAsync = new ProfileEditor(this, READ_DATA);
            editorAsync.execute();
        }
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder closeDialog = new AlertDialog.Builder(this);
        closeDialog.setMessage(R.string.exit_confirm);
        closeDialog.setNegativeButton(R.string.no_confirm, null);
        closeDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        closeDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.edit, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (editorAsync == null || editorAsync.getStatus() != RUNNING) {
                EditText name = findViewById(R.id.edit_name);
                if (name.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, R.string.edit_empty_name, LENGTH_SHORT).show();
                } else {
                    editorAsync = new ProfileEditor(this, WRITE_DATA);
                    editorAsync.execute();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent intent) {
        super.onActivityResult(reqCode, returnCode, intent);
        if (reqCode == REQ_PB && returnCode == RESULT_OK) {
            if (intent != null && intent.getData() != null) {
                Cursor c = getContentResolver().query(intent.getData(), MEDIA_MODE, null, null, null);
                if (c != null && c.moveToFirst()) {
                    int index = c.getColumnIndex(MEDIA_MODE[0]);
                    String mediaPath = c.getString(index);
                    txtImg.setText(mediaPath);
                    c.close();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_PERM && grantResults[0] == PERMISSION_GRANTED)
            getMedia();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.edit_upload) {
            getMedia();
        }
    }


    private void getMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQ_PB);
            } else {
                requestPermissions(PERM_READ, REQ_PERM);
            }
        } else {
            Intent i = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQ_PB);
        }
    }
}