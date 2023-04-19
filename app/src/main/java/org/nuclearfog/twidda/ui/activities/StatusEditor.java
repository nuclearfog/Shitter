package org.nuclearfog.twidda.ui.activities;

import static android.view.View.OnClickListener;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.InstanceLoader;
import org.nuclearfog.twidda.backend.async.StatusUpdater;
import org.nuclearfog.twidda.backend.async.StatusUpdater.StatusUpdateResult;
import org.nuclearfog.twidda.backend.helper.PollUpdate;
import org.nuclearfog.twidda.backend.helper.StatusUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Emoji;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.adapter.IconAdapter;
import org.nuclearfog.twidda.ui.adapter.IconAdapter.OnMediaClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.EmojiPicker;
import org.nuclearfog.twidda.ui.dialogs.EmojiPicker.OnEmojiSelectListener;
import org.nuclearfog.twidda.ui.dialogs.PollDialog;
import org.nuclearfog.twidda.ui.dialogs.PollDialog.PollUpdateCallback;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;
import org.nuclearfog.twidda.ui.dialogs.StatusPreferenceDialog;

import java.io.Serializable;

/**
 * Status editor activity.
 *
 * @author nuclearfog
 */
public class StatusEditor extends MediaActivity implements OnClickListener, OnProgressStopListener, OnConfirmListener,
		OnMediaClickListener, TextWatcher, PollUpdateCallback, OnEmojiSelectListener {

	/**
	 * key to add the status to reply
	 * value type is {@link Status}
	 */
	public static final String KEY_STATUS_EDITOR_DATA = "status_data";

	/**
	 * key for the text added to the status if any
	 * value type is String
	 */
	public static final String KEY_STATUS_EDITOR_TEXT = "status_text";

	/**
	 * key for status update to restore
	 * value type is {@link StatusUpdate}
	 */
	private static final String KEY_STATUS_UPDATE = "status_update";

	private AsyncCallback<StatusUpdateResult> statusUpdateResult = this::onStatusUpdated;
	private AsyncCallback<Instance> instanceResult = this::onInstanceResult;

	private View mediaBtn;
	private View locationBtn;
	private View pollBtn;
	private View locationPending;
	private EditText statusText;

	private StatusUpdater statusUpdater;
	private InstanceLoader instanceLoader;

	private GlobalSettings settings;
	private ConfirmDialog confirmDialog;
	private ProgressDialog loadingCircle;
	private PollDialog pollDialog;
	private EmojiPicker emojiPicker;
	private StatusPreferenceDialog preferenceDialog;
	private IconAdapter adapter;

	private StatusUpdate statusUpdate = new StatusUpdate();


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
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
		settings = GlobalSettings.getInstance(this);
		loadingCircle = new ProgressDialog(this);
		confirmDialog = new ConfirmDialog(this);
		preferenceDialog = new StatusPreferenceDialog(this, statusUpdate);
		pollDialog = new PollDialog(this, this);
		emojiPicker = new EmojiPicker(this, this);
		AppStyles.setEditorTheme(root, background);

		if (!settings.getLogin().getConfiguration().locationSupported()) {
			locationBtn.setVisibility(View.GONE);
		}
		long replyId = 0L;
		String prefix;
		Serializable serializedStatus = getIntent().getSerializableExtra(KEY_STATUS_EDITOR_DATA);
		if (serializedStatus instanceof Status) {
			Status status = (Status) serializedStatus;
			replyId = status.getId();
			statusUpdate.setVisibility(status.getVisibility());
			prefix = status.getUserMentions();
		} else {
			prefix = getIntent().getStringExtra(KEY_STATUS_EDITOR_TEXT);
		}
		statusUpdate.addReplyStatusId(replyId);
		if (prefix != null) {
			statusText.append(prefix);
		}
		adapter = new IconAdapter(settings, true);
		adapter.addOnMediaClickListener(this);
		iconList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
		iconList.setAdapter(adapter);

		statusText.addTextChangedListener(this);
		emojiButton.setOnClickListener(this);
		preference.setOnClickListener(this);
		statusButton.setOnClickListener(this);
		pollBtn.setOnClickListener(this);
		mediaBtn.setOnClickListener(this);
		locationBtn.setOnClickListener(this);
		confirmDialog.setConfirmListener(this);
		loadingCircle.addOnProgressStopListener(this);
		adapter.addOnMediaClickListener(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
		if (statusUpdate.getInstance() == null) {
			instanceLoader.execute(null, instanceResult);
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
		outState.putSerializable(KEY_STATUS_UPDATE, statusUpdate);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Serializable serializedStatusUpdate = savedInstanceState.getSerializable(KEY_STATUS_UPDATE);
		if (serializedStatusUpdate instanceof StatusUpdate) {
			statusUpdate = (StatusUpdate) serializedStatusUpdate;
		}
	}


	@Override
	public void onBackPressed() {
		showClosingMsg();
	}


	@Override
	protected void onDestroy() {
		loadingCircle.dismiss();
		statusUpdater.cancel();
		instanceLoader.cancel();
		super.onDestroy();
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
			pollDialog.show(statusUpdate.getPoll());
		}
		// open status preference
		else if (v.getId() == R.id.popup_status_pref) {
			preferenceDialog.show();
		}
		// Add media to the status
		else if (v.getId() == R.id.popup_status_add_media) {
			if (statusUpdate.getAttachmentType() == StatusUpdate.EMPTY) {
				// request images/videos
				getMedia(REQUEST_IMG_VID);
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
		statusText.append(emoji.getCode());
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	@Override
	public void afterTextChanged(Editable s) {
		statusUpdate.addText(s.toString());
		// todo add character limit check
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
		int mediaType = statusUpdate.addMedia(this, uri);
		switch (mediaType) {
			case StatusUpdate.MEDIA_IMAGE:
				adapter.addImageItem();
				break;

			case StatusUpdate.MEDIA_GIF:
				adapter.addGifItem();
				break;

			case StatusUpdate.MEDIA_VIDEO:
				adapter.addVideoItem();
				break;

			case StatusUpdate.MEDIA_ERROR:
				Toast.makeText(getApplicationContext(), R.string.error_adding_media, Toast.LENGTH_SHORT).show();
				break;
		}
		// hide media button if limit is reached
		if (statusUpdate.mediaLimitReached()) {
			mediaBtn.setVisibility(View.GONE);
		}
		// hide poll button
		if (mediaType != StatusUpdate.MEDIA_ERROR && pollBtn.getVisibility() != View.GONE) {
			pollBtn.setVisibility(View.GONE);
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
	public void onMediaClick(int index) {
		Uri[] uris = statusUpdate.getMediaUris();
		switch (statusUpdate.getAttachmentType()) {
			case StatusUpdate.MEDIA_IMAGE:
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.IMAGE_URI, uris[index]);
				intent.putExtra(ImageViewer.IMAGE_TYPE, ImageViewer.IMAGE_DEFAULT);
				startActivity(intent);
				break;

			case StatusUpdate.MEDIA_GIF:
				intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.IMAGE_URI, uris[index]);
				intent.putExtra(ImageViewer.IMAGE_TYPE, ImageViewer.IMAGE_GIF);
				startActivity(intent);
				break;

			case StatusUpdate.MEDIA_VIDEO:
				intent = new Intent(this, VideoViewer.class);
				intent.putExtra(VideoViewer.VIDEO_URI, uris[0]);
				intent.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
				startActivity(intent);
				break;
		}
	}


	@Override
	public void onPollUpdate(@Nullable PollUpdate update) {
		statusUpdate.addPoll(update);
	}

	/**
	 * show confirmation dialog when closing edited status
	 */
	private void showClosingMsg() {
		if (statusUpdate.isEmpty()) {
			finish();
		} else {
			confirmDialog.show(ConfirmDialog.STATUS_EDITOR_LEAVE);
		}
	}

	/**
	 * called when the status was successfully updated
	 */
	private void onStatusUpdated(@NonNull StatusUpdateResult result) {
		if (result.success) {
			Toast.makeText(getApplicationContext(), R.string.info_status_sent, Toast.LENGTH_LONG).show();
			finish();
		} else {
			String message = ErrorHandler.getErrorMessage(this, result.exception);
			confirmDialog.show(ConfirmDialog.STATUS_EDITOR_ERROR, message);
			loadingCircle.dismiss();
		}
	}

	/**
	 * set instance information such as upload limits
	 */
	private void onInstanceResult(Instance instance) {
		statusUpdate.setInstanceInformation(instance);
		pollDialog.setInstance(instance);
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
			loadingCircle.show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_media_init, Toast.LENGTH_SHORT).show();
		}
	}
}