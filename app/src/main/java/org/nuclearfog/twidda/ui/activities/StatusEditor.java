package org.nuclearfog.twidda.ui.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ui.adapter.IconAdapter;
import org.nuclearfog.twidda.ui.adapter.IconAdapter.OnMediaClickListener;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.StatusUpdater;
import org.nuclearfog.twidda.backend.helper.StatusUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;

/**
 * Status editor activity.
 *
 * @author nuclearfog
 */
public class StatusEditor extends MediaActivity implements OnClickListener, OnProgressStopListener, OnConfirmListener, OnMediaClickListener, TextWatcher {

	/**
	 * key to add a statusd ID to reply
	 * value type is Long
	 */
	public static final String KEY_STATUS_EDITOR_REPLYID = "status_reply_id";

	/**
	 * key for the text added to the status if any
	 * value type is String
	 */
	public static final String KEY_STATUS_EDITOR_TEXT = "status_text";

	private ImageButton mediaBtn, locationBtn;
	private View locationPending;

	@Nullable
	private StatusUpdater uploaderAsync;
	private GlobalSettings settings;

	private ConfirmDialog confirmDialog;
	private ProgressDialog loadingCircle;
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
		ImageButton statusButton = findViewById(R.id.popup_status_send);
		ImageButton closeButton = findViewById(R.id.popup_status_close);
		RecyclerView iconList = findViewById(R.id.popup_status_media_icons);
		EditText statusText = findViewById(R.id.popup_status_input);
		locationBtn = findViewById(R.id.popup_status_add_location);
		mediaBtn = findViewById(R.id.popup_status_add_media);
		locationPending = findViewById(R.id.popup_status_location_loading);

		settings = GlobalSettings.getInstance(this);
		loadingCircle = new ProgressDialog(this);
		confirmDialog = new ConfirmDialog(this);
		AppStyles.setEditorTheme(root, background);

		if (!settings.getLogin().getConfiguration().locationSupported()) {
			locationBtn.setVisibility(GONE);
		}
		long inReplyId = getIntent().getLongExtra(KEY_STATUS_EDITOR_REPLYID, 0);
		String prefix = getIntent().getStringExtra(KEY_STATUS_EDITOR_TEXT);
		statusUpdate.addReplyStatusId(inReplyId);
		if (prefix != null) {
			statusText.append(prefix);
		}
		adapter = new IconAdapter(settings);
		adapter.addOnMediaClickListener(this);
		iconList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
		iconList.setAdapter(adapter);

		closeButton.setOnClickListener(this);
		statusButton.setOnClickListener(this);
		mediaBtn.setOnClickListener(this);
		locationBtn.setOnClickListener(this);
		confirmDialog.setConfirmListener(this);
		loadingCircle.addOnProgressStopListener(this);
		adapter.addOnMediaClickListener(this);
		statusText.addTextChangedListener(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
		if (settings.getLogin().getConfiguration().locationSupported()) {
			if (isLocating()) {
				locationPending.setVisibility(VISIBLE);
				locationBtn.setVisibility(INVISIBLE);
			} else {
				locationPending.setVisibility(INVISIBLE);
				locationBtn.setVisibility(VISIBLE);
			}
		}
	}


	@Override
	protected void onDestroy() {
		loadingCircle.dismiss();
		if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING)
			uploaderAsync.cancel(true);
		super.onDestroy();
	}


	@Override
	public void onBackPressed() {
		showClosingMsg();
	}


	@Override
	public void onClick(View v) {
		// send status
		if (v.getId() == R.id.popup_status_send) {
			// check if status is empty
			if (statusUpdate.isEmpty()) {
				Toast.makeText(getApplicationContext(), R.string.error_empty_tweet, LENGTH_SHORT).show();
			}
			// check if GPS location is pending
			else if (isLocating()) {
				Toast.makeText(getApplicationContext(), R.string.info_location_pending, LENGTH_SHORT).show();
			}
			// check if gps locating is not pending
			else if (uploaderAsync == null || uploaderAsync.getStatus() != RUNNING) {
				updateStatus();
			}
		}
		// show closing message
		else if (v.getId() == R.id.popup_status_close) {
			showClosingMsg();
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
			locationPending.setVisibility(VISIBLE);
			locationBtn.setVisibility(INVISIBLE);
			getLocation();
		}
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
	}


	@Override
	protected void onAttachLocation(@Nullable Location location) {
		if (location != null) {
			statusUpdate.addLocation(location);
			Toast.makeText(getApplicationContext(), R.string.info_gps_attached, LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_gps, LENGTH_LONG).show();
		}
		locationPending.setVisibility(INVISIBLE);
		locationBtn.setVisibility(VISIBLE);
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
				Toast.makeText(getApplicationContext(), R.string.error_adding_media, LENGTH_SHORT).show();
				break;
		}
		if (statusUpdate.mediaLimitReached()) {
			mediaBtn.setVisibility(GONE);
		}
	}


	@Override
	public void stopProgress() {
		if (uploaderAsync != null && uploaderAsync.getStatus() == RUNNING) {
			uploaderAsync.cancel(true);
		}
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
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
			case StatusUpdate.MEDIA_GIF:
				Intent mediaViewer = new Intent(this, ImageViewer.class);
				mediaViewer.putExtra(ImageViewer.IMAGE_URI, uris[index]);
				startActivity(mediaViewer);
				break;

			case StatusUpdate.MEDIA_VIDEO:
				mediaViewer = new Intent(this, VideoViewer.class);
				mediaViewer.putExtra(VideoViewer.VIDEO_URI, uris[0]);
				mediaViewer.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
				startActivity(mediaViewer);
				break;
		}
	}

	/**
	 * called if status was updated successfully
	 */
	public void onSuccess() {
		Toast.makeText(getApplicationContext(), R.string.info_tweet_sent, LENGTH_LONG).show();
		finish();
	}

	/**
	 * Show confirmation dialog if an error occurs while sending status
	 */
	public void onError(@Nullable ConnectionException exception) {
		String message = ErrorHandler.getErrorMessage(this, exception);
		confirmDialog.show(ConfirmDialog.STATUS_EDITOR_ERROR, message);
		loadingCircle.dismiss();
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
	 * start uploading status and media files
	 */
	private void updateStatus() {
		// first initialize filestreams of the media files
		if (statusUpdate.prepare(getContentResolver())) {
			// send status
			uploaderAsync = new StatusUpdater(this);
			uploaderAsync.execute(statusUpdate);
			// show progress dialog
			loadingCircle.show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_media_init, LENGTH_SHORT).show();
		}
	}
}