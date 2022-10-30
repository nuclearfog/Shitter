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
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.StatusUpdater;
import org.nuclearfog.twidda.backend.update.StatusUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;

/**
 * Status editor activity.
 *
 * @author nuclearfog
 */
public class StatusEditor extends MediaActivity implements OnClickListener, OnProgressStopListener, OnConfirmListener {

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

	private static final String MIME_GIF = "image/gif";
	private static final String MIME_IMAGE_ALL = "image/";
	private static final String MIME_VIDEO_ALL = "video/";

	/**
	 * image limit of a status
	 */
	private static final int MAX_IMAGES = 4;

	/**
	 * video limit of a status
	 */
	private static final int MAX_VIDEOS = 1;

	/**
	 * gif limit of a status
	 */
	private static final int MAX_GIF = 1;

	/**
	 * mention limit of a status
	 */
	private static final int MAX_MENTIONS = 10;

	private static final int MEDIA_NONE = 0;
	private static final int MEDIA_IMAGE = 1;
	private static final int MEDIA_VIDEO = 2;
	private static final int MEDIA_GIF = 3;

	private StatusUpdater uploaderAsync;
	private GlobalSettings settings;

	private ConfirmDialog confirmDialog;
	private ProgressDialog loadingCircle;

	private ImageButton mediaBtn, previewBtn, locationBtn;
	private EditText statusText;
	private View locationPending;

	private StatusUpdate statusUpdate = new StatusUpdate();
	private int selectedFormat = MEDIA_NONE;


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
		locationBtn = findViewById(R.id.popup_status_add_location);
		mediaBtn = findViewById(R.id.popup_status_add_media);
		previewBtn = findViewById(R.id.popup_status_prev_media);
		statusText = findViewById(R.id.popup_status_input);
		locationPending = findViewById(R.id.popup_status_location_loading);

		settings = GlobalSettings.getInstance(this);
		loadingCircle = new ProgressDialog(this);
		confirmDialog = new ConfirmDialog(this);
		AppStyles.setEditorTheme(root, background);

		long inReplyId = getIntent().getLongExtra(KEY_STATUS_EDITOR_REPLYID, 0);
		String prefix = getIntent().getStringExtra(KEY_STATUS_EDITOR_TEXT);

		statusUpdate.setReplyId(inReplyId);
		if (prefix != null) {
			statusText.append(prefix);
		}

