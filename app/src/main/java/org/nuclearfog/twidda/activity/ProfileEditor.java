package org.nuclearfog.twidda.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ProfileUpdater;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserHolder;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MediaType.IMAGE;
import static org.nuclearfog.twidda.activity.MediaViewer.MediaType.IMAGE_STORAGE;


public class ProfileEditor extends AppCompatActivity implements OnClickListener {

    private static final String[] PERM_READ = {READ_EXTERNAL_STORAGE};
    private static final String[] MEDIA_MODE = {MediaStore.Images.Media.DATA};
    private static final int REQ_PERM = 3;
    private static final int REQ_PB = 4;

    private ProfileUpdater editorAsync;
    private TwitterUser user;
    private ImageView pb_image;
    private EditText name, link, loc, bio;
    private Button txtImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_editprofile);
        Toolbar toolbar = findViewById(R.id.editprofile_toolbar);
        View root = findViewById(R.id.page_edit);
        pb_image = findViewById(R.id.edit_pb);
        txtImg = findViewById(R.id.edit_upload);
        name = findViewById(R.id.edit_name);
        link = findViewById(R.id.edit_link);
        loc = findViewById(R.id.edit_location);
        bio = findViewById(R.id.edit_bio);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.page_profile_edior);
        GlobalSettings settings = GlobalSettings.getInstance(this);
        FontTool.setViewFont(root, settings.getFontFace());
        root.setBackgroundColor(settings.getBackgroundColor());
        txtImg.setOnClickListener(this);
        pb_image.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (editorAsync == null) {
            editorAsync = new ProfileUpdater(this);
            editorAsync.execute();
        }
    }


    @Override
    protected void onDestroy() {
        if (editorAsync != null && editorAsync.getStatus() == RUNNING)
            editorAsync.cancel(true);
        super.onDestroy();
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
                String username = name.getText().toString();
                String userLink = link.getText().toString();
                String userLoc = loc.getText().toString();
                String userBio = bio.getText().toString();
                String imgLink = txtImg.getText().toString();
                if (username.trim().isEmpty()) {
                    Toast.makeText(this, R.string.edit_empty_name, LENGTH_SHORT).show();
                } else {
                    UserHolder userHolder = new UserHolder(username, userLink, userLoc, userBio, imgLink);
                    editorAsync = new ProfileUpdater(this, userHolder);
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
                    pb_image.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
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
        switch (v.getId()) {
            case R.id.edit_upload:
                getMedia();
                break;

            case R.id.edit_pb:
                if (user != null) {
                    Intent image = new Intent(getApplicationContext(), MediaViewer.class);
                    if (!txtImg.getText().toString().isEmpty()) {
                        String[] mediaLink = new String[]{txtImg.getText().toString()};
                        image.putExtra(KEY_MEDIA_LINK, mediaLink);
                        image.putExtra(KEY_MEDIA_TYPE, IMAGE_STORAGE);
                    } else {
                        String[] mediaLink = new String[]{user.getImageLink()};
                        image.putExtra(KEY_MEDIA_LINK, mediaLink);
                        image.putExtra(KEY_MEDIA_TYPE, IMAGE);
                    }
                    startActivity(image);
                }
                break;
        }
    }


    public void setUser(TwitterUser user) {
        String pbLink = user.getImageLink() + "_bigger";
        Picasso.get().load(pbLink).into(pb_image);
        name.setText(user.getUsername());
        link.setText(user.getLink());
        loc.setText(user.getLocation());
        bio.setText(user.getBio());
        this.user = user;
    }


    private void getMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PackageManager.PERMISSION_GRANTED) {
                Intent media = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
                if (media.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(media, REQ_PB);
                else
                    Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
            } else {
                requestPermissions(PERM_READ, REQ_PERM);
            }
        } else {
            Intent media = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
            if (media.resolveActivity(getPackageManager()) != null)
                startActivityForResult(media, REQ_PB);
            else
                Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
        }
    }
}