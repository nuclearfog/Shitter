package org.nuclearfog.twidda.ui.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.holder.TweetUpdate;
import org.nuclearfog.twidda.backend.async.TweetUpdater;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;

/**
 * Tweet editor activity. Media files and location can be attached to a tweet.
 *
 * @author nuclearfog
 */
public class TweetEditor extends MediaActivity implements OnClickListener, OnProgressStopListener, OnConfirmListener {

    /**
     * key for the replied tweet if any
     */
    public static final String KEY_TWEETPOPUP_REPLYID = "tweet_replyID";

    /**
     * key for the text added to the tweet if any
     */
    public static final String KEY_TWEETPOPUP_TEXT = "tweet_text";

    private static final String MIME_GIF = "image/gif";
    private static final String MIME_IMAGE_ALL = "image/";
    private static final String MIME_VIDEO_ALL = "video/";

    /**
     * image limit of a tweet
     */
    private static final int MAX_IMAGES = 4;

    /**
     * video limit of a tweet
     */
    private static final int MAX_VIDEOS = 1;

    /**
     * gif limit of a tweet
     */
    private static final int MAX_GIF = 1;

    /**
     * mention limit of a tweet
     */
    private static final int MAX_MENTIONS = 10;

    private static final int MEDIA_NONE = 0;
    private static final int MEDIA_IMAGE = 1;
    private static final int MEDIA_VIDEO = 2;
    private static final int MEDIA_GIF = 3;

    private TweetUpdater uploaderAsync;
    private GlobalSettings settings;

    private ConfirmDialog confirmDialog;
    private ProgressDialog loadingCircle;

    private ImageButton mediaBtn, previewBtn, locationBtn;
    private EditText tweetText;
    private View locationPending;

