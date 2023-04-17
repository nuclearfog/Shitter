package org.nuclearfog.twidda.ui.fragments;

import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.ui.activities.MessageEditor.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_USER;
import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;

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
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.ui.activities.ImageViewer;
import org.nuclearfog.twidda.ui.activities.MessageEditor;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.adapter.MessageAdapter;
import org.nuclearfog.twidda.ui.adapter.MessageAdapter.OnMessageClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

/**
 * Fragment class to show a list of directmessages
 *
 * @author nuclearfog
 */
public class MessageFragment extends ListFragment implements OnMessageClickListener, OnConfirmListener, AsyncCallback<MessageLoaderResult> {

	/**
	 * "index" used to replace the whole list with new items
	 */
	private static final int CLEAR_LIST = -1;

	private MessageLoader messageLoader;
	private MessageAdapter adapter;
	private ConfirmDialog confirmDialog;

	private long selectedId;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		confirmDialog = new ConfirmDialog(requireContext());
		adapter = new MessageAdapter(requireContext(), this);
		messageLoader = new MessageLoader(requireContext());
		setAdapter(adapter);

		confirmDialog.setConfirmListener(this);

		loadMessages(false, null, CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		messageLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		loadMessages(true, null, CLEAR_LIST);
	}


	@Override
	protected void onReset() {
		adapter = new MessageAdapter(requireContext(), this);
		setAdapter(adapter);
		loadMessages(false, null, CLEAR_LIST);
		setRefresh(true);
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
		LinkUtils.openLink(requireActivity(), tag);
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
						selectedId = message.getId();
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
							Intent intent = new Intent(requireContext(), ImageViewer.class);
							intent.putExtra(ImageViewer.IMAGE_URI, Uri.parse(message.getMedia()[mediaIndex].getUrl()));
							intent.putExtra(ImageViewer.IMAGE_TYPE, ImageViewer.IMAGE_DEFAULT);
							startActivity(intent);
						}
					}
					break;
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(String cursor, int index) {
		if (messageLoader.isIdle()) {
			loadMessages(false, cursor, index);
			return true;
		}
		return false;
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.MESSAGE_DELETE) {
			MessageLoaderParam param = new MessageLoaderParam(MessageLoaderParam.DELETE, 0, selectedId, "");
			messageLoader.execute(param, this);
		}
	}


	@Override
	public void onResult(@NonNull MessageLoaderResult result) {
		switch (result.mode) {
			case MessageLoaderResult.DATABASE:
			case MessageLoaderResult.ONLINE:
				if (result.messages != null) {
					adapter.addItems(result.messages, result.index);
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
	private void loadMessages(boolean local, String cursor, int index) {
		int mode = local ? MessageLoaderParam.DATABASE : MessageLoaderParam.ONLINE;
		MessageLoaderParam param = new MessageLoaderParam(mode, index, 0L, cursor);
		messageLoader.execute(param, this);
	}
}