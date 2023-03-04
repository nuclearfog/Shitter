package org.nuclearfog.twidda.ui.fragments;

import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.ui.activities.MessageEditor.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_USER;
import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.KEY_STATUS_ID;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.KEY_STATUS_NAME;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.TWITTER_LINK_PATTERN;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.MessageLoader;
import org.nuclearfog.twidda.backend.async.MessageLoader.MessageLoaderParam;
import org.nuclearfog.twidda.backend.async.MessageLoader.MessageLoaderResult;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.ui.activities.ImageViewer;
import org.nuclearfog.twidda.ui.activities.MessageEditor;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.activities.StatusActivity;
import org.nuclearfog.twidda.ui.adapter.MessageAdapter;
import org.nuclearfog.twidda.ui.adapter.MessageAdapter.OnMessageClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

import java.util.List;

/**
 * Fragment class to show a list of directmessages
 *
 * @author nuclearfog
 */
public class MessageFragment extends ListFragment implements OnMessageClickListener, OnConfirmListener, AsyncCallback<MessageLoaderResult> {

	private MessageLoader messageLoader;
	private MessageAdapter adapter;
	private ConfirmDialog confirmDialog;

	private long deleteId;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		confirmDialog = new ConfirmDialog(requireContext());
		adapter = new MessageAdapter(requireContext(), this);
		messageLoader = new MessageLoader(requireContext());
		setAdapter(adapter);

		confirmDialog.setConfirmListener(this);

		loadMessages(false, null);
		setRefresh(true);
	}


	@Override
	protected void onReset() {
		adapter = new MessageAdapter(requireContext(), this);
		setAdapter(adapter);
		loadMessages(false, null);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		messageLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		loadMessages(true, null);
	}


	@Override
	public void onTagClick(String tag) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), SearchActivity.class);
			intent.putExtra(KEY_SEARCH_QUERY, tag);
			startActivity(intent);
		}
	}


	@Override
	public void onLinkClick(String tag) {
		Uri link = Uri.parse(tag);
		// open status link
		if (TWITTER_LINK_PATTERN.matcher(link.getScheme() + "://" + link.getHost() + link.getPath()).matches()) {
			List<String> segments = link.getPathSegments();
			Intent intent = new Intent(requireContext(), StatusActivity.class);
			intent.putExtra(KEY_STATUS_ID, Long.parseLong(segments.get(2)));
			intent.putExtra(KEY_STATUS_NAME, segments.get(0));
			startActivity(intent);
		}
		// open link in browser
		else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(link);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(requireContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
			}
		}
	}


	@Override
	public void onClick(Message message, int action, int... extras) {
		if (!isRefreshing()) {
			switch (action) {
				case ANSWER:
					Intent sendDm = new Intent(requireContext(), MessageEditor.class);
					sendDm.putExtra(KEY_DM_PREFIX, message.getSender().getScreenname());
					startActivity(sendDm);
					break;

				case DELETE:
					if (!confirmDialog.isShowing() && messageLoader.isIdle()) {
						deleteId = message.getId();
						confirmDialog.show(ConfirmDialog.MESSAGE_DELETE);
					}
					break;

				case PROFILE:
					Intent profile = new Intent(requireContext(), ProfileActivity.class);
					profile.putExtra(KEY_PROFILE_USER, message.getSender());
					startActivity(profile);
					break;

				case MEDIA:
					if (extras.length == 1) {
						int mediaIndex = extras[0];
						if (mediaIndex >= 0 && mediaIndex < message.getMedia().length) {
							Intent imageIntent = new Intent(requireContext(), ImageViewer.class);
							imageIntent.putExtra(ImageViewer.IMAGE_URI, Uri.parse(message.getMedia()[mediaIndex].getUrl()));
							startActivity(imageIntent);
						}
					}
					break;
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(String cursor) {
		if (messageLoader.isIdle()) {
			loadMessages(false, cursor);
			return true;
		}
		return false;
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
		if (type == ConfirmDialog.MESSAGE_DELETE) {
			MessageLoaderParam param = new MessageLoaderParam(MessageLoaderParam.DELETE, deleteId, "");
			messageLoader.execute(param, this);
		}
	}


	@Override
	public void onResult(MessageLoaderResult result) {
		switch (result.mode) {
			case MessageLoaderResult.DATABASE:
			case MessageLoaderResult.ONLINE:
				if (result.messages != null) {
					adapter.addItems(result.messages);
				}
				break;

			case MessageLoaderResult.DELETE:
				if (getContext() != null) {
					Toast.makeText(getContext(), R.string.info_dm_removed, LENGTH_SHORT).show();
				}
				adapter.removeItem(result.id);
				break;

			case MessageLoaderResult.ERROR:
				if (getContext() != null) {
					String message = ErrorHandler.getErrorMessage(getContext(), result.exception);
					Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
				}
				if (result.exception != null && result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
					adapter.removeItem(result.id);
				}
				break;
		}
		setRefresh(false);
	}

	/**
	 * @param local  true to load message from database
	 * @param cursor list cursor
	 */
	private void loadMessages(boolean local, String cursor) {
		int mode = local ? MessageLoaderParam.DATABASE : MessageLoaderParam.ONLINE;
		MessageLoaderParam param = new MessageLoaderParam(mode, 0L, cursor);
		messageLoader.execute(param, this);
	}
}