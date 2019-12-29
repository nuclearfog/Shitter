package org.nuclearfog.twidda.activity;

import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.StatusUploader;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.helper.StringTools;
import org.nuclearfog.twidda.backend.helper.StringTools.FileType;
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
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_LINK;
import static org.nuclearfog.twidda.activity.MediaViewer.KEY_MEDIA_TYPE;
import static org.nuclearfog.twidda.activity.MediaViewer.MediaType.ANGIF_STORAGE;
import static org.nuclearfog.twidda.activity.MediaViewer.MediaType.IMAGE_STORAGE;
import static org.nuclearfog.twidda.activity.MediaViewer.MediaType.VIDEO_STORAGE;


public class TweetPopup extends AppCompatActivity implements OnClickListener, LocationListener {

    public static final String KEY_TWEETPOPUP_REPLYID = "tweet_replyID";
    public static final String KEY_TWEETPOPUP_PREFIX = "tweet_prefix";

    private static final String[] PERM_STORAGE = {READ_EXTERNAL_STORAGE};
    private static final String[] PERM_LOCATION = {ACCESS_FINE_LOCATION};
    private static final String[] GET_MEDIA = {MediaStore.Images.Media.DATA};
    private static final String TYPE_IMAGE = "image/*";
    private static final String TYPE_VIDEO = "video/*";
    private static final int PICK_MEDIA = 3;
    private static final int CHECK_PERM = 4;
    private static final int MAX_IMAGES = 4;

    @Nullable
    private LocationManager mLocation;
    private StatusUploader uploaderAsync;
    private Location location;
    private List<String> mediaPath;
    private View mediaBtn, previewBtn, locationProg, locationBtn;
    private TextView imgCount;
    private EditText tweet;
    private String addition = "";
    private long inReplyId = 0;
    private Mode mode = Mode.NONE;

