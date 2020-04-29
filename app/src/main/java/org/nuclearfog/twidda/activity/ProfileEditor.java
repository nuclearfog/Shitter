package org.nuclearfog.twidda.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import androidx.appcompat.app.AlertDialog.Builder;
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
import static android.view.View.INVISIBLE;
import static android.widget.Toast.LENGTH_SHORT;


public class ProfileEditor extends AppCompatActivity implements OnClickListener {

    private static final String[] PERM_READ = {READ_EXTERNAL_STORAGE};
    private static final String[] MEDIA_MODE = {MediaStore.Images.Media.DATA};
    private static final int REQ_PERM = 3;
    private static final int REQ_PROFILE_IMG = 4;
    private static final int REQ_PROFILE_BANNER = 5;

    private ProfileUpdater editorAsync;
    private ImageView profile_image, profile_banner;
    private EditText name, link, loc, bio;
    private Button add_banner_btn;
    private String profileLink, bannerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_editprofile);
        Toolbar toolbar = findViewById(R.id.editprofile_toolbar);
        View root = findViewById(R.id.page_edit);
        profile_image = findViewById(R.id.edit_pb);
        profile_banner = findViewById(R.id.edit_banner);
        add_banner_btn = findViewById(R.id.edit_add_banner);
        name = findViewById(R.id.edit_name);
        link = findViewById(R.id.edit_link);
        loc = findViewById(R.id.edit_location);
        bio = findViewById(R.id.edit_bio);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.page_profile_edior);
        GlobalSettings settings = GlobalSettings.getInstance(this);
        FontTool.setViewFontAndColor(settings, root);
        root.setBackgroundColor(settings.getBackgroundColor());
        profile_image.setOnClickListener(this);
        profile_banner.setOnClickListener(this);
        add_banner_btn.setOnClickListener(this);
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
        Builder closeDialog = new Builder(this, R.style.InfoDialog);
        closeDialog.setMessage(R.string.confirm_discard);
        closeDialog.setNegativeButton(R.string.confirm_no, null);
        closeDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
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
                if (username.trim().isEmpty()) {
                    Toast.makeText(this, R.string.error_empty_name, LENGTH_SHORT).show();
                } else {
                    UserHolder userHolder = new UserHolder(username, userLink, userLoc, userBio, profileLink, bannerLink);
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
        if (returnCode == RESULT_OK && (reqCode == REQ_PROFILE_IMG || reqCode == REQ_PROFILE_BANNER)) {
            if (intent != null && intent.getData() != null) {
                Cursor c = getContentResolver().query(intent.getData(), MEDIA_MODE, null, null, null);
                if (c != null && c.moveToFirst()) {
                    int index = c.getColumnIndex(MEDIA_MODE[0]);
                    String mediaPath = c.getString(index);
                    Bitmap image = BitmapFactory.decodeFile(mediaPath);
                    if (reqCode == REQ_PROFILE_IMG) {
                        profile_image.setImageBitmap(image);
                        profileLink = mediaPath;
                    } else {
                        profile_banner.setImageBitmap(image);
                        add_banner_btn.setVisibility(INVISIBLE);
                        bannerLink = mediaPath;
                    }
                    c.close();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_PERM && grantResults[0] == PERMISSION_GRANTED)
            getMedia(requestCode);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_pb:
                getMedia(REQ_PROFILE_IMG);
                break;

            case R.id.edit_add_banner:
            case R.id.edit_banner:
                getMedia(REQ_PROFILE_BANNER);
                break;
        }
    }


    public void setUser(TwitterUser user) {
        String pbLink = user.getImageLink();
        String bnLink = user.getBannerLink() + "/600x200";

        if (!user.hasDefaultProfileImage())
            pbLink += "_bigger";
        Picasso.get().load(pbLink).into(profile_image);
        if (user.hasBannerImg()) {
            Picasso.get().load(bnLink).into(profile_banner);
            add_banner_btn.setVisibility(INVISIBLE);
        }
        name.setText(user.getUsername());
        link.setText(user.getLink());
        loc.setText(user.getLocation());
        bio.setText(user.getBio());
    }


    private void getMedia(int request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PackageManager.PERMISSION_GRANTED) {
                Intent media = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
                if (media.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(media, request);
                else
                    Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
            } else {
                requestPermissions(PERM_READ, REQ_PERM);
            }
        } else {
            Intent media = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
            if (media.resolveActivity(getPackageManager()) != null)
                startActivityForResult(media, request);
            else
                Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
        }
    }
}