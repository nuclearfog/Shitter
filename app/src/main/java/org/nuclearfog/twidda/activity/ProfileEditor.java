package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.ProfileUpdater;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.holder.UserHolder;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_NO_TITLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.UserProfile.RETURN_PROFILE_CHANGED;


public class ProfileEditor extends AppCompatActivity implements OnClickListener, OnDismissListener {

    private static final String[] PERM_READ = {READ_EXTERNAL_STORAGE};
    private static final String[] MEDIA_MODE = {MediaStore.Images.Media.DATA};
    private static final int REQ_PERM = 3;
    private static final int REQ_PROFILE_IMG = 4;
    private static final int REQ_PROFILE_BANNER = 5;

    private ProfileUpdater editorAsync;
    private ImageView profile_image, profile_banner;
    private EditText name, link, loc, bio;
    private Button add_banner_btn;
    private Dialog loadingCircle;
    private TwitterUser user;
    private String profileLink, bannerLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        loadingCircle = new Dialog(this, R.style.LoadingDialog);
        View load = View.inflate(this, R.layout.item_load, null);
        View cancelButton = load.findViewById(R.id.kill_button);

        toolbar.setTitle(R.string.page_profile_edior);
        setSupportActionBar(toolbar);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        FontTool.setViewFontAndColor(settings, root);
        root.setBackgroundColor(settings.getBackgroundColor());

        loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
        loadingCircle.setCanceledOnTouchOutside(false);
        loadingCircle.setContentView(load);
        cancelButton.setVisibility(VISIBLE);

        profile_image.setOnClickListener(this);
        profile_banner.setOnClickListener(this);
        add_banner_btn.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        loadingCircle.setOnDismissListener(this);
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
        String username = name.getText().toString();
        String userLink = link.getText().toString();
        String userLoc = loc.getText().toString();
        String userBio = bio.getText().toString();
        if (username.equals(user.getUsername()) && userLink.equals(user.getLink())
                && userLoc.equals(user.getLocation()) && userBio.equals(user.getBio())
                && profileLink == null && bannerLink == null) {
            finish();
        } else {
            Builder closeDialog = new Builder(this, R.style.ConfirmDialog);
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.edit, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (editorAsync == null || editorAsync.getStatus() != RUNNING) {
                String username = name.getText().toString();
                String userLink = link.getText().toString();
                String userLoc = loc.getText().toString();
                String userBio = bio.getText().toString();
                if (username.trim().isEmpty()) {
                    String errMsg = getString(R.string.error_empty_name);
                    name.setError(errMsg);
                } else if (!userLink.isEmpty() && userLink.contains(" ")) {
                    String errMsg = getString(R.string.error_invalid_link);
                    link.setError(errMsg);
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
    protected void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
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

            case R.id.kill_button:
                loadingCircle.dismiss();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (editorAsync != null && editorAsync.getStatus() == RUNNING) {
            editorAsync.cancel(true);
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
        this.user = user;
    }

    /**
     * called after user profile was updated successfully
     */
    public void setSuccess() {
        Toast.makeText(this, R.string.info_profile_updated, Toast.LENGTH_SHORT).show();
        setResult(RETURN_PROFILE_CHANGED);
        finish();
    }

    /**
     * called after an error occurs
     *
     * @param err Engine Exception
     */
    public void setError(EngineException err) {
        ErrorHandler.handleFailure(this, err);
        if (user == null) {
            finish();
        }
    }


    private void getMedia(int request) {
        boolean accessGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(READ_EXTERNAL_STORAGE);
            if (check == PackageManager.PERMISSION_DENIED) {
                requestPermissions(PERM_READ, REQ_PERM);
                accessGranted = false;
            }
        }
        if (accessGranted) {
            Intent media = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
            if (media.resolveActivity(getPackageManager()) != null)
                startActivityForResult(media, request);
            else
                Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
        }
    }
}