    private enum Mode {
        IMAGE,
        VIDEO,
        GIF,
        NONE
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_tweet);
        View root = findViewById(R.id.tweet_popup);
        View tweetButton = findViewById(R.id.tweet_send);
        View closeButton = findViewById(R.id.close);
        locationBtn = findViewById(R.id.tweet_add_location);
        mediaBtn = findViewById(R.id.tweet_add_media);
        previewBtn = findViewById(R.id.tweet_prev_media);
        tweet = findViewById(R.id.tweet_input);
        imgCount = findViewById(R.id.imgcount);
        locationProg = findViewById(R.id.location_progress);
        mLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            inReplyId = param.getLong(KEY_TWEETPOPUP_REPLYID, 0);
            addition = param.getString(KEY_TWEETPOPUP_PREFIX, "") + " ";
        }

        mediaPath = new LinkedList<>();
        GlobalSettings settings = GlobalSettings.getInstance(this);
        FontTool.setViewFont(root, settings.getFontFace());
        root.setBackgroundColor(settings.getPopupColor());
        tweet.append(addition);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);
        mediaBtn.setOnClickListener(this);
        previewBtn.setOnClickListener(this);
        locationBtn.setOnClickListener(this);
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
    protected void onActivityResult(int reqCode, int returnCode, Intent intent) {
        super.onActivityResult(reqCode, returnCode, intent);
        if (reqCode == PICK_MEDIA && returnCode == RESULT_OK) {
            if (intent != null && intent.getData() != null) {
                Cursor cursor = getContentResolver().query(intent.getData(), GET_MEDIA, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(GET_MEDIA[0]);
                    String path = cursor.getString(index);
                    FileType type = StringTools.getFileType(path);

                    switch (type) {
                        case IMAGE:
                            if (mode == Mode.NONE)
                                mode = Mode.IMAGE;
                            if (mediaPath.size() < MAX_IMAGES && mode == Mode.IMAGE) {
                                mediaPath.add(path);
                                previewBtn.setVisibility(VISIBLE);
                                String count = Integer.toString(mediaPath.size());
                                imgCount.setText(count);
                                if (mediaPath.size() == MAX_IMAGES)
                                    mediaBtn.setVisibility(INVISIBLE);
                            }
                            break;

                        case ANGIF:
                            if (mode == Mode.NONE)
                                mode = Mode.GIF;
                            if (mode == Mode.GIF) {
                                mediaPath.add(path);
                                previewBtn.setVisibility(VISIBLE);
                                mediaBtn.setVisibility(INVISIBLE);
                            }
                            break;

                        case VIDEO:
                            if (mode == Mode.NONE)
                                mode = Mode.VIDEO;
                            if (mode == Mode.VIDEO) {
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
        if (requestCode == CHECK_PERM && permissions.length > 0 && grantResults.length > 0) {
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
                String tweetStr = tweet.getText().toString();
                if (tweetStr.trim().isEmpty() && mediaPath.isEmpty()) {
                    Toast.makeText(this, R.string.empty_tweet, LENGTH_SHORT).show();
                } else if (locationProg.getVisibility() == INVISIBLE) {
                    TweetHolder tweet = new TweetHolder(tweetStr, inReplyId);
                    if (!mediaPath.isEmpty())
                        tweet.addMedia(mediaPath.toArray(new String[0]));
                    if (location != null)
                        tweet.addLocation(location);
                    uploaderAsync = new StatusUploader(this, tweet);
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

                switch (mode) {
                    case IMAGE:
                        image.putExtra(KEY_MEDIA_TYPE, IMAGE_STORAGE);
                        startActivity(image);
                        break;

                    case VIDEO:
                        image.putExtra(KEY_MEDIA_TYPE, VIDEO_STORAGE);
                        startActivity(image);
                        break;

                    case GIF:
                        image.putExtra(KEY_MEDIA_TYPE, ANGIF_STORAGE);
                        startActivity(image);
                        break;
                }
                break;

            case R.id.tweet_add_location:
                getLocation();
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
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onProviderDisabled(String provider) {
        if (location == null)
            Toast.makeText(this, R.string.error_gps, LENGTH_LONG).show();
        locationProg.setVisibility(INVISIBLE);
        locationBtn.setVisibility(VISIBLE);
    }


    /**
     * show confirmation dialog when closing edited tweet
     */
    private void showClosingMsg() {
        if (!addition.equals(tweet.getText().toString()) || !mediaPath.isEmpty()) {
            Builder closeDialog = new Builder(this);
            closeDialog.setMessage(R.string.confirm_cancel_tweet);
            closeDialog.setNegativeButton(R.string.no_confirm, null);
            closeDialog.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
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
                requestPermissions(PERM_STORAGE, CHECK_PERM);
                accessGranted = false;
            }
        }
        if (accessGranted) {
            if (mode == Mode.NONE) {
                Intent mediaIntent = new Intent(ACTION_PICK);
                mediaIntent.setDataAndType(EXTERNAL_CONTENT_URI, TYPE_IMAGE + TYPE_VIDEO);
                startActivityForResult(mediaIntent, PICK_MEDIA);
            } else if (mode == Mode.IMAGE) {
                Intent imageIntent = new Intent(ACTION_PICK);
                imageIntent.setDataAndType(EXTERNAL_CONTENT_URI, TYPE_IMAGE);
                startActivityForResult(imageIntent, PICK_MEDIA);
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
                requestPermissions(PERM_LOCATION, CHECK_PERM);
                accessGranted = false;
            }
        }
        if (accessGranted) {
            if (mLocation != null && mLocation.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, R.string.info_get_location, LENGTH_SHORT).show();
                mLocation.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                locationProg.setVisibility(VISIBLE);
                locationBtn.setVisibility(INVISIBLE);
            } else {
                Toast.makeText(this, R.string.error_location, LENGTH_SHORT).show();
            }
        }
    }
}