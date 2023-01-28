package org.nuclearfog.twidda.ui.activities;

import static android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;
import static android.media.MediaPlayer.OnCompletionListener;
import static android.media.MediaPlayer.OnErrorListener;
import static android.media.MediaPlayer.OnInfoListener;
import static android.media.MediaPlayer.OnPreparedListener;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.SeekbarUpdater;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

/**
 * video player activity to show local and online videos/animations
 *
 * @author nuclearfog
 */
public class VideoViewer extends MediaActivity implements OnSeekBarChangeListener, OnCompletionListener, OnDismissListener,
		OnPreparedListener, OnInfoListener, OnErrorListener, OnClickListener, OnTouchListener, OnConfirmListener {

	/**
	 * key for an Uri array with local links
	 * value type is {@link Uri}
	 */
	public static final String VIDEO_URI = "media_uri";

	/**
	 * Key to enable extra layouts for a video
	 * value type is Boolean
	 */
	public static final String ENABLE_VIDEO_CONTROLS = "enable_controls";

	/**
	 * playback status used if player is idle
	 */
	private static final int IDLE = -1;

	/**
	 * playback status used for "play" mode
	 */
	private static final int PLAY = 1;

	/**
	 * playback status used for "pause" mode
	 */
	private static final int PAUSE = 2;

	/**
	 * playback status used for "forward" mode
	 */
	private static final int FORWARD = 3;

	/**
	 * playback status used for "backward" mode
	 */
	private static final int BACKWARD = 4;

	/**
	 * refresh time for video progress update in milliseconds
	 */
	private static final int PROGRESS_UPDATE = 1000;

	/**
	 * speed ratio for "backward" and "forward"
	 */
	private static final int SPEED_FACTOR = 6;

	@Nullable
	private SeekbarUpdater seekUpdate;
	private GlobalSettings settings;

	private ConfirmDialog confirmDialog;

	private TextView duration, position;
	private ProgressBar loadingCircle;
	private SeekBar video_progress;
	private ImageButton playPause;
	private VideoView videoView;
	private ViewGroup controlPanel;

	private Uri link;
	private boolean enableVideoExtras;
	private int playStatus = IDLE;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_video);
		ImageButton forward = findViewById(R.id.controller_forward);
		ImageButton backward = findViewById(R.id.controller_backward);
		ImageButton share = findViewById(R.id.controller_share);
		loadingCircle = findViewById(R.id.media_progress);
		controlPanel = findViewById(R.id.media_controlpanel);
		videoView = findViewById(R.id.video_view);
		video_progress = findViewById(R.id.controller_progress);
		playPause = findViewById(R.id.controller_play);
		duration = findViewById(R.id.controller_duration);
		position = findViewById(R.id.controller_position);

		confirmDialog = new ConfirmDialog(this);

		settings = GlobalSettings.getInstance(this);
		AppStyles.setProgressColor(loadingCircle, settings.getHighlightColor());
		AppStyles.setTheme(controlPanel);
		videoView.setZOrderMediaOverlay(true); // disable black background
		videoView.getHolder().setFormat(PixelFormat.TRANSPARENT);

		// get extras
		enableVideoExtras = getIntent().getBooleanExtra(ENABLE_VIDEO_CONTROLS, false);
		Parcelable data = getIntent().getParcelableExtra(VIDEO_URI);

		if (data instanceof Uri) {
			link = (Uri) data;
			// enable control bar if set
			if (enableVideoExtras) {
				controlPanel.setVisibility(VISIBLE);
				if (link.getScheme().startsWith("http")) {
					// attach link to share button
					share.setTag(link);
				} else {
					share.setVisibility(GONE);
				}
				seekUpdate = new SeekbarUpdater(this, PROGRESS_UPDATE);
			}
			videoView.setVideoURI(link);
		}
		share.setOnClickListener(this);
		playPause.setOnClickListener(this);
		videoView.setOnTouchListener(this);
		backward.setOnTouchListener(this);
		forward.setOnTouchListener(this);
		videoView.setOnPreparedListener(this);
		videoView.setOnCompletionListener(this);
		videoView.setOnErrorListener(this);
		video_progress.setOnSeekBarChangeListener(this);
		confirmDialog.setConfirmListener(this);
		confirmDialog.setOnDismissListener(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		if (enableVideoExtras) {
			videoView.pause();
			setPlayPause(R.drawable.play);
			playStatus = PAUSE;
		}
	}


	@Override
	protected void onDestroy() {
		if (seekUpdate != null)
			seekUpdate.shutdown();
		super.onDestroy();
	}


	@Override
	public void onClick(View v) {
		// play/pause video
		if (v.getId() == R.id.controller_play) {
			if (playStatus == PAUSE) {
				playStatus = PLAY;
				if (!videoView.isPlaying())
					videoView.start();
				setPlayPause(R.drawable.pause);
			} else if (playStatus == PLAY) {
				playStatus = PAUSE;
				if (videoView.isPlaying())
					videoView.pause();
				setPlayPause(R.drawable.play);
			}
		}
		// open link with another app
		else if (v.getId() == R.id.controller_share) {
			if (v.getTag() instanceof Uri) {
				Intent intent = new Intent(Intent.ACTION_VIEW, (Uri) v.getTag());
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException err) {
					Toast.makeText(getApplicationContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
				}
			}
		}
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// fast backward
		if (v.getId() == R.id.controller_backward) {
			if (playStatus == PAUSE)
				return false;
			if (event.getAction() == ACTION_DOWN) {
				playStatus = BACKWARD;
				videoView.pause();
				return true;
			}
			if (event.getAction() == ACTION_UP) {
				playStatus = PLAY;
				videoView.start();
				return true;
			}
		}
		// fast forward
		else if (v.getId() == R.id.controller_forward) {
			if (playStatus == PAUSE)
				return false;
			if (event.getAction() == ACTION_DOWN) {
				playStatus = FORWARD;
				videoView.pause();
				return true;
			}
			if (event.getAction() == ACTION_UP) {
				playStatus = PLAY;
				videoView.start();
				return true;
			}
		}
		// show/hide control panel
		else if (v.getId() == R.id.video_view) {
			if (event.getAction() == ACTION_DOWN) {
				if (enableVideoExtras) {
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
	public void onPrepared(MediaPlayer mp) {
		// enable controls for video
		if (enableVideoExtras) {
			switch (playStatus) {
				case IDLE:
					// initialize seekbar
					playStatus = PLAY;
					video_progress.setMax(mp.getDuration());
					duration.setText(StringTools.formatMediaTime(mp.getDuration()));
					mp.setOnInfoListener(this);
					// fall through

				case PLAY:
					// set video pos and start playback
					mp.seekTo(video_progress.getProgress());
					mp.start();
					break;

				case PAUSE:
					// set only video pos
					mp.seekTo(video_progress.getProgress());
					break;
			}
		}
		// setup video looping for gif
		else {
			loadingCircle.setVisibility(INVISIBLE);
			mp.setLooping(true);
			mp.start();
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
			if (link.getScheme().startsWith("http")) {
				confirmDialog.show(ConfirmDialog.VIDEO_ERROR);
			} else {
				Toast.makeText(getApplicationContext(), R.string.error_cant_load_video, LENGTH_SHORT).show();
				finish();
			}
			return true;
		}
		return false;
	}


	@Override
	public void onCompletion(MediaPlayer mp) {
		setPlayPause(R.drawable.play);
		video_progress.setProgress(0);
		playStatus = PAUSE;
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
		if (playStatus == PLAY) {
			videoView.start();
		}
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
		if (type == ConfirmDialog.VIDEO_ERROR) {
			if (link != null) {
				// open link in a browser
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(link);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException err) {
					Toast.makeText(getApplicationContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
				}
			}
		}
	}


	@Override
	public void onDismiss(DialogInterface dialog) {
		finish();
	}

	/**
	 * updates controller panel SeekBar
	 */
	public void updateSeekBar() {
		int videoPos = video_progress.getProgress();
		switch (playStatus) {
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


	private void setPlayPause(@DrawableRes int icon) {
		playPause.setImageResource(icon);
		AppStyles.setDrawableColor(playPause.getDrawable(), settings.getIconColor());
		AppStyles.setButtonColor(playPause, settings.getFontColor());
	}
}