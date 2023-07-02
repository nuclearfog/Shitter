package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.backend.utils.LinkUtils;

import java.io.Closeable;

import okhttp3.Call;

/**
 * Dialog with audio player and controls
 *
 * @author nuclearfog
 */
public class AudioPlayerDialog extends Dialog implements OnClickListener, Closeable {

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
		player = new ExoPlayer.Builder(activity.getApplicationContext(), createRenderer(activity.getApplicationContext())).build();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_audio_player);
		mediaLink = findViewById(R.id.dialog_audio_player_share);
		controls = findViewById(R.id.dialog_audio_player_controls);

		controls.setShowNextButton(false);
		controls.setShowPreviousButton(false);
		controls.setPlayer(player);
		controls.setShowTimeoutMs(-1);

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
				dataSourceFactory = new OkHttpDataSource.Factory((Call.Factory) ConnectionBuilder.create(getContext()));
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
		player.pause();
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
			controls.setPlayer(null);
		}
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

	/**
	 *
	 */
	private RenderersFactory createRenderer(Context context) {
		return new RenderersFactory() {
			@Override
			public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener, AudioRendererEventListener audioRendererEventListener,
			                                  TextOutput textRendererOutput, MetadataOutput metadataRendererOutput) {
				return new Renderer[]{
						new MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, eventHandler, audioRendererEventListener)
				};
			}
		};
	}
}