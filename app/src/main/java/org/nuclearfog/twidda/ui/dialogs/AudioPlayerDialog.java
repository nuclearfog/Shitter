package org.nuclearfog.twidda.ui.dialogs;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
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

/**
 * Dialog with audio player and controls
 *
 * @author nuclearfog
 */
@SuppressLint("UnsafeOptInUsageError")
public class AudioPlayerDialog extends DialogFragment implements OnClickListener {

	/**
	 * Bundle key used to set/restore audio uri
	 * value type is {@link Uri}
	 */
	private static final String KEY_URI = "audio-uri";

	/**
	 * Bundle key used to restore last audio position
	 * value type is long
	 */
	private static final String KEY_POS = "audio-pos";

	private PlayerControlView controls;
	private TextView mediaLink;

	private ExoPlayer player;

	private Uri data;

	/**
	 *
	 */
	public AudioPlayerDialog() {
		setStyle(STYLE_NO_TITLE, R.style.AudioDialog);
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.dialog_audio_player, container, false);
		mediaLink = root.findViewById(R.id.dialog_audio_player_share);
		controls = root.findViewById(R.id.dialog_audio_player_controls);

		mediaLink.setOnClickListener(this);
		return root;
	}


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		long lastPos = 0L;
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			data = savedInstanceState.getParcelable(KEY_URI);
			lastPos = savedInstanceState.getLong(KEY_POS, 0L);
		}
		DataSource.Factory dataSourceFactory;
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
					return new ContentDataSource(requireContext());
				}
			};
		}
		MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(MediaItem.fromUri(data));
		player = new ExoPlayer.Builder(requireContext(), new RenderersFactory() {
			@NonNull
			@Override
			public Renderer[] createRenderers(@NonNull Handler eventHandler, @NonNull VideoRendererEventListener videoRendererEventListener,
			                                  @NonNull AudioRendererEventListener audioRendererEventListener, @NonNull TextOutput textRendererOutput,
			                                  @NonNull MetadataOutput metadataRendererOutput) {
				return new Renderer[] { new MediaCodecAudioRenderer(requireContext(), MediaCodecSelector.DEFAULT, eventHandler, audioRendererEventListener)};
			}
		}).build();
		player.setMediaSource(mediaSource);
		player.setPlayWhenReady(true);
		player.seekTo(lastPos);
		player.prepare();
		controls.setPlayer(player);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putParcelable(KEY_URI, data);
		outState.putLong(KEY_POS, player.getCurrentPosition());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onStop() {
		super.onStop();
		if (player.isPlaying()) {
			player.pause();
		}
	}


	@Override
	public void onDestroyView() {
		// remove player to prevent memory leak
		if (controls != null) {
			player.stop();
			player.release();
			controls.setPlayer(null);
		}
		super.onDestroyView();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_audio_player_share) {
			LinkUtils.openMediaLink(v.getContext(), data);
		}
	}

	/**
	 * show audioplayer dialog
	 *
	 * @param fragment parent fragment
	 * @param data path/url to the audio file
	 */
	public static void show(Fragment fragment, Uri data) {
		if (fragment.isAdded()) {
			show(fragment.getChildFragmentManager(), data);
		}
	}

	/**
	 * show audioplayer dialog
	 *
	 * @param activity parent activity
	 * @param data path/url to the audio file
	 */
	public static void show(FragmentActivity activity, Uri data) {
		show(activity.getSupportFragmentManager(), data);
	}

	/**
	 *
	 */
	private static void show(FragmentManager fm, Uri data) {
		String tag = data.toString();
		Fragment dialogFragment = fm.findFragmentByTag(tag);
		if (dialogFragment == null) {
			AudioPlayerDialog dialog = new AudioPlayerDialog();
			Bundle args = new Bundle();
			args.putParcelable(KEY_URI, data);
			dialog.setArguments(args);
			dialog.show(fm, tag);
		}
	}
}