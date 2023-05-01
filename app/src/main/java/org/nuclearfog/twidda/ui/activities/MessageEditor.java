package org.nuclearfog.twidda.ui.activities;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.InstanceLoader;
import org.nuclearfog.twidda.backend.async.MessageUpdater;
import org.nuclearfog.twidda.backend.async.MessageUpdater.MessageUpdateResult;
import org.nuclearfog.twidda.backend.helper.update.MessageUpdate;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog.OnProgressStopListener;

import java.io.Serializable;

/**
 * Directmessage editor activity
 *
 * @author nuclearfog
 */
public class MessageEditor extends MediaActivity implements OnClickListener, OnConfirmListener, OnProgressStopListener {

	/**
	 * key for the screenname if any
	 * value type is String
	 */
	public static final String KEY_MESSAGE_PREFIX = "message_prefix";

	/**
	 * key for message update
	 * value type is {@link MessageUpdate}
	 */
	private static final String KEY_MESSAGE_UPDATE = "message_update";

	private AsyncCallback<Instance> instanceResult = this::onInstanceResult;
	private AsyncCallback<MessageUpdateResult> messageResult = this::onMessageResult;

	private InstanceLoader instanceLoader;
	private MessageUpdater messageUpdater;

	private ProgressDialog loadingCircle;
	private ConfirmDialog confirmDialog;

	private EditText receiver, message;
	private ImageButton media, preview;

	private MessageUpdate messageUpdate = new MessageUpdate();


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.popup_message);
		ViewGroup root = findViewById(R.id.dm_popup);
		ImageButton send = findViewById(R.id.popup_message_send);
		ImageView background = findViewById(R.id.popup_message_background);
		media = findViewById(R.id.popup_message_media);
		preview = findViewById(R.id.popup_message_preview);
		receiver = findViewById(R.id.popup_message_receiver);
		message = findViewById(R.id.popup_message_text);
		AppStyles.setEditorTheme(root, background);

		messageUpdater = new MessageUpdater(this);
		instanceLoader = new InstanceLoader(this);
		loadingCircle = new ProgressDialog(this);
		confirmDialog = new ConfirmDialog(this);

		String prefix = getIntent().getStringExtra(KEY_MESSAGE_PREFIX);
		if (prefix != null) {
			receiver.append(prefix);
		}
		send.setOnClickListener(this);
		media.setOnClickListener(this);
		preview.setOnClickListener(this);
		loadingCircle.addOnProgressStopListener(this);
		confirmDialog.setConfirmListener(this);
	}


	@Override
	protected void onResume() {
		if (messageUpdate.getInstance() == null) {
			instanceLoader.execute(null, instanceResult);
		}
		super.onResume();
	}


	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_MESSAGE_UPDATE, messageUpdate);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Serializable serializedStatusUpdate = savedInstanceState.getSerializable(KEY_MESSAGE_UPDATE);
		if (serializedStatusUpdate instanceof MessageUpdate) {
			messageUpdate = (MessageUpdate) serializedStatusUpdate;
		}
	}


	@Override
	public void onBackPressed() {
		if (receiver.getText().length() == 0 && message.getText().length() == 0 && messageUpdate.getMediaUri() == null) {
			loadingCircle.dismiss();
			super.onBackPressed();
		} else {
			confirmDialog.show(ConfirmDialog.MESSAGE_EDITOR_LEAVE);
		}
	}


	@Override
	protected void onDestroy() {
		messageUpdater.cancel();
		if (messageUpdate != null)
			messageUpdate.close();
		super.onDestroy();
	}


	@Override
	protected void onAttachLocation(@Nullable Location location) {
	}


	@Override
	protected void onMediaFetched(int resultType, @NonNull Uri uri) {
		if (resultType == REQUEST_IMAGE) {
			if (messageUpdate.addMedia(this, uri)) {
				preview.setVisibility(VISIBLE);
				media.setVisibility(GONE);
			} else {
				Toast.makeText(getApplicationContext(), R.string.error_adding_media, LENGTH_SHORT).show();
			}
		}
	}


	@Override
	public void onClick(View v) {
		// send direct message
		if (v.getId() == R.id.popup_message_send) {
			if (messageUpdater.isIdle()) {
				sendMessage();
			}
		}
		// get media
		else if (v.getId() == R.id.popup_message_media) {
			getMedia(REQUEST_IMAGE);
		}
		// open media
		else if (v.getId() == R.id.popup_message_preview) {
			if (messageUpdate.getMediaUri() != null) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.IMAGE_URI, messageUpdate.getMediaUri());
				intent.putExtra(ImageViewer.IMAGE_TYPE, ImageViewer.IMAGE_DEFAULT);
				startActivity(intent);
			}
		}
	}


	@Override
	public void stopProgress() {
		messageUpdater.cancel();
	}


	@Override
	public void onConfirm(int type) {
		// retry sending message
		if (type == ConfirmDialog.MESSAGE_EDITOR_ERROR) {
			sendMessage();
		}
		// leave message editor
		else if (type == ConfirmDialog.MESSAGE_EDITOR_LEAVE) {
			finish();
		}
	}

	/**
	 * check inputs and send message
	 */
	private void sendMessage() {
		String username = receiver.getText().toString();
		String message = this.message.getText().toString();
		if (!username.trim().isEmpty() && (!message.trim().isEmpty() || messageUpdate.getMediaUri() != null)) {
			if (messageUpdate.prepare(getContentResolver())) {
				messageUpdate.setReceiver(username);
				messageUpdate.setText(message);
				messageUpdater.execute(messageUpdate, messageResult);
				loadingCircle.show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.error_media_init, LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), R.string.error_dm, LENGTH_SHORT).show();
		}
	}

	/**
	 *
	 */
	private void onMessageResult(@NonNull MessageUpdateResult result) {
		if (result.success) {
			Toast.makeText(getApplicationContext(), R.string.info_dm_send, Toast.LENGTH_SHORT).show();
			finish();
		} else {
			String message = ErrorHandler.getErrorMessage(this, result.exception);
			confirmDialog.show(ConfirmDialog.MESSAGE_EDITOR_ERROR, message);
			loadingCircle.dismiss();
		}
	}

	/**
	 *
	 */
	private void onInstanceResult(Instance instance) {
		messageUpdate.setInstanceInformation(instance);
	}
}