package org.nuclearfog.twidda.activity;

import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.TweetUploader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.TweetHolder;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_PICK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.Window.FEATURE_NO_TITLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_ANGIF_STORAGE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_IMG_STORAGE;
import static org.nuclearfog.twidda.activity.MediaViewer.MEDIAVIEWER_VIDEO_STORAGE;


public class TweetPopup extends AppCompatActivity implements OnClickListener, LocationListener, OnDismissListener {

    public static final String KEY_TWEETPOPUP_REPLYID = "tweet_replyID";
    public static final String KEY_TWEETPOPUP_PREFIX = "tweet_prefix";

    private enum MediaType {
        NONE,
        GIF,
        IMAGE,
        VIDEO
    }

    private static final String[] PERM_STORAGE = {READ_EXTERNAL_STORAGE};
    private static final String[] PERM_LOCATION = {ACCESS_FINE_LOCATION};
    private static final String[] GET_MEDIA = {MediaStore.Images.Media.DATA};
    private static final String TYPE_IMAGE = "image/*";
    private static final String TYPE_VIDEO = "video/*";
    private static final int REQ_PICK_MEDIA = 3;
    private static final int REQ_CHECK_PERM = 4;
    private static final int MAX_IMAGES = 4;

    @Nullable
    private LocationManager mLocation;
    private TweetUploader uploaderAsync;
    private Location location;
    private List<String> mediaPath;
    private View mediaBtn, previewBtn, locationProg, locationBtn;
    private Dialog loadingCircle;
    private TextView imgCount;
    private EditText tweetText;

    private MediaType selectedFormat = MediaType.NONE;
    private long inReplyId = 0;

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
        imgCount = findViewById(R.id.imgcount);
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
            if (param.containsKey(KEY_TWEETPOPUP_PREFIX)) {
                String addition = param.getString(KEY_TWEETPOPUP_PREFIX) + " ";
                tweetText.append(addition);
            }
        }

        loadingCircle.requestWindowFeature(FEATURE_NO_TITLE);
        loadingCircle.setCanceledOnTouchOutside(false);
        loadingCircle.setContentView(load);
        FontTool.setViewFontAndColor(settings, root);
        root.setBackgroundColor(settings.getPopupColor());

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
                                    String count = Integer.toString(mediaPath.size());
                                    imgCount.setText(count);
                                    if (mediaPath.size() == MAX_IMAGES)
                                        mediaBtn.setVisibility(INVISIBLE);
                                }
                            } else {
                                Toast.makeText(this, R.string.info_cant_add_video, LENGTH_SHORT).show();
                            }
                            break;

                        case "gif":
                            if (selectedFormat == MediaType.NONE) {
                                selectedFormat = MediaType.GIF;
                                mediaPath.add(path);
                                previewBtn.setVisibility(VISIBLE);
                                mediaBtn.setVisibility(INVISIBLE);
                            }
                            break;

                        case "mp4":
                        case "3gp":
                            if (selectedFormat == MediaType.NONE) {
                                selectedFormat = MediaType.VIDEO;
                                mediaPath.add(path);
                                previewBtn.setVisibility(VISIBLE);
                                mediaBtn.setVisibility(INVISIBLE);
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
                    TweetHolder tweet = new TweetHolder(tweetStr, inReplyId);
                    if (selectedFormat == MediaType.IMAGE || selectedFormat == MediaType.GIF)
                        tweet.addMedia(mediaPath.toArray(new String[0]), TweetHolder.MediaType.IMAGE);
                    else if (selectedFormat == MediaType.VIDEO)
                        tweet.addMedia(mediaPath.toArray(new String[0]), TweetHolder.MediaType.VIDEO);
                    if (location != null)
                        tweet.addLocation(location);
                    uploaderAsync = new TweetUploader(this, tweet);
                    uploaderAsync.execute();
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
                    case IMAGE:
                        image.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMG_STORAGE);
                        startActivity(image);
                        break;

                    case VIDEO:
                        image.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_VIDEO_STORAGE);
                        startActivity(image);
                        break;

                    case GIF:
                        image.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_ANGIF_STORAGE);
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
    public void onLocationChanged(Location location) {
        this.location = location;
        Toast.makeText(this, R.string.info_gps_attached, LENGTH_LONG).show();
        locationProg.setVisibility(INVISIBLE);
        locationBtn.setVisibility(VISIBLE);
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (location == null)
            Toast.makeText(this, R.string.error_gps, LENGTH_LONG).show();
        locationProg.setVisibility(INVISIBLE);
        locationBtn.setVisibility(VISIBLE);
    }

    @Override
    public void onProviderEnabled(String provider) {
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
     * @param tweet tweet to re-send
     */
    public void onError(final TweetHolder tweet, EngineException error) {
        ErrorHandler.handleFailure(this, error);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ConfirmDialog);
        builder.setTitle(R.string.info_error).setMessage(R.string.error_sending_tweet)
                .setPositiveButton(R.string.confirm_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploaderAsync = new TweetUploader(TweetPopup.this, tweet);
                        uploaderAsync.execute();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }


    /**
     * show confirmation dialog when closing edited tweet
     */
    private void showClosingMsg() {
        if (tweetText.getText().length() > 0 || !mediaPath.isEmpty()) {
            Builder closeDialog = new Builder(this, R.style.ConfirmDialog);
            closeDialog.setMessage(R.string.confirm_cancel_tweet);
            closeDialog.setNegativeButton(R.string.confirm_no, null);
            closeDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            closeDialog.show();
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
            if (selectedFormat == MediaType.IMAGE)
                mediaSelect.setDataAndType(EXTERNAL_CONTENT_URI, TYPE_IMAGE);
            else
                mediaSelect.setDataAndType(EXTERNAL_CONTENT_URI, TYPE_IMAGE + TYPE_VIDEO);
            if (mediaSelect.resolveActivity(getPackageManager()) != null)
                startActivityForResult(mediaSelect, REQ_PICK_MEDIA);
            else
                Toast.makeText(getApplicationContext(), R.string.error_no_media_app, LENGTH_SHORT).show();
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