    private TweetUpdate tweetUpdate = new TweetUpdate();
    private int selectedFormat = MEDIA_NONE;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppStyles.setFontScale(newBase));
    }


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.popup_tweet);
        ViewGroup root = findViewById(R.id.tweet_popup);
        ImageView background = findViewById(R.id.tweet_popup_background);
        ImageButton tweetButton = findViewById(R.id.tweet_send);
        ImageButton closeButton = findViewById(R.id.close);
        locationBtn = findViewById(R.id.tweet_add_location);
        mediaBtn = findViewById(R.id.tweet_add_media);
        previewBtn = findViewById(R.id.tweet_prev_media);
        tweetText = findViewById(R.id.tweet_input);
        locationPending = findViewById(R.id.location_progress);

        settings = GlobalSettings.getInstance(this);
        loadingCircle = new ProgressDialog(this);
        confirmDialog = new ConfirmDialog(this);

        Intent data = getIntent();
        long inReplyId = data.getLongExtra(KEY_TWEETPOPUP_REPLYID, 0);
        String prefix = data.getStringExtra(KEY_TWEETPOPUP_TEXT);

        tweetUpdate.setReplyId(inReplyId);
        if (prefix != null) {
            tweetText.append(prefix);
        }

        mediaBtn.setImageResource(R.drawable.attachment);
        locationBtn.setImageResource(R.drawable.location);
        tweetButton.setImageResource(R.drawable.tweet);
        closeButton.setImageResource(R.drawable.cross);
        AppStyles.setEditorTheme(root, background);

        closeButton.setOnClickListener(this);
        tweetButton.setOnClickListener(this);
        mediaBtn.setOnClickListener(this);
        previewBtn.setOnClickListener(this);
        locationBtn.setOnClickListener(this);
        confirmDialog.setConfirmListener(this);
        loadingCircle.addOnProgressStopListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isLocating()) {
            locationPending.setVisibility(VISIBLE);
            locationBtn.setVisibility(INVISIBLE);
        } else {
            locationPending.setVisibility(INVISIBLE);
            locationBtn.setVisibility(VISIBLE);
        }
    }


    @Override
    protected void onDestroy() {
        loadingCircle.dismiss();
        if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING)
            uploaderAsync.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        showClosingMsg();
    }


    @Override
    public void onClick(View v) {
        // send tweet
        if (v.getId() == R.id.tweet_send) {
            String tweetStr = tweetText.getText().toString();
            // check if tweet is empty
            if (tweetStr.trim().isEmpty() && tweetUpdate.mediaCount() == 0) {
                Toast.makeText(this, R.string.error_empty_tweet, LENGTH_SHORT).show();
            }
            // check if mentions exceed the limit
            else if (StringTools.countMentions(tweetStr) > MAX_MENTIONS) {
                Toast.makeText(this, R.string.error_mention_exceed, LENGTH_SHORT).show();
            }
            // check if GPS location is pending
            else if (isLocating()) {
                Toast.makeText(this, R.string.info_location_pending, LENGTH_SHORT).show();
            }
            // check if gps locating is not pending
            else if (uploaderAsync == null || uploaderAsync.getStatus() != RUNNING) {
                updateTweet();
            }
        }
        // close tweet editor
        else if (v.getId() == R.id.close) {
            showClosingMsg();
        }
        // Add media to the tweet
        else if (v.getId() == R.id.tweet_add_media) {
            if (selectedFormat == MEDIA_NONE) {
                // request images/videos
                getMedia(REQUEST_IMG_VID);
            } else {
                // request images only
                getMedia(REQUEST_IMAGE);
            }
        }
        // open media preview
        else if (v.getId() == R.id.tweet_prev_media) {
            Uri[] uris = tweetUpdate.getMediaUris();
            //
            if (selectedFormat == MEDIA_VIDEO) {
                Intent mediaViewer = new Intent(this, VideoViewer.class);
                mediaViewer.putExtra(VideoViewer.VIDEO_URI, uris[0]);
                mediaViewer.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
                startActivity(mediaViewer);
            }
            //
            else if (selectedFormat == MEDIA_IMAGE) {
                Intent mediaViewer = new Intent(this, ImageViewer.class);
                mediaViewer.putExtra(ImageViewer.IMAGE_URIS, uris);
                mediaViewer.putExtra(ImageViewer.IMAGE_DOWNLOAD, false);
                startActivity(mediaViewer);
            }
            //
            else if (selectedFormat == MEDIA_GIF) {
                // todo add support for local gif animation
                Intent mediaViewer = new Intent(this, ImageViewer.class);
                mediaViewer.putExtra(ImageViewer.IMAGE_URIS, uris);
                mediaViewer.putExtra(ImageViewer.IMAGE_DOWNLOAD, false);
                startActivity(mediaViewer);
            }
        }
        // add location to the tweet
        else if (v.getId() == R.id.tweet_add_location) {
            locationPending.setVisibility(VISIBLE);
            locationBtn.setVisibility(INVISIBLE);
            getLocation(true);
        }
    }


    @Override
    protected void onAttachLocation(@Nullable Location location) {
        if (location != null) {
            tweetUpdate.setLocation(location);
            Toast.makeText(this, R.string.info_gps_attached, LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.error_gps, LENGTH_LONG).show();
        }
        locationPending.setVisibility(INVISIBLE);
        locationBtn.setVisibility(VISIBLE);
    }


    @Override
    protected void onMediaFetched(int resultType, @NonNull Uri uri) {
        int mediaCount = 0;
        String mime = getContentResolver().getType(uri);
        if (mime == null) {
            Toast.makeText(this, R.string.error_file_format, LENGTH_SHORT).show();
        }
        // check if file is a 'gif' image
        else if (mime.equals(MIME_GIF)) {
            if (selectedFormat == MEDIA_NONE || selectedFormat == MEDIA_GIF) {
                mediaCount = addTweetMedia(uri, R.drawable.gif, MAX_GIF);
                if (mediaCount > 0) {
                    selectedFormat = MEDIA_GIF;
                }
            }
        }
        // check if file is an image
        else if (mime.startsWith(MIME_IMAGE_ALL)) {
            if (selectedFormat == MEDIA_NONE || selectedFormat == MEDIA_IMAGE) {
                mediaCount = addTweetMedia(uri, R.drawable.image, MAX_IMAGES);
                if (mediaCount > 0) {
                    selectedFormat = MEDIA_IMAGE;
                }
            }
        }
        // check if file is a video
        else if (mime.startsWith(MIME_VIDEO_ALL)) {
            if (selectedFormat == MEDIA_NONE || selectedFormat == MEDIA_VIDEO) {
                mediaCount = addTweetMedia(uri, R.drawable.video, MAX_VIDEOS);
                if (mediaCount > 0) {
                    selectedFormat = MEDIA_VIDEO;
                }
            }
        }
        // check if media was successfully added
        if (mediaCount <= 0) {
            Toast.makeText(this, R.string.error_adding_media, LENGTH_SHORT).show();
        }
    }


    @Override
    public void stopProgress() {
        if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING) {
            uploaderAsync.cancel(true);
        }
    }


    @Override
    public void onConfirm(DialogType type, boolean rememberChoice) {
        // retry uploading tweet
        if (type == DialogType.TWEET_EDITOR_ERROR) {
            updateTweet();
        }
        // leave editor
        else if (type == DialogType.TWEET_EDITOR_LEAVE) {
            finish();
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
    public void onError(@Nullable ErrorHandler.TwitterError error) {
        String message = ErrorHandler.getErrorMessage(this, error);
        confirmDialog.setMessage(message);
        confirmDialog.show(DialogType.TWEET_EDITOR_ERROR);
        loadingCircle.dismiss();
    }

    /**
     * show confirmation dialog when closing edited tweet
     */
    private void showClosingMsg() {
        if (tweetText.length() > 0 || tweetUpdate.mediaCount() > 0 || tweetUpdate.hasLocation()) {
            confirmDialog.show(DialogType.TWEET_EDITOR_LEAVE);
        } else {
            finish();
        }
    }

    /**
     * attach media to the tweet
     *
     * @param uri   Uri link of the media
     * @param icon  icon of the preview button
     * @param limit limit of the media count
     * @return media count or -1 if adding failed
     */
    private int addTweetMedia(Uri uri, @DrawableRes int icon, int limit) {
        previewBtn.setImageResource(icon);
        AppStyles.setDrawableColor(previewBtn, settings.getIconColor());
        int mediaCount = tweetUpdate.addMedia(this, uri);
        if (mediaCount > 0)
            previewBtn.setVisibility(VISIBLE);
        // if limit reached, remove mediaselect button
        if (mediaCount == limit) {
            mediaBtn.setVisibility(GONE);
        }
        return mediaCount;
    }

    /**
     * start uploading tweet and media files
     */
    private void updateTweet() {
        // first initialize filestreams of the media files
        if (tweetUpdate.prepare(getContentResolver())) {
            String tweetStr = tweetText.getText().toString();
            // add media
            tweetUpdate.setText(tweetStr);
            // send tweet
            uploaderAsync = new TweetUpdater(this);
            uploaderAsync.execute(tweetUpdate);
            // show progress dialog
            loadingCircle.show();
        } else {
            Toast.makeText(this, R.string.error_media_init, LENGTH_SHORT).show();
        }
    }
}