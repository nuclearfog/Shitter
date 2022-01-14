package org.nuclearfog.twidda.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ImageAdapter;
import org.nuclearfog.twidda.adapter.ImageAdapter.OnImageClickListener;
import org.nuclearfog.twidda.backend.ImageLoader;
import org.nuclearfog.twidda.backend.SeekbarUpdater;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.zoomview.ZoomView;

import static android.media.MediaPlayer.*;
import static android.os.AsyncTask.Status.*;
import static android.view.MotionEvent.*;
import static android.view.View.*;
import static android.widget.Toast.*;
import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;

import java.io.File;

/**
 * Media viewer activity for images and videos
 *
 * @author nuclearfog
 */
public class MediaViewer extends MediaActivity implements OnImageClickListener, OnSeekBarChangeListener, OnCompletionListener,
        OnPreparedListener, OnInfoListener, OnErrorListener, OnClickListener, OnTouchListener {

    /**
     * key for an Uri array with local links
     */
    public static final String KEY_MEDIA_URI = "media_uri";

    /**
     * Key for the media type, required
     * {@link #MEDIAVIEWER_IMAGE}, {@link #MEDIAVIEWER_VIDEO} or {@link #MEDIAVIEWER_ANGIF}
     */
    public static final String KEY_MEDIA_TYPE = "media_type";

    /**
     * cache folder name
     */
    public static final String CACHE_FOLDER = "imagecache";

    /**
     * value for {@link #KEY_MEDIA_TYPE} to show images
     */
    public static final int MEDIAVIEWER_IMAGE = 0x997BCDCE;

    /**
     * value for {@link #KEY_MEDIA_TYPE} to show a video
     */
    public static final int MEDIAVIEWER_VIDEO = 0x500C9A42;

    /**
     * value for {@link #KEY_MEDIA_TYPE} to show an animated image
     */
    public static final int MEDIAVIEWER_ANGIF = 0x6500EDB0;

    /**
     * refresh time for video progress updatein milliseconds
     */
    private static final int PROGRESS_UPDATE = 1000;

    /**
     * speed factor for fast forward or fast backward
     */
    private static final int SPEED_FACTOR = 6;

    /**
     * video play status
     */
    private enum PlayStat {
        PLAY,
        PAUSE,
        FORWARD,
        BACKWARD,
        IDLE
    }

    @Nullable
    private ImageLoader imageAsync;
    private SeekbarUpdater seekUpdate;

    private TextView duration, position;
    private ProgressBar loadingCircle;
    private SeekBar video_progress;
    private ImageButton play, pause;
    private ImageAdapter adapter;
    private VideoView videoView;
    private ZoomView zoomImage;
    private ViewGroup controlPanel;

    private Uri[] mediaLinks;
    private int type;

    private PlayStat playStat = PlayStat.IDLE;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppStyles.setFontScale(newBase));
    }


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_media);
        RecyclerView imageList = findViewById(R.id.image_list);
        View imageListContainer = findViewById(R.id.image_preview_list);
        controlPanel = findViewById(R.id.media_controlpanel);
        loadingCircle = findViewById(R.id.media_progress);
        zoomImage = findViewById(R.id.image_full);
        videoView = findViewById(R.id.video_view);
        video_progress = controlPanel.findViewById(R.id.controller_progress);
        play = controlPanel.findViewById(R.id.controller_play);
        pause = controlPanel.findViewById(R.id.controller_pause);
        duration = controlPanel.findViewById(R.id.controller_duration);
        position = controlPanel.findViewById(R.id.controller_position);
        ImageButton forward = controlPanel.findViewById(R.id.controller_forward);
        ImageButton backward = controlPanel.findViewById(R.id.controller_backward);
        ImageButton share = controlPanel.findViewById(R.id.controller_share);

        share.setImageResource(R.drawable.share);
        forward.setImageResource(R.drawable.forward);
        backward.setImageResource(R.drawable.backward);
        play.setImageResource(R.drawable.play);
        pause.setImageResource(R.drawable.pause);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        AppStyles.setProgressColor(loadingCircle, settings.getHighlightColor());
        AppStyles.setTheme(controlPanel, settings.getBackgroundColor());
        adapter = new ImageAdapter(getApplicationContext(), this);

        // get extras
        type = getIntent().getIntExtra(KEY_MEDIA_TYPE, 0);
        Parcelable[] links =  getIntent().getParcelableArrayExtra(KEY_MEDIA_URI);

        // init media view
        if (links != null) {
            mediaLinks = new Uri[links.length];
            for (int i = 0; i < mediaLinks.length ; i++) {
                mediaLinks[i] = (Uri) links[i];
            }
            switch (type) {
                case MEDIAVIEWER_IMAGE:
                    zoomImage.setVisibility(VISIBLE);
                    imageListContainer.setVisibility(VISIBLE);
                    if (!mediaLinks[0].getScheme().startsWith("http")) {
                        adapter.disableSaveButton();
                        for (Uri uri : mediaLinks)
                            setImage(uri);
                        adapter.disableLoading();
                    } else {
                        imageAsync = new ImageLoader(this);
                        imageAsync.execute(mediaLinks);
                    }
                    imageList.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
                    imageList.setAdapter(adapter);
                    break;

                case MEDIAVIEWER_VIDEO:
                    controlPanel.setVisibility(VISIBLE);
                    if (!mediaLinks[0].getScheme().startsWith("http"))
                        share.setVisibility(GONE); // local image
                    seekUpdate = new SeekbarUpdater(this, PROGRESS_UPDATE);
                    // fall through
                case MEDIAVIEWER_ANGIF:
                    videoView.setVisibility(VISIBLE);
                    videoView.setZOrderMediaOverlay(true); // disable black background
                    videoView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                    videoView.setVideoURI(mediaLinks[0]);
                    break;
            }
        }

        share.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        videoView.setOnTouchListener(this);
        backward.setOnTouchListener(this);
        forward.setOnTouchListener(this);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setOnErrorListener(this);
        video_progress.setOnSeekBarChangeListener(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (type == MEDIAVIEWER_VIDEO) {
            playStat = PlayStat.PAUSE;
            setPlayPauseButton();
            videoView.pause();
        }
    }


    @Override
    protected void onDestroy() {
        if (imageAsync != null && imageAsync.getStatus() == RUNNING)
            imageAsync.cancel(true);
        if (seekUpdate != null)
            seekUpdate.shutdown();
        clearCache();
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        // play video
        if (v.getId() == R.id.controller_play) {
            if (!videoView.isPlaying())
                videoView.start();
            playStat = PlayStat.PLAY;
            setPlayPauseButton();
        }
        // pause video
        if (v.getId() == R.id.controller_pause) {
            if (videoView.isPlaying())
                videoView.pause();
            playStat = PlayStat.PAUSE;
            setPlayPauseButton();
        }
        // open link with another app
        else if (v.getId() == R.id.controller_share) {
            if (mediaLinks != null && mediaLinks.length > 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW, mediaLinks[0]);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException err) {
                    Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // fast backward
        if (v.getId() == R.id.controller_backward) {
            if (playStat == PlayStat.PAUSE)
                return false;
            if (event.getAction() == ACTION_DOWN) {
                playStat = PlayStat.BACKWARD;
                videoView.pause();
                return true;
            }
            if (event.getAction() == ACTION_UP) {
                playStat = PlayStat.PLAY;
                videoView.start();
                return true;
            }
        }
        // fast forward
        else if (v.getId() == R.id.controller_forward) {
            if (playStat == PlayStat.PAUSE)
                return false;
            if (event.getAction() == ACTION_DOWN) {
                playStat = PlayStat.FORWARD;
                videoView.pause();
                return true;
            }
            if (event.getAction() == ACTION_UP) {
                playStat = PlayStat.PLAY;
                videoView.start();
                return true;
            }
        }
        // show/hide control panel
        else if (v.getId() == R.id.video_view) {
            if (event.getAction() == ACTION_DOWN) {
                if (type == MEDIAVIEWER_VIDEO) {
                    if (controlPanel.getVisibility() == VISIBLE) {
                        controlPanel.setVisibility(INVISIBLE);
                    } else {
                        controlPanel.setVisibility(VISIBLE);
                    }
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    protected void onAttachLocation(@Nullable Location location) {
    }


    @Override
    protected void onMediaFetched(int resultType, @NonNull Uri uri) {
    }


    @Override
    public void onImageClick(Uri uri) {
        zoomImage.reset();
        zoomImage.setImageURI(uri);
    }


    @Override
    public void onImageSave(Uri uri) {
        storeImage(uri);
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        // configure to play GIF
        if (type == MEDIAVIEWER_ANGIF) {
            loadingCircle.setVisibility(INVISIBLE);
            mp.setLooping(true);
            mp.start();
        }
        // configure to play video
        else if (type == MEDIAVIEWER_VIDEO) {
            if (playStat == PlayStat.IDLE) {
                playStat = PlayStat.PLAY;
                video_progress.setMax(mp.getDuration());
                duration.setText(StringTools.formatMediaTime(mp.getDuration()));
                mp.setOnInfoListener(this);
            }
            if (playStat == PlayStat.PLAY) {
                int videoPos = video_progress.getProgress();
                mp.seekTo(videoPos);
                mp.start();
            }
        }
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MEDIA_INFO_BUFFERING_END:
            case MEDIA_INFO_VIDEO_RENDERING_START:
                loadingCircle.setVisibility(INVISIBLE);
                return true;

            case MEDIA_INFO_BUFFERING_START:
                loadingCircle.setVisibility(VISIBLE);
                return true;
        }
        return false;
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (what == MEDIA_ERROR_UNKNOWN) {
            Toast.makeText(this, R.string.error_cant_load_video, Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
        return false;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        playStat = PlayStat.PAUSE;
        setPlayPauseButton();
        video_progress.setProgress(0);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        videoView.pause();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        position.setText(StringTools.formatMediaTime(progress));
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        videoView.seekTo(seekBar.getProgress());
        videoView.start();
    }

    /**
     * Called from {@link ImageLoader} when all images are downloaded successfully
     */
    public void onSuccess() {
        adapter.disableLoading();
    }

    /**
     * Called from {@link ImageLoader} when an error occurs
     *
     * @param err Exception caught by {@link ImageLoader}
     */
    public void onError(ErrorHandler.TwitterError err) {
        ErrorHandler.handleFailure(getApplicationContext(), err);
        finish();
    }

    /**
     * set downloaded image into preview list
     *
     * @param imageUri Image Uri
     */
    public void setImage(Uri imageUri) {
        if (adapter.isEmpty()) {
            zoomImage.reset();
            zoomImage.setImageURI(imageUri);
            loadingCircle.setVisibility(INVISIBLE);
        }
        adapter.addLast(imageUri);
    }

    /**
     * set play pause button
     */
    private void setPlayPauseButton() {
        if (playStat == PlayStat.PAUSE || playStat == PlayStat.IDLE) {
            play.setVisibility(VISIBLE);
            pause.setVisibility(INVISIBLE);
        } else {
            play.setVisibility(INVISIBLE);
            pause.setVisibility(VISIBLE);
        }
    }

    /**
     * updates controller panel SeekBar
     */
    public void updateSeekBar() {
        int videoPos = video_progress.getProgress();
        switch (playStat) {
            case PLAY:
                video_progress.setProgress(videoView.getCurrentPosition());
                break;

            case FORWARD:
                videoPos += 2 * PROGRESS_UPDATE * SPEED_FACTOR;
                if (videoPos > videoView.getDuration())
                    videoPos = videoView.getDuration();
                videoView.seekTo(videoPos);
                video_progress.setProgress(videoPos);
                break;

            case BACKWARD:
                videoPos -= 2 * PROGRESS_UPDATE * SPEED_FACTOR;
                if (videoPos < 0)
                    videoPos = 0;
                videoView.seekTo(videoPos);
                video_progress.setProgress(videoPos);
                break;
        }
    }

    /**
     * clear the image cache
     */
    private void clearCache() {
        File cacheFolder = new File(getExternalCacheDir(), CACHE_FOLDER);
        File[] files = cacheFolder.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}