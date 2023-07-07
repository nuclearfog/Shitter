package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.ui.dialogs.DescriptionDialog;
import org.nuclearfog.twidda.ui.dialogs.DescriptionDialog.DescriptionCallback;
import org.nuclearfog.twidda.ui.views.DescriptionView;

import java.io.Serializable;

import okhttp3.Call;

/**
 * video player activity to show local and online videos/animations
 *
 * @author nuclearfog
 */
public class VideoViewer extends AppCompatActivity implements Player.Listener, DescriptionCallback, RenderersFactory {

	/**
	 * bundle key used for media information
	 * value type is {@link Media}
	 */
	public static final String KEY_VIDEO_ONLINE = "media-video";

	/**
	 * bundle key used for mediastatus information
	 * value type is {@link MediaStatus}
	 */
	public static final String KEY_VIDEO_LOCAL = "video-media-status";

	/**
	 * Activity result code used to update {@link MediaStatus} information
	 */
	public static final int RESULT_VIDEO_UPDATE = 0x1528;

	/**
	 * online video cache size
	 */
	private static final int CACHE_SIZE = 64000000;

	@Nullable
	private DescriptionView descriptionView; // only used in portrait layout
	private Toolbar toolbar;
	private StyledPlayerView playerView;

	private DescriptionDialog descriptionDialog;

	private ExoPlayer player;
	@Nullable
	private MediaStatus mediaStatus;
	@Nullable
	private Media media;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.page_video);
		playerView = findViewById(R.id.page_video_player);
		toolbar = findViewById(R.id.page_video_toolbar);
		descriptionView = findViewById(R.id.page_video_description);
		descriptionDialog = new DescriptionDialog(this, this);
		player = new ExoPlayer.Builder(this, this).build();

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		playerView.setShowNextButton(false);
		playerView.setShowPreviousButton(false);

		MediaItem mediaItem;
		DataSource.Factory dataSourceFactory;
		Serializable serializedMedia, serializedMediaStatus;
		if (savedInstance != null) {
			serializedMedia = savedInstance.getSerializable(KEY_VIDEO_ONLINE);
			serializedMediaStatus = savedInstance.getSerializable(KEY_VIDEO_LOCAL);
		} else {
			serializedMedia = getIntent().getSerializableExtra(KEY_VIDEO_ONLINE);
			serializedMediaStatus = getIntent().getSerializableExtra(KEY_VIDEO_LOCAL);
		}
		// check if video is online
		if (serializedMedia instanceof Media) {
			this.media = (Media) serializedMedia;
			mediaItem = MediaItem.fromUri(media.getUrl());
			dataSourceFactory = new OkHttpDataSource.Factory((Call.Factory) ConnectionBuilder.create(this, CACHE_SIZE));
			if (media.getMediaType() != Media.VIDEO) {
				playerView.setUseController(false);
				player.setRepeatMode(Player.REPEAT_MODE_ONE);
			}
			if (descriptionView != null && !media.getDescription().isEmpty()) {
				descriptionView.setVisibility(View.VISIBLE);
				descriptionView.setDescription(media.getDescription());
			}
		}
		// check if viceo is local
		else if (serializedMediaStatus instanceof MediaStatus) {
			this.mediaStatus = (MediaStatus) serializedMediaStatus;
			mediaItem = MediaItem.fromUri(mediaStatus.getPath());
			dataSourceFactory = new DataSource.Factory() {
				@NonNull
				@Override
				public DataSource createDataSource() {
					return new ContentDataSource(getApplicationContext());
				}
			};
			if (mediaStatus.getMediaType() != MediaStatus.VIDEO) {
				playerView.setUseController(false);
				player.setRepeatMode(Player.REPEAT_MODE_ONE);
			}
			if (descriptionView != null && !mediaStatus.getDescription().isEmpty()) {
				descriptionView.setVisibility(View.VISIBLE);
				descriptionView.setDescription(mediaStatus.getDescription());
			}
		} else {
			finish();
			return;
		}
		// initialize video extractor
		player.setMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem));
		playerView.setPlayer(player);
		// prepare playback
		player.addListener(this);
		player.prepare();
		player.setPlayWhenReady(true);
	}


	@Override
	protected void onPause() {
		super.onPause();
		if (player.isPlaying()) {
			player.pause();
		}
	}


	@Override
	public void onBackPressed() {
		if (mediaStatus != null) {
			Intent intent = new Intent();
			intent.putExtra(KEY_VIDEO_LOCAL, mediaStatus);
			setResult(RESULT_VIDEO_UPDATE, intent);
		}
		player.stop();
		super.onBackPressed();
	}


	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_VIDEO_LOCAL, mediaStatus);
		outState.putSerializable(KEY_VIDEO_ONLINE, media);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onDestroy() {
		// remove player reference to prevent memory leak
		playerView.setPlayer(null);
		super.onDestroy();
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
		MenuItem menuOpenLink = menu.findItem(R.id.menu_video_link);
		MenuItem menuDescription = menu.findItem(R.id.menu_video_add_description);
		AppStyles.setMenuIconColor(menu, Color.WHITE);
		menuOpenLink.setVisible(media != null);
		menuDescription.setVisible(mediaStatus != null);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_video_link) {
			if (media != null) {
				LinkUtils.openMediaLink(this, Uri.parse(media.getUrl()));
			}
		}
		//
		else if (item.getItemId() == R.id.menu_video_add_description) {
			descriptionDialog.show();
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onDescriptionSet(String description) {
		if (mediaStatus != null) {
			mediaStatus.setDescription(description);
		}
		if (descriptionView != null) {
			if (!description.trim().isEmpty()) {
				descriptionView.setDescription(description);
				descriptionView.setVisibility(View.VISIBLE);
			} else {
				descriptionView.setDescription("");
				descriptionView.setVisibility(View.INVISIBLE);
			}
		}
	}


	@Override
	public void onPlayerError(PlaybackException error) {
		Toast.makeText(getApplicationContext(), "ExoPlayer: " + error.getErrorCodeName(), Toast.LENGTH_SHORT).show();
		finish();
	}


	@Override
	public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener, AudioRendererEventListener audioRendererEventListener,
	                                  TextOutput textRendererOutput, MetadataOutput metadataRendererOutput) {
		return new Renderer[]{
				new MediaCodecVideoRenderer(getApplicationContext(), MediaCodecSelector.DEFAULT, 0L, eventHandler, videoRendererEventListener, 4),
				new MediaCodecAudioRenderer(getApplicationContext(), MediaCodecSelector.DEFAULT, eventHandler, audioRendererEventListener)
		};
	}
}