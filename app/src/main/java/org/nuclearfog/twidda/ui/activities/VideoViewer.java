package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
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
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.backend.utils.LinkUtils;

import okhttp3.Call;

/**
 * video player activity to show local and online videos/animations
 *
 * @author nuclearfog
 */
public class VideoViewer extends AppCompatActivity implements Player.Listener {

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
	 * online video cache size
	 */
	private static final int CACHE_SIZE = 64000000;

	private ExoPlayer player;
	private Toolbar toolbar;

	private Uri data;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_video);
		StyledPlayerView playerView = findViewById(R.id.page_video_player);
		toolbar = findViewById(R.id.page_video_toolbar);
		playerView.setShowNextButton(false);
		playerView.setShowPreviousButton(false);

		toolbar.setTitle("");
		setSupportActionBar(toolbar);

		RenderersFactory renderersFactory = new RenderersFactory() {
			@Override
			public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener,
			                                  AudioRendererEventListener audioRendererEventListener, TextOutput textRendererOutput, MetadataOutput metadataRendererOutput) {
				return new Renderer[]{
						new MediaCodecVideoRenderer(getApplicationContext(), MediaCodecSelector.DEFAULT, 0L, eventHandler, videoRendererEventListener, 4),
						new MediaCodecAudioRenderer(getApplicationContext(), MediaCodecSelector.DEFAULT, eventHandler, audioRendererEventListener)
				};
			}
		};
		player = new ExoPlayer.Builder(this, renderersFactory).build();
		player.addListener(this);

		data = getIntent().getParcelableExtra(VIDEO_URI);
		boolean enableControls = getIntent().getBooleanExtra(ENABLE_VIDEO_CONTROLS, true);
		if (!enableControls) {
			playerView.setUseController(false);
			player.setRepeatMode(Player.REPEAT_MODE_ONE);
		}
		DataSource.Factory dataSourceFactory;
		MediaItem mediaItem = MediaItem.fromUri(data);

		// initialize online source
		if (data.getScheme().startsWith("http")) {
			// configure with okhttp connection of the app
			dataSourceFactory = new OkHttpDataSource.Factory((Call.Factory) ConnectionBuilder.create(this, CACHE_SIZE));
		}
		// initialize local source
		else {
			toolbar.setVisibility(View.GONE);
			dataSourceFactory = new DataSource.Factory() {
				@NonNull
				@Override
				public DataSource createDataSource() {
					return new ContentDataSource(getApplicationContext());
				}
			};
		}
		// initialize video extractor
		MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem);
		player.setMediaSource(mediaSource);
		playerView.setPlayer(player);

		player.prepare();
		player.setPlayWhenReady(true);
	}


	@Override
	protected void onPause() {
		super.onPause();
		player.pause();
	}


	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			toolbar.setVisibility(View.GONE);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			toolbar.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.video, menu);
		AppStyles.setMenuIconColor(menu, Color.WHITE);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_video_link) {
			if (data != null) {
				LinkUtils.openMediaLink(this, data);
			}
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onPlayerError(PlaybackException error) {
		Toast.makeText(getApplicationContext(), "ExoPlayer: " + error.getErrorCodeName(), Toast.LENGTH_SHORT).show();
		finish();
	}
}