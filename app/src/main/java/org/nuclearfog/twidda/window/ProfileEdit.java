package org.nuclearfog.twidda.window;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ProfileEditor;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;

/**
 * @see org.nuclearfog.twidda.backend.ProfileEditor
 */
public class ProfileEdit extends AppCompatActivity implements View.OnClickListener {

    private ProfileEditor mEdit;
    private TextView txtImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_editprofile);

        Toolbar toolbar = findViewById(R.id.editprofile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        View root = findViewById(R.id.page_edit);
        View uploadButton = findViewById(R.id.edit_upload);
        txtImg = findViewById(R.id.pb_path);

        root.setBackgroundColor(settings.getBackgroundColor());
        uploadButton.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mEdit == null) {
            mEdit = new ProfileEditor(this);
            mEdit.execute(ProfileEditor.READ_DATA);
        }
    }


    @Override
    protected void onStop() {
        if (mEdit != null && mEdit.getStatus() == AsyncTask.Status.RUNNING)
            mEdit.cancel(true);
        super.onStop();
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
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mEdit == null || mEdit.getStatus() != RUNNING) {
                    save();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, Intent i) {
        super.onActivityResult(reqCode, returnCode, i);
        if (returnCode == RESULT_OK && i.getData() != null) {
            String[] mode = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(i.getData(), mode, null, null, null);
            if (c != null && c.moveToFirst()) {
                int index = c.getColumnIndex(mode[0]);
                String mediaPath = c.getString(index);
                txtImg.setText(mediaPath);
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
        if (v.getId() == R.id.edit_upload) {
            getMedia();
        }
    }


    private void save() {
        EditText name = findViewById(R.id.edit_name);
        if (name.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.edit_empty_name, Toast.LENGTH_SHORT).show();
        } else {
            if (mEdit != null && mEdit.getStatus() == RUNNING)
                mEdit.cancel(true);
            mEdit = new ProfileEditor(this);
            mEdit.execute(ProfileEditor.WRITE_DATA);
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
