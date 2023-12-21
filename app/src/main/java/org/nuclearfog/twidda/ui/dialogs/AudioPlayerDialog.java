package org.nuclearfog.twidda.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.datasource.ContentDataSource;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.metadata.MetadataOutput;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.text.TextOutput;
import androidx.media3.exoplayer.video.VideoRendererEventListener;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.ui.PlayerControlView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.backend.utils.LinkUtils;

import java.io.Closeable;

/**
 * Dialog with audio player and controls
 *
 * @author nuclearfog
 */
@SuppressLint("UnsafeOptInUsageError")
public class AudioPlayerDialog extends Dialog implements OnClickListener, RenderersFactory, Closeable {

	private PlayerControlView controls;
	private TextView mediaLink;

	private ExoPlayer player;

	@Nullable
	private Uri data;

	/**
	 *
	 */
	public AudioPlayerDialog(Activity activity) {
		super(activity, R.style.AudioDialog);
		player = new ExoPlayer.Builder(activity.getApplicationContext(), this).build();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_audio_player);
		mediaLink = findViewById(R.id.dialog_audio_player_share);
		controls = findViewById(R.id.dialog_audio_player_controls);

		controls.setPlayer(player);
		mediaLink.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		// prevent re-initializing after resuming
		if (data != null) {
			DataSource.Factory dataSourceFactory;
			MediaItem mediaItem = MediaItem.fromUri(data);
			// initialize online source
			if (data.getScheme().startsWith("http")) {
				// configure with okhttp connection of the app
				dataSourceFactory = new OkHttpDataSource.Factory(ConnectionBuilder.create(getContext()));
				mediaLink.setVisibility(View.VISIBLE);
			}
			// initialize local source
			else {
				mediaLink.setVisibility(View.GONE);
				dataSourceFactory = new DataSource.Factory() {
					@NonNull
					@Override
					public DataSource createDataSource() {
						return new ContentDataSource(getContext());
					}
				};
			}
			MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem);
			player.setMediaSource(mediaSource);
			player.prepare();
			player.setPlayWhenReady(true);
			// reset data source
			data = null;
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
		if (player.isPlaying()) {
			player.pause();
		}
	}


	@Override
	public void show() {
		// use show(Uri) instead
	}


	@Override
	public void dismiss() {
		if (player.isPlaying()) {
			player.stop();
		}
		if (isShowing()) {
			super.dismiss();
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_audio_player_share) {
			LinkUtils.openMediaLink(getContext(), data);
		}
	}

	@Override
	public void close() {
		// remove player to prevent memory leak
		if (controls != null) {
			player.release();
			controls.setPlayer(null);
		}
	}


	@NonNull
	@Override
	public Renderer[] createRenderers(@NonNull Handler eventHandler, @NonNull VideoRendererEventListener videoRendererEventListener,
	                                  @NonNull AudioRendererEventListener audioRendererEventListener, @NonNull TextOutput textRendererOutput,
	                                  @NonNull MetadataOutput metadataRendererOutput) {
		return new Renderer[]{
				new MediaCodecAudioRenderer(getContext(), MediaCodecSelector.DEFAULT, eventHandler, audioRendererEventListener)
		};
	}

	/**
	 * show dialog and play audio
	 *
	 * @param data uri to the audio file
	 */
	public void show(Uri data) {
		if (!isShowing()) {
			this.data = data;
			super.show();
		}
	}
}