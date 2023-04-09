package org.nuclearfog.twidda.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ConnectionBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;

import okhttp3.Call;

/**
 * video player activity to show local and online videos/animations
 *
 * @author nuclearfog
 */
public class VideoViewer extends AppCompatActivity {

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

	private GlobalSettings settings;
	private ExoPlayer player;

	private Uri data;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_video);

		ViewGroup root = findViewById(R.id.page_video_root);
		StyledPlayerView playerView = findViewById(R.id.page_video_player);
		Toolbar toolbar = findViewById(R.id.page_video_toolbar);

		player = new ExoPlayer.Builder(this).build();
		settings = GlobalSettings.getInstance(this);

		AppStyles.setTheme(root);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setTitle("");
		}

		data = getIntent().getParcelableExtra(VIDEO_URI);
		boolean enableControls = getIntent().getBooleanExtra(ENABLE_VIDEO_CONTROLS, true);
		if (!enableControls) {
			playerView.setUseController(false);
			player.setRepeatMode(Player.REPEAT_MODE_ONE);
		}

		MediaItem mediaItem = MediaItem.fromUri(data);
		DataSource.Factory dataSourceFactory = new OkHttpDataSource.Factory((Call.Factory) ConnectionBuilder.create(this, 128000));
		MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);

		player.setMediaSource(mediaSource);
		playerView.setPlayer(player);

		player.prepare();
		player.play();
	}


	@Override
	protected void onPause() {
		super.onPause();
		player.pause();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.video, menu);
		AppStyles.setMenuIconColor(menu, settings.getIconColor());
		menu.findItem(R.id.menu_video_link).setVisible(data.getScheme().startsWith("http"));
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_video_link) {
			if (data != null) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(data);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException err) {
					Toast.makeText(getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}
}