		closeButton.setOnClickListener(this);
		statusButton.setOnClickListener(this);
		mediaBtn.setOnClickListener(this);
		previewBtn.setOnClickListener(this);
		locationBtn.setOnClickListener(this);
		confirmDialog.setConfirmListener(this);
		loadingCircle.addOnProgressStopListener(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
		if (isLocating()) {
			locationPending.setVisibility(VISIBLE);
			locationBtn.setVisibility(INVISIBLE);
		} else {
			locationPending.setVisibility(INVISIBLE);
			locationBtn.setVisibility(VISIBLE);
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
			String statusText = this.statusText.getText().toString();
			// check if status is empty
			if (statusText.trim().isEmpty() && statusUpdate.mediaCount() == 0) {
				Toast.makeText(this, R.string.error_empty_tweet, LENGTH_SHORT).show();
			}
			// check if mentions exceed the limit
			else if (StringTools.countMentions(statusText) > MAX_MENTIONS) {
				Toast.makeText(this, R.string.error_mention_exceed, LENGTH_SHORT).show();
			}
			// check if GPS location is pending
			else if (isLocating()) {
				Toast.makeText(this, R.string.info_location_pending, LENGTH_SHORT).show();
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
			if (selectedFormat == MEDIA_NONE) {
				// request images/videos
				getMedia(REQUEST_IMG_VID);
			} else {
				// request images only
				getMedia(REQUEST_IMAGE);
			}
		}
		// open media preview
		else if (v.getId() == R.id.popup_status_prev_media) {
			Uri[] uris = statusUpdate.getMediaUris();
			//
			if (selectedFormat == MEDIA_VIDEO) {
				Intent mediaViewer = new Intent(this, VideoViewer.class);
				mediaViewer.putExtra(VideoViewer.VIDEO_URI, uris[0]);
				mediaViewer.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
				startActivity(mediaViewer);
			}
			//
			else if (selectedFormat == MEDIA_IMAGE) {
				Intent mediaViewer = new Intent(this, ImageViewer.class);
				mediaViewer.putExtra(ImageViewer.IMAGE_URIS, uris);
				mediaViewer.putExtra(ImageViewer.IMAGE_DOWNLOAD, false);
				startActivity(mediaViewer);
			}
			//
			else if (selectedFormat == MEDIA_GIF) {
				// todo add support for local gif animation
				Intent mediaViewer = new Intent(this, ImageViewer.class);
				mediaViewer.putExtra(ImageViewer.IMAGE_URIS, uris);
				mediaViewer.putExtra(ImageViewer.IMAGE_DOWNLOAD, false);
				startActivity(mediaViewer);
			}
		}
		// add location to the status
		else if (v.getId() == R.id.popup_status_add_location) {
			locationPending.setVisibility(VISIBLE);
			locationBtn.setVisibility(INVISIBLE);
			getLocation(true);
		}
	}


	@Override
	protected void onAttachLocation(@Nullable Location location) {
		if (location != null) {
			statusUpdate.setLocation(location);
			Toast.makeText(this, R.string.info_gps_attached, LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.error_gps, LENGTH_LONG).show();
		}
		locationPending.setVisibility(INVISIBLE);
		locationBtn.setVisibility(VISIBLE);
	}


	@Override
	protected void onMediaFetched(int resultType, @NonNull Uri uri) {
		int mediaCount = 0;
		String mime = getContentResolver().getType(uri);
		if (mime == null) {
			Toast.makeText(this, R.string.error_file_format, LENGTH_SHORT).show();
		}
		// check if file is a 'gif' image
		else if (mime.equals(MIME_GIF)) {
			if (selectedFormat == MEDIA_NONE || selectedFormat == MEDIA_GIF) {
				mediaCount = addStatusMedia(uri, R.drawable.gif, MAX_GIF);
				if (mediaCount > 0) {
					selectedFormat = MEDIA_GIF;
				}
			}
		}
		// check if file is an image
		else if (mime.startsWith(MIME_IMAGE_ALL)) {
			if (selectedFormat == MEDIA_NONE || selectedFormat == MEDIA_IMAGE) {
				mediaCount = addStatusMedia(uri, R.drawable.image, MAX_IMAGES);
				if (mediaCount > 0) {
					selectedFormat = MEDIA_IMAGE;
				}
			}
		}
		// check if file is a video
		else if (mime.startsWith(MIME_VIDEO_ALL)) {
			if (selectedFormat == MEDIA_NONE || selectedFormat == MEDIA_VIDEO) {
				mediaCount = addStatusMedia(uri, R.drawable.video, MAX_VIDEOS);
				if (mediaCount > 0) {
					selectedFormat = MEDIA_VIDEO;
				}
			}
		}
		// check if media was successfully added
		if (mediaCount <= 0) {
			Toast.makeText(this, R.string.error_adding_media, LENGTH_SHORT).show();
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

	/**
	 * called if status was updated successfully
	 */
	public void onSuccess() {
		Toast.makeText(this, R.string.info_tweet_sent, LENGTH_LONG).show();
		finish();
	}

	/**
	 * Show confirmation dialog if an error occurs while sending status
	 */
	public void onError(@Nullable ConnectionException error) {
		String message = ErrorHandler.getErrorMessage(this, error);
		confirmDialog.show(ConfirmDialog.STATUS_EDITOR_ERROR, message);
		loadingCircle.dismiss();
	}

	/**
	 * show confirmation dialog when closing edited status
	 */
	private void showClosingMsg() {
		if (statusText.length() > 0 || statusUpdate.mediaCount() > 0 || statusUpdate.hasLocation()) {
			confirmDialog.show(ConfirmDialog.STATUS_EDITOR_LEAVE);
		} else {
			finish();
		}
	}

	/**
	 * attach media to the status
	 *
	 * @param uri   Uri link of the media
	 * @param icon  icon of the preview button
	 * @param limit limit of the media count
	 * @return media count or -1 if adding failed
	 */
	private int addStatusMedia(Uri uri, @DrawableRes int icon, int limit) {
		previewBtn.setImageResource(icon);
		AppStyles.setDrawableColor(previewBtn, settings.getIconColor());
		int mediaCount = statusUpdate.addMedia(this, uri);
		if (mediaCount > 0)
			previewBtn.setVisibility(VISIBLE);
		// if limit reached, remove mediaselect button
		if (mediaCount == limit) {
			mediaBtn.setVisibility(GONE);
		}
		return mediaCount;
	}

	/**
	 * start uploading status and media files
	 */
	private void updateStatus() {
		// first initialize filestreams of the media files
		if (statusUpdate.prepare(getContentResolver())) {
			String statusText = this.statusText.getText().toString();
			// add media
			statusUpdate.setText(statusText);
			// send status
			uploaderAsync = new StatusUpdater(this);
			uploaderAsync.execute(statusUpdate);
			// show progress dialog
			loadingCircle.show();
		} else {
			Toast.makeText(this, R.string.error_media_init, LENGTH_SHORT).show();
		}
	}
}