package org.nuclearfog.twidda.ui.fragments;

import android.content.Intent;
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
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.lists.Messages;
import org.nuclearfog.twidda.ui.activities.ImageViewer;
import org.nuclearfog.twidda.ui.activities.MessageEditor;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.adapter.recyclerview.MessageAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.MessageAdapter.OnMessageClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

import java.io.Serializable;

/**
 * Fragment class to show a list of directmessages
 *
 * @author nuclearfog
 */
public class MessageFragment extends ListFragment implements OnMessageClickListener, OnConfirmListener, AsyncCallback<MessageLoaderResult> {

	/**
	 * bundle key used to save adapter items
	 * value type is {@link Messages}
	 */
	private static final String KEY_SAVE = "message-save";

	private MessageLoader messageLoader;
	private MessageAdapter adapter;
	private ConfirmDialog confirmDialog;

	private long selectedId;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		confirmDialog = new ConfirmDialog(requireActivity(), this);
		adapter = new MessageAdapter(this);
		messageLoader = new MessageLoader(requireContext());
		setAdapter(adapter);

		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Messages) {
				adapter.replaceItems((Messages) data);
				return;
			}
		}
		loadMessages(false, null, MessageAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		messageLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		loadMessages(true, null, MessageAdapter.CLEAR_LIST);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		loadMessages(false, null, MessageAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onTagClick(String tag) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), SearchActivity.class);
			intent.putExtra(SearchActivity.KEY_QUERY, tag);
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
					sendDm.putExtra(MessageEditor.KEY_MESSAGE_PREFIX, message.getSender().getScreenname());
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
					profile.putExtra(ProfileActivity.KEY_USER, message.getSender());
					startActivity(profile);
					break;

				case MEDIA:
					if (extras.length == 1) {
						int mediaIndex = extras[0];
						if (mediaIndex >= 0 && mediaIndex < message.getMedia().length) {
							Intent intent = new Intent(requireContext(), ImageViewer.class);
							intent.putExtra(ImageViewer.KEY_IMAGE_DATA, message.getMedia()[mediaIndex].getUrl());
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
					Toast.makeText(getContext(), R.string.info_dm_removed, Toast.LENGTH_SHORT).show();
				}
				adapter.removeItem(result.id);
				break;

			case MessageLoaderResult.ERROR:
				if (getContext() != null) {
					ErrorUtils.showErrorMessage(getContext(), result.exception);
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