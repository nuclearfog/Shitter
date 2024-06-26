package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.InstanceLoader;
import org.nuclearfog.twidda.backend.async.StatusUpdater;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.update.PollUpdate;
import org.nuclearfog.twidda.backend.helper.update.StatusPreferenceUpdate;
import org.nuclearfog.twidda.backend.helper.update.StatusUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.recyclerview.IconAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.IconAdapter.OnIconClickListener;
import org.nuclearfog.twidda.ui.dialogs.AudioPlayerDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.EmojiPickerDialog;
import org.nuclearfog.twidda.ui.dialogs.EmojiPickerDialog.OnEmojiSelectListener;
import org.nuclearfog.twidda.ui.dialogs.PollDialog;
import org.nuclearfog.twidda.ui.dialogs.PollDialog.PollUpdateCallback;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;
import org.nuclearfog.twidda.ui.dialogs.StatusPreferenceDialog;
import org.nuclearfog.twidda.ui.dialogs.StatusPreferenceDialog.PreferenceSetCallback;
import org.nuclearfog.twidda.ui.views.InputView;
import org.nuclearfog.twidda.ui.views.InputView.OnTextChangeListener;

import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 * Status editor activity.
 *
 * @author nuclearfog
 */
public class StatusEditor extends MediaActivity implements ActivityResultCallback<ActivityResult>, OnClickListener, OnTextChangeListener,
		OnProgressStopListener, OnConfirmListener, OnIconClickListener, PollUpdateCallback, OnEmojiSelectListener, PreferenceSetCallback {

	/**
	 * return code used to send status information to calling activity
	 */
	public static final int RETURN_STATUS_UPDATE = 0x30220;

	/**
	 * key for the text added to the status if any
	 * value type is String
	 */
	public static final String KEY_TEXT = "status_text";

	/**
	 * Bundle key used to add a status to reply
	 * value type is {@link Status}
	 */
	public static final String KEY_REPLY_DATA = "status_reply_data";

	/**
	 * Bundle key used to add a status to edit
	 * value type is {@link Status}
	 */
	public static final String KEY_EDIT_DATA = "status_edit_data";

	/**
	 * Bundle key used to restore status update from previous lifecycle
	 * value type is {@link StatusUpdate}
	 */
	private static final String KEY_UPDATE_DATA = "status_update_data";


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
	private AsyncCallback<StatusUpdater.Result> statusUpdateResult = this::onStatusUpdated;
	private AsyncCallback<InstanceLoader.Result> instanceResult = this::onInstanceResult;

	private View mediaBtn;
	private View locationBtn;
	private View pollBtn;
	private View locationPending;
	private InputView statusText;

	private StatusUpdater statusUpdater;
	private InstanceLoader instanceLoader;

	private GlobalSettings settings;
	private EmojiPickerDialog emojiPicker;
	private IconAdapter adapter;

	private StatusUpdate statusUpdate = new StatusUpdate();
	@Nullable
	private Instance instance;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popup_status);
		ViewGroup root = findViewById(R.id.popup_status_root);
		ImageView background = findViewById(R.id.popup_status_background);
		View statusButton = findViewById(R.id.popup_status_send);
		View emojiButton = findViewById(R.id.popup_status_emoji);
		View preference = findViewById(R.id.popup_status_pref);
		RecyclerView iconList = findViewById(R.id.popup_status_media_icons);
		statusText = findViewById(R.id.popup_status_input);
		pollBtn = findViewById(R.id.popup_status_add_poll);
		locationBtn = findViewById(R.id.popup_status_add_location);
		mediaBtn = findViewById(R.id.popup_status_add_media);
		locationPending = findViewById(R.id.popup_status_location_loading);

		instanceLoader = new InstanceLoader(this);
		statusUpdater = new StatusUpdater(this);
		settings = GlobalSettings.get(this);
		emojiPicker = new EmojiPickerDialog(this, this);
		adapter = new IconAdapter(this, true);

		iconList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
		iconList.setAdapter(adapter);
		AppStyles.setEditorTheme(root, background);
		if (!settings.getLogin().getConfiguration().locationSupported()) {
			locationBtn.setVisibility(View.GONE);
		}
		if (!settings.getLogin().getConfiguration().isEmojiSupported()) {
			emojiButton.setVisibility(View.GONE);
		}

		// get statusupdate from previous lifecycle of this activity
		if (savedInstanceState != null) {
			statusUpdate = (StatusUpdate) savedInstanceState.getSerializable(KEY_UPDATE_DATA);
			statusText.setText(statusUpdate.getText());
			for (MediaStatus item : statusUpdate.getMediaStatuses()) {
				addMedia(item.getMediaType());
			}
		}
		// get parameters from other activities
		else {
			if (getIntent().hasExtra(KEY_TEXT)) {
				String prefix = getIntent().getStringExtra(KEY_TEXT);
				if (prefix != null) {
					statusUpdate.addText(prefix);
					statusText.setText(prefix);
				}
			} else if (getIntent().hasExtra(KEY_REPLY_DATA)) {
				Object data = getIntent().getSerializableExtra(KEY_REPLY_DATA);
				if (data instanceof Status) {
					Status status = (Status) data;
					statusUpdate.setStatusToReply(status);
					statusText.append(status.getUserMentions());
				}
			} else if (getIntent().hasExtra(KEY_EDIT_DATA)) {
				Object data = getIntent().getSerializableExtra(KEY_EDIT_DATA);
				if (data instanceof Status) {
					Status status = (Status) data;
					statusUpdate.setStatusToEdit(status);
					statusText.append(status.getText());
					for (Media media : status.getMedia()) {
						addMedia(media.getMediaType());
					}
					// disable attach button
					mediaBtn.setVisibility(View.GONE);
				}
			}
		}
		statusText.setOnTextChangeListener(this);
		emojiButton.setOnClickListener(this);
		preference.setOnClickListener(this);
		statusButton.setOnClickListener(this);
		pollBtn.setOnClickListener(this);
		mediaBtn.setOnClickListener(this);
		locationBtn.setOnClickListener(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
		if (statusUpdate.getInstance() == null) {
			InstanceLoader.Param param = new InstanceLoader.Param(InstanceLoader.Param.LOAD_LOCAL);
			instanceLoader.execute(param, instanceResult);
		}
		if (settings.getLogin().getConfiguration().locationSupported()) {
			if (isLocating()) {
				locationPending.setVisibility(View.VISIBLE);
				locationBtn.setVisibility(View.INVISIBLE);
			} else {
				locationPending.setVisibility(View.INVISIBLE);
				locationBtn.setVisibility(View.VISIBLE);
			}
		}
	}


	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_REPLY_DATA, statusUpdate);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onBackPressed() {
		if (statusUpdate.isEmpty()) {
			super.onBackPressed();
		} else {
			ConfirmDialog.show(this, ConfirmDialog.STATUS_EDITOR_LEAVE, null);
		}
	}


	@Override
	protected void onDestroy() {
		statusUpdater.cancel();
		instanceLoader.cancel();
		super.onDestroy();
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getResultCode() == ImageViewer.RETURN_MEDIA_STATUS_UPDATE) {
			if (result.getData() != null) {
				Serializable data = result.getData().getSerializableExtra(ImageViewer.KEY_IMAGE_DATA);
				if (data instanceof MediaStatus) {
					MediaStatus mediaStatus = (MediaStatus) data;
					statusUpdate.updateMediaStatus(mediaStatus);
				}
			}
		} else if (result.getResultCode() == VideoViewer.RESULT_VIDEO_UPDATE) {
			if (result.getData() != null) {
				Serializable data = result.getData().getSerializableExtra(VideoViewer.KEY_VIDEO_DATA);
				if (data instanceof MediaStatus) {
					MediaStatus mediaStatus = (MediaStatus) data;
					statusUpdate.updateMediaStatus(mediaStatus);
				}
			}
		} else {
			super.onActivityResult(result);
		}
	}


	@Override
	public void onClick(View v) {
		// send status
		if (v.getId() == R.id.popup_status_send) {
			// check if status is empty
			if (statusUpdate.isEmpty()) {
				Toast.makeText(getApplicationContext(), R.string.error_empty_status, Toast.LENGTH_SHORT).show();
			}
			// check if GPS location is pending
			else if (isLocating()) {
				Toast.makeText(getApplicationContext(), R.string.info_location_pending, Toast.LENGTH_SHORT).show();
			}
			// check if gps locating is not pending
			else if (statusUpdater.isIdle()) {
				updateStatus();
			}
		}
		// show closing message
		else if (v.getId() == R.id.popup_status_emoji) {
			emojiPicker.show();
		}
		// show poll dialog
		else if (v.getId() == R.id.popup_status_add_poll) {
			PollDialog.show(this, statusUpdate.getPoll(), instance);
		}
		// open status preference
		else if (v.getId() == R.id.popup_status_pref) {
			if (statusUpdate.getStatusId() == 0L) {
				StatusPreferenceDialog.show(this, statusUpdate.getStatusPreferences(), StatusPreferenceDialog.STATUS_POST);
			} else {
				StatusPreferenceDialog.show(this, statusUpdate.getStatusPreferences(), StatusPreferenceDialog.STATUS_EDIT);
			}
		}
		// Add media to the status
		else if (v.getId() == R.id.popup_status_add_media) {
			if (statusUpdate.getMediaStatuses().isEmpty()) {
				// request images/videos
				getMedia(REQUEST_ALL);
			} else {
				// request images only
				getMedia(REQUEST_IMAGE);
			}
		}
		// add location to the status
		else if (v.getId() == R.id.popup_status_add_location) {
			locationPending.setVisibility(View.VISIBLE);
			locationBtn.setVisibility(View.INVISIBLE);
			getLocation();
		}
	}


	@Override
	public void onEmojiSelected(Emoji emoji) {
		String tagToInsert;
		int start = Math.max(statusText.getSelectionStart(), 0);
		int end = Math.max(statusText.getSelectionEnd(), 0);
		if (start == 0) {
			tagToInsert = emoji.getCode() + ' ';
		} else if (end == statusText.length()) {
			tagToInsert = ' ' + emoji.getCode();
		} else {
			tagToInsert = ' ' + emoji.getCode() + ' ';
		}
		if (statusText.getText() != null) {
			statusText.getText().replace(Math.min(start, end), Math.max(start, end), tagToInsert, 0, tagToInsert.length());
		}
	}


	@Override
	public void onTextChanged(InputView inputView, String text) {
		if (inputView.getId() == R.id.popup_status_input) {
			statusUpdate.addText(text);
		}
	}


	@Override
	protected void onAttachLocation(@Nullable Location location) {
		if (location != null) {
			statusUpdate.addLocation(location);
			Toast.makeText(getApplicationContext(), R.string.info_gps_attached, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_gps, Toast.LENGTH_LONG).show();
		}
		locationPending.setVisibility(View.INVISIBLE);
		locationBtn.setVisibility(View.VISIBLE);
	}


	@Override
	protected void onMediaFetched(int resultType, @NonNull Uri uri) {
		try {
			MediaStatus mediaStatus = new MediaStatus(getApplicationContext(), uri);
			int mediaType = statusUpdate.addMedia(mediaStatus);
			addMedia(mediaType);
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), R.string.error_adding_media, Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	public void stopProgress() {
		statusUpdater.cancel();
	}


	@Override
	public void onConfirm(int type) {
		// retry uploading status
		if (type == ConfirmDialog.STATUS_EDITOR_ERROR) {
			updateStatus();
		}
		// leave editor
		else if (type == ConfirmDialog.STATUS_EDITOR_LEAVE) {
			finish();
		}
	}


	@Override
	public void onPreferenceSet(StatusPreferenceUpdate update) {
		statusUpdate.setStatusPreferences(update);
	}


	@Override
	public void onIconClick(int type, int index) {
		if (type == OnIconClickListener.MEDIA && index < statusUpdate.getMediaStatuses().size()) {
			MediaStatus media = statusUpdate.getMediaStatuses().get(index);
			switch (media.getMediaType()) {
				case MediaStatus.PHOTO:
				case MediaStatus.GIF:
					Intent intent = new Intent(this, ImageViewer.class);
					intent.putExtra(ImageViewer.KEY_IMAGE_DATA, media);
					activityResultLauncher.launch(intent);
					break;

				case MediaStatus.VIDEO:
					intent = new Intent(this, VideoViewer.class);
					intent.putExtra(VideoViewer.KEY_VIDEO_DATA, media);
					activityResultLauncher.launch(intent);
					break;

				case MediaStatus.AUDIO:
					if (media.getPath() != null) {
						AudioPlayerDialog.show(this, Uri.parse(media.getPath()));
					}
					break;
			}
		}
	}


	@Override
	public void onPollUpdate(@Nullable PollUpdate update) {
		statusUpdate.addPoll(update);
		if (update != null) {
			if (statusUpdate.mediaLimitReached()) {
				mediaBtn.setVisibility(View.GONE);
			}
		} else {
			if (!statusUpdate.mediaLimitReached()) {
				mediaBtn.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * update icon adapter and set buttons
	 *
	 * @param mediaType media type attached to {@link StatusUpdate}
	 */
	private void addMedia(int mediaType) {
		switch (mediaType) {
			case Media.PHOTO:
			case MediaStatus.PHOTO:
				adapter.addImageItem();
				break;

			case Media.GIF:
			case MediaStatus.GIF:
				adapter.addGifItem();
				break;

			case Media.VIDEO:
			case MediaStatus.VIDEO:
				adapter.addVideoItem();
				break;

			case Media.AUDIO:
			case MediaStatus.AUDIO:
				adapter.addAudioItem();
				break;

			default:
				Toast.makeText(getApplicationContext(), R.string.error_adding_media, Toast.LENGTH_SHORT).show();
				return;
		}
		// hide media button if limit is reached
		if (statusUpdate.mediaLimitReached()) {
			mediaBtn.setVisibility(View.GONE);
		}
		// hide poll button
		if (pollBtn.getVisibility() != View.GONE) {
			pollBtn.setVisibility(View.GONE);
		}
	}

	/**
	 * called when the status was successfully updated
	 */
	private void onStatusUpdated(@NonNull StatusUpdater.Result result) {
		if (result.exception != null) {
			if (result.exception.getErrorCode() != ConnectionException.INTERRUPTED) {
				String message = ErrorUtils.getErrorMessage(this, result.exception);
				ConfirmDialog.show(this, ConfirmDialog.STATUS_EDITOR_ERROR, message);
			}
		} else {
			if (result.status != null) {
				Intent intent = new Intent();
				intent.putExtra(KEY_REPLY_DATA, result.status);
				setResult(RETURN_STATUS_UPDATE, intent);
			}
			Toast.makeText(getApplicationContext(), R.string.info_status_sent, Toast.LENGTH_LONG).show();
			finish();
		}
		ProgressDialog.dismiss(this);
	}

	/**
	 * set instance information such as upload limits
	 */
	private void onInstanceResult(InstanceLoader.Result result) {
		if (result.instance != null) {
			this.instance = result.instance;
			statusUpdate.setInstanceInformation(result.instance);
		}
	}

	/**
	 * start uploading status and media files
	 */
	private void updateStatus() {
		// first initialize filestreams of the media files
		if (statusUpdate.prepare(getContentResolver())) {
			// send status
			statusUpdater.execute(statusUpdate, statusUpdateResult);
			// show progress dialog
			ProgressDialog.show(this, true);
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_media_init, Toast.LENGTH_SHORT).show();
		}
	}
}