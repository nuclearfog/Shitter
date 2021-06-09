package org.nuclearfog.twidda.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.holder.ImageHolder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.zoomview.ZoomView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;
import static android.os.AsyncTask.Status.RUNNING;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;

/**
 * Media viewer activity for images and videos
 *
 * @author nuclearfog
 */
public class MediaViewer extends MediaActivity implements OnImageClickListener, OnSeekBarChangeListener, OnCompletionListener,
        OnPreparedListener, OnInfoListener, OnErrorListener, OnClickListener, OnTouchListener {

    /**
     * Key for the media URL, local or online, required
     */
    public static final String KEY_MEDIA_LINK = "media_link";

    /**
     * Key for the media type, required
     * {@link #MEDIAVIEWER_IMAGE}, {@link #MEDIAVIEWER_VIDEO} or {@link #MEDIAVIEWER_ANGIF}
     */
    public static final String KEY_MEDIA_TYPE = "media_type";
    /**
     * setup media viewer for images from twitter
     */
    public static final int MEDIAVIEWER_IMAGE = 0x997BCDCE;

    /**
     * setup media viewer for videos
     */
    public static final int MEDIAVIEWER_VIDEO = 0x500C9A42;

    /**
     * setup media viewer for GIF animation
     */
    public static final int MEDIAVIEWER_ANGIF = 0x6500EDB0;

    /**
     * refresh time for video progress update
     */
    private static final int PROGRESS_UPDATE = 1000;

    /**
     * speed factor for fast forward or backward
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

    private WeakReference<MediaViewer> updateEvent = new WeakReference<>(this);
    @Nullable
    private ScheduledExecutorService progressUpdate;
    @Nullable
    private ImageLoader imageAsync;

    private TextView duration, position;
    private ProgressBar loadingCircle;
    private SeekBar video_progress;
    private ImageButton play, pause;
    private ImageAdapter adapter;
    private VideoView videoView;
    private ZoomView zoomImage;
    private View controlPanel;

    private String[] mediaLinks;
    private int type;

    private PlayStat playStat = PlayStat.IDLE;


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_media);
        RecyclerView imageList = findViewById(R.id.image_list);
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
        AppStyles.setTheme(settings, controlPanel, settings.getCardColor());
        adapter = new ImageAdapter(settings, this);

        // get intent data and type
        mediaLinks = getIntent().getStringArrayExtra(KEY_MEDIA_LINK);
        type = getIntent().getIntExtra(KEY_MEDIA_TYPE, 0);

        if (mediaLinks != null && mediaLinks.length > 0) {
            switch (type) {
                case MEDIAVIEWER_IMAGE:
                    zoomImage.setVisibility(VISIBLE);
                    imageList.setVisibility(VISIBLE);
                    if (!mediaLinks[0].startsWith("http"))
                        adapter.disableSaveButton();
                    imageList.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
                    imageList.setAdapter(adapter);
                    imageAsync = new ImageLoader(this);
                    imageAsync.execute(mediaLinks);
                    break;

                case MEDIAVIEWER_VIDEO:
                    controlPanel.setVisibility(VISIBLE);
                    if (!mediaLinks[0].startsWith("http"))
                        share.setVisibility(GONE); // local image
                    final Runnable seekUpdate = new Runnable() {
                        public void run() {
                            if (updateEvent.get() != null) {
                                updateEvent.get().updateSeekBar();
                            }
                        }
                    };
                    progressUpdate = Executors.newScheduledThreadPool(1);
                    progressUpdate.scheduleWithFixedDelay(new Runnable() {
                        public void run() {
                            if (updateEvent.get() != null) {
                                updateEvent.get().runOnUiThread(seekUpdate);
                            }
                        }
                    }, PROGRESS_UPDATE, PROGRESS_UPDATE, TimeUnit.MILLISECONDS);
                case MEDIAVIEWER_ANGIF:
                    videoView.setVisibility(VISIBLE);
                    videoView.setZOrderMediaOverlay(true); // disable black background
                    videoView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                    videoView.setVideoURI(Uri.parse(mediaLinks[0]));
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
        if (progressUpdate != null)
            progressUpdate.shutdown();
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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mediaLinks[0]));
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
    protected void onMediaFetched(int resultType, @NonNull String path) {
    }


    @Override
    public void onImageClick(Bitmap image) {
        zoomImage.reset();
        zoomImage.setImageBitmap(image);
    }


    @Override
    public void onImageSave(Bitmap image, int pos) {
        if (mediaLinks != null && pos < mediaLinks.length && mediaLinks[pos] != null) {
            String link = mediaLinks[pos];
            String name = "shitter_" + link.substring(link.lastIndexOf('/') + 1);
            storeImage(image, name);
        }
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
    public void onError(EngineException err) {
        ErrorHandler.handleFailure(getApplicationContext(), err);
        finish();
    }

    /**
     * set downloaded image into preview list
     *
     * @param image Image container
     */
    public void setImage(ImageHolder image) {
        if (adapter.isEmpty()) {
            zoomImage.reset();
            zoomImage.setImageBitmap(image.reducedImage);
            loadingCircle.setVisibility(INVISIBLE);
        }
        adapter.addLast(image);
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
    private void updateSeekBar() {
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
}