package org.nuclearfog.twidda.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.TweetUploader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.TweetHolder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_NO_TITLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_IMG_S;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_VIDEO;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.TWEETPOPUP_ERROR;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.TWEETPOPUP_LEAVE;

/**
 * Activity to create a tweet
 */
public class TweetPopup extends AppCompatActivity implements OnClickListener, LocationListener,
        OnDismissListener, OnDialogClick {

    /**
     * key for the replied tweet if any
     */
    public static final String KEY_TWEETPOPUP_REPLYID = "tweet_replyID";

    /**
     * key for the text added to the tweet if any
     */
    public static final String KEY_TWEETPOPUP_TEXT = "tweet_text";

    private enum MediaType {
        NONE,
        GIF,
        IMAGE,
        VIDEO
    }

    /**
     * permission request for the external storage
     */
    private static final String[] PERM_STORAGE = {READ_EXTERNAL_STORAGE};

    /**
     * permission request for GPS location
     */
    private static final String[] PERM_LOCATION = {ACCESS_FINE_LOCATION};

    /**
     * Cursor mode to get the full path to the image
     */
    private static final String[] GET_MEDIA = {MediaStore.Images.Media.DATA};

    /**
     * mime type for image files with undefined extensions
     */
    private static final String TYPE_IMAGE = "image/*";

    /**
     * mime type for image files with undefined extensions
     */
    private static final String TYPE_VIDEO = "video/*";

    /**
     * request code to access gallery picker
     */
    private static final int REQ_PICK_MEDIA = 3;

    /**
     * request code to check permissions
     */
    private static final int REQ_CHECK_PERM = 4;

    /**
     * max amount of images (limited to 4 by twitter)
     */
    private static final int MAX_IMAGES = 4;

    @Nullable
    private LocationManager mLocation;
    private TweetUploader uploaderAsync;
    private Location location;
    private List<String> mediaPath;
    private ImageButton mediaBtn, previewBtn, locationBtn;
    private View locationProg;
    private Dialog loadingCircle, errorDialog, closingDialog;
    private EditText tweetText;

    private MediaType selectedFormat = MediaType.NONE;
    private String prefix = "";
    private long inReplyId = 0;
    private TweetHolder tweet;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_tweet);
        View root = findViewById(R.id.tweet_popup);
        View tweetButton = findViewById(R.id.tweet_send);
        View closeButton = findViewById(R.id.close);
        locationBtn = findViewById(R.id.tweet_add_location);
        mediaBtn = findViewById(R.id.tweet_add_media);
        previewBtn = findViewById(R.id.tweet_prev_media);
        tweetText = findViewById(R.id.tweet_input);
        locationProg = findViewById(R.id.location_progress);
        loadingCircle = new Dialog(this, R.style.LoadingDialog);
        View load = View.inflate(this, R.layout.item_load, null);
        View cancelButton = load.findViewById(R.id.kill_button);

        mLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GlobalSettings settings = GlobalSettings.getInstance(this);
        mediaPath = new LinkedList<>();

        Bundle param = getIntent().getExtras();
        if (param != null) {
            inReplyId = param.getLong(KEY_TWEETPOPUP_REPLYID, 0);
            prefix = param.getString(KEY_TWEETPOPUP_TEXT, "");
            tweetText.append(prefix);
        }

        errorDialog = DialogBuilder.create(this, TWEETPOPUP_ERROR, this);
        closingDialog = DialogBuilder.create(this, TWEETPOPUP_LEAVE, this);
        loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
        loadingCircle.setCanceledOnTouchOutside(false);
        loadingCircle.setContentView(load);
        FontTool.setViewFontAndColor(settings, root);
        root.setBackgroundColor(settings.getPopupColor());
        cancelButton.setVisibility(VISIBLE);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);
        mediaBtn.setOnClickListener(this);
        previewBtn.setOnClickListener(this);
        locationBtn.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        loadingCircle.setOnDismissListener(this);
    }


    @Override
    protected void onDestroy() {
        if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING)
            uploaderAsync.cancel(true);
        if (mLocation != null)
            mLocation.removeUpdates(this);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        showClosingMsg();
    }


    @Override
    protected void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
        super.onActivityResult(reqCode, returnCode, intent);
        if (reqCode == REQ_PICK_MEDIA && returnCode == RESULT_OK) {
            if (intent != null && intent.getData() != null) {
                Cursor cursor = getContentResolver().query(intent.getData(), GET_MEDIA, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(GET_MEDIA[0]);
                    String path = cursor.getString(index);
                    String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
                    switch (extension) {
                        case "jpg":
                        case "jpeg":
                        case "png":
                            if (selectedFormat == MediaType.NONE)
                                selectedFormat = MediaType.IMAGE;
                            if (selectedFormat == MediaType.IMAGE) {
                                if (mediaPath.size() < MAX_IMAGES) {
                                    mediaPath.add(path);
                                    previewBtn.setVisibility(VISIBLE);
                                    if (mediaPath.size() == MAX_IMAGES) {
                                        mediaBtn.setVisibility(GONE);
                                    }
                                }
                            } else {
                                Toast.makeText(this, R.string.info_cant_add_video, LENGTH_SHORT).show();
                            }
                            break;

                        case "gif":
                            if (selectedFormat == MediaType.NONE) {
                                selectedFormat = MediaType.GIF;
                                previewBtn.setImageResource(R.drawable.video);
                                previewBtn.setVisibility(VISIBLE);
                                mediaBtn.setVisibility(GONE);
                                mediaPath.add(path);
                            }
                            break;

                        case "mp4":
                        case "3gp":
                            if (selectedFormat == MediaType.NONE) {
                                selectedFormat = MediaType.VIDEO;
                                previewBtn.setImageResource(R.drawable.video);
                                previewBtn.setVisibility(VISIBLE);
                                mediaBtn.setVisibility(GONE);
                                mediaPath.add(path);
                            }
                            break;

                        default:
                            Toast.makeText(this, R.string.error_file_format, LENGTH_SHORT).show();
                            break;
                    }
                    cursor.close();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_CHECK_PERM && permissions.length > 0 && grantResults.length > 0) {
            switch (permissions[0]) {
                case READ_EXTERNAL_STORAGE:
                    if (grantResults[0] == PERMISSION_GRANTED)
                        getMedia();
                    break;

                case ACCESS_FINE_LOCATION:
                    if (grantResults[0] == PERMISSION_GRANTED)
                        getLocation();
                    break;
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tweet_send:
                String tweetStr = tweetText.getText().toString();
                if (tweetStr.trim().isEmpty() && mediaPath.isEmpty()) {
                    Toast.makeText(this, R.string.error_empty_tweet, LENGTH_SHORT).show();
                } else if (locationProg.getVisibility() == INVISIBLE) {
                    tweet = new TweetHolder(tweetStr, inReplyId);
                    if (selectedFormat == MediaType.IMAGE || selectedFormat == MediaType.GIF)
                        tweet.addMedia(mediaPath.toArray(new String[0]), TweetHolder.MediaType.IMAGE);
                    else if (selectedFormat == MediaType.VIDEO)
                        tweet.addMedia(mediaPath.toArray(new String[0]), TweetHolder.MediaType.VIDEO);
                    if (location != null)
                        tweet.addLocation(location);
                    uploaderAsync = new TweetUploader(this);
                    uploaderAsync.execute(tweet);
                }
                break;

            case R.id.close:
                showClosingMsg();
                break;

            case R.id.tweet_add_media:
                getMedia();
                break;

            case R.id.tweet_prev_media:
                Intent image = new Intent(this, MediaViewer.class);
                image.putExtra(KEY_MEDIA_LINK, mediaPath.toArray(new String[0]));

                switch (selectedFormat) {
                    case VIDEO:
                        image.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_VIDEO);
                        startActivity(image);
                        break;

                    case GIF:
                    case IMAGE:
                        image.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMG_S);
                        startActivity(image);
                        break;
                }
                break;

            case R.id.tweet_add_location:
                getLocation();
                break;

            case R.id.kill_button:
                loadingCircle.dismiss();
                break;
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
        Toast.makeText(this, R.string.info_gps_attached, LENGTH_LONG).show();
        locationProg.setVisibility(INVISIBLE);
        locationBtn.setVisibility(VISIBLE);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (location == null)
            Toast.makeText(this, R.string.error_gps, LENGTH_LONG).show();
        locationProg.setVisibility(INVISIBLE);
        locationBtn.setVisibility(VISIBLE);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING) {
            uploaderAsync.cancel(true);
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        if (type == TWEETPOPUP_ERROR) {
            uploaderAsync = new TweetUploader(this);
            uploaderAsync.execute(tweet);
        } else if (type == TWEETPOPUP_LEAVE) {
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
     * called after sending tweet
     */
    public void onSuccess() {
        Toast.makeText(this, R.string.info_tweet_sent, LENGTH_LONG).show();
        finish();
    }

    /**
     * Show confirmation dialog if an error occurs while sending tweet
     */
    public void onError(EngineException error) {
        ErrorHandler.handleFailure(this, error);
        if (!errorDialog.isShowing()) {
            errorDialog.show();
        }
    }


    /**
     * show confirmation dialog when closing edited tweet
     */
    private void showClosingMsg() {
        if (!prefix.equals(tweetText.getText().toString()) || !mediaPath.isEmpty()) {
            if (!closingDialog.isShowing()) {
                closingDialog.show();
            }
        } else {
            finish();
        }
    }


    /**
     * get images or video from storage to attach to tweet
     */
    private void getMedia() {
        boolean accessGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(PERM_STORAGE[0]) != PERMISSION_GRANTED) {
                requestPermissions(PERM_STORAGE, REQ_CHECK_PERM);
                accessGranted = false;
            }
        }
        if (accessGranted) {
            Intent mediaSelect = new Intent(ACTION_PICK);
            mediaSelect.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Add multiple mime types
                if (selectedFormat == MediaType.IMAGE) {
                    mediaSelect.setType(TYPE_IMAGE);
                } else {
                    // pick image or video
                    mediaSelect.setType("*/*");
                    String[] mimeTypes = new String[]{TYPE_IMAGE, TYPE_VIDEO};
                    mediaSelect.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                }
            } else {
                // TODO add a selector for the user to choose between image or video
                mediaSelect.setType(TYPE_IMAGE);
            }
            try {
                startActivityForResult(mediaSelect, REQ_PICK_MEDIA);
            } catch (ActivityNotFoundException err) {
                Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Get current GPS location to attach to tweet
     */
    private void getLocation() {
        boolean accessGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(PERM_LOCATION[0]) != PERMISSION_GRANTED) {
                requestPermissions(PERM_LOCATION, REQ_CHECK_PERM);
                accessGranted = false;
            }
        }
        if (accessGranted) {
            if (mLocation != null && mLocation.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocation.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                Toast.makeText(this, R.string.info_get_location, LENGTH_SHORT).show();
                locationProg.setVisibility(VISIBLE);
                locationBtn.setVisibility(INVISIBLE);
            } else {
                Toast.makeText(this, R.string.error_location, LENGTH_SHORT).show();
            }
        }
    }
}