package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;

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

import okhttp3.Call;

/**
 * Dialog with audio player and controls
 *
 * @author nuclearfog
 */
public class AudioPlayerDialog  extends Dialog {

	private ExoPlayer player;

	/**
	 * @inheritDoc
	 */
	public AudioPlayerDialog(@NonNull Context context) {
		super(context, R.style.AudioDialog);
		PlayerControlView controls = new PlayerControlView(context);
		setContentView(controls);
		controls.setShowNextButton(false);
		controls.setShowPreviousButton(false);
		player = new ExoPlayer.Builder(context, createRenderer(context)).build();
		controls.setPlayer(player);
		controls.setShowTimeoutMs(-1);
	}


	@Override
	public void show() {
	}

	/**
	 * show dialog and play audio
	 *
	 * @param data uri to the audio file
	 */
	public void show(Uri data) {
		if (isShowing())
			return;
		super.show();

		DataSource.Factory dataSourceFactory;
		MediaItem mediaItem = MediaItem.fromUri(data);

		// initialize online source
		if (data.getScheme().startsWith("http")) {
			// configure with okhttp connection of the app
			dataSourceFactory = new OkHttpDataSource.Factory((Call.Factory) ConnectionBuilder.create(getContext()));
		}
		// initialize local source
		else {
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
	}

	/**
	 *
	 */
	private RenderersFactory createRenderer(Context context) {
		return new RenderersFactory() {
			@Override
			public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener,
				AudioRendererEventListener audioRendererEventListener, TextOutput textRendererOutput, MetadataOutput metadataRendererOutput) {
				return new Renderer[]{
						new MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, eventHandler, audioRendererEventListener)
				};
			}
		};
	}
}