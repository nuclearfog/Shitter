package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.UserUpdater;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.UserHolder;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_NO_TITLE;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.UserProfile.RETURN_PROFILE_CHANGED;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.PROFILE_EDIT_LEAVE;
import static org.nuclearfog.twidda.database.GlobalSettings.BANNER_IMG_MID_RES;
import static org.nuclearfog.twidda.database.GlobalSettings.PROFILE_IMG_HIGH_RES;

/**
 * Activity for Twitter profile editor
 */
public class ProfileEditor extends AppCompatActivity implements OnClickListener, OnDismissListener, OnDialogClick {

    private static final String[] PERM_READ = {READ_EXTERNAL_STORAGE};

    /**
     * Cursor mode to get the full path to the image
     */
    private static final String[] MEDIA_MODE = {MediaStore.Images.Media.DATA};

    /**
     * Request code for read permissions
     */
    private static final int REQ_PERM = 3;

    /**
     * Request code for loading new profile image
     */
    private static final int REQ_PROFILE_IMG = 4;

    /**
     * Request code for loading new banner image
     */
    private static final int REQ_PROFILE_BANNER = 5;

    /**
     * MIME type for profile and banner images
     */
    private static final String IMG_MIME = "image/*";

    private UserUpdater editorAsync;

    private ImageView profile_image, profile_banner;
    private EditText name, link, loc, bio;
    private Dialog loadingCircle, closeDialog;
    private Button addBannerBtn;
    private View changeBannerBtn;

    private TwitterUser user;
    private String profileLink, bannerLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_editprofile);
        Toolbar toolbar = findViewById(R.id.editprofile_toolbar);
        View root = findViewById(R.id.page_edit);
        View header = findViewById(R.id.editprofile_header);
        profile_image = findViewById(R.id.edit_pb);
        profile_banner = findViewById(R.id.edit_banner);
        addBannerBtn = findViewById(R.id.edit_add_banner);
        changeBannerBtn = findViewById(R.id.edit_change_banner);
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
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        int layoutHeight = displaySize.x / 3;
        int buttonHeight = (int) getResources().getDimension(R.dimen.editprofile_dummy_height);
        header.getLayoutParams().height = layoutHeight + buttonHeight;
        header.requestLayout();

        closeDialog = DialogBuilder.create(this, PROFILE_EDIT_LEAVE, this);
        loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
        loadingCircle.setCanceledOnTouchOutside(false);
        loadingCircle.setContentView(load);
        cancelButton.setVisibility(VISIBLE);

        profile_image.setOnClickListener(this);
        profile_banner.setOnClickListener(this);
        addBannerBtn.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        loadingCircle.setOnDismissListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (editorAsync == null) {
            editorAsync = new UserUpdater(this);
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
        } else if (!closeDialog.isShowing()) {
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
                    editorAsync = new UserUpdater(this, userHolder);
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
                        int bannerHeight = profile_banner.getMeasuredWidth() / 3;
                        if (bannerHeight > 0)
                            profile_banner.setMaxHeight(bannerHeight);
                        profile_banner.setImageBitmap(image);
                        addBannerBtn.setVisibility(INVISIBLE);
                        changeBannerBtn.setVisibility(VISIBLE);
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
        int viewId = v.getId();
        // select net profile image
        if (viewId == R.id.edit_pb) {
            getMedia(REQ_PROFILE_IMG);
        }
        // select new banner image
        else if (viewId == R.id.edit_add_banner || viewId == R.id.edit_banner) {
            getMedia(REQ_PROFILE_BANNER);
        }
        // stop update
        else if (viewId == R.id.kill_button) {
            loadingCircle.dismiss();
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        if (editorAsync != null && editorAsync.getStatus() == RUNNING) {
            editorAsync.cancel(true);
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        if (type == PROFILE_EDIT_LEAVE) {
            finish();
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

    /**
     * Set current user's information
     *
     * @param user Current user
     */
    public void setUser(TwitterUser user) {
        String pbLink = user.getImageLink();
        String bnLink = user.getBannerLink() + BANNER_IMG_MID_RES;

        if (!user.hasDefaultProfileImage())
            pbLink += PROFILE_IMG_HIGH_RES;
        Picasso.get().load(pbLink).into(profile_image);
        if (user.hasBannerImg()) {
            Picasso.get().load(bnLink).into(profile_banner);
            addBannerBtn.setVisibility(INVISIBLE);
            changeBannerBtn.setVisibility(VISIBLE);
        } else {
            addBannerBtn.setVisibility(VISIBLE);
            changeBannerBtn.setVisibility(INVISIBLE);
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

    /**
     * Get images from storage or ask for permission
     *
     * @param request image type to load from storage
     */
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
            Intent mediaSelect = new Intent(ACTION_PICK);
            mediaSelect.setType(IMG_MIME);
            try {
                startActivityForResult(mediaSelect, request);
            } catch (ActivityNotFoundException err) {
                Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
            }
        }
    }
}