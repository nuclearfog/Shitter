package org.nuclearfog.twidda.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.model.Message;
import org.nuclearfog.twidda.model.lists.Messages;
import org.nuclearfog.twidda.ui.adapter.holder.MessageHolder;
import org.nuclearfog.twidda.ui.adapter.holder.MessageHolder.OnItemClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.PlaceHolder;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show directmessages
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.MessageFragment
 */
public class MessageAdapter extends Adapter<ViewHolder> implements OnItemClickListener {

	/**
	 * index of {@link #loadingIndex} if no index is defined
	 */
	private static final int NO_LOADING = -1;

	/**
	 * view type of a message item
	 */
	private static final int TYPE_MESSAGE = 0;

	/**
	 * view type of a placeholder item
	 */
	private static final int TYPE_PLACEHOLDER = 1;

	/**
	 * "index" used to replace the whole list with new items
	 */
	public static final int CLEAR_LIST = -1;

	private OnMessageClickListener itemClickListener;

	private Messages messages = new Messages();
	private int loadingIndex = NO_LOADING;

	/**
	 * @param itemClickListener click listener
	 */
	public MessageAdapter(OnMessageClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}


	@Override
	public int getItemCount() {
		return messages.size();
	}


	@Override
	public int getItemViewType(int index) {
		if (messages.get(index) == null)
			return TYPE_PLACEHOLDER;
		return TYPE_MESSAGE;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_MESSAGE) {
			return new MessageHolder(parent, this);
		} else {
			return new PlaceHolder(parent, this, false);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
		if (vh instanceof MessageHolder) {
			Message message = messages.get(index);
			if (message != null) {
				((MessageHolder) vh).setContent(message);
			}
		} else if (vh instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) vh;
			placeHolder.setLoading(loadingIndex == index);
		}
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		Message message = messages.get(position);
		if (message != null) {
			switch (type) {
				case OnItemClickListener.MESSAGE_ANSWER:
					itemClickListener.onClick(message, OnMessageClickListener.ANSWER);
					break;

				case OnItemClickListener.MESSAGE_DELETE:
					itemClickListener.onClick(message, OnMessageClickListener.DELETE);
					break;

				case OnItemClickListener.MESSAGE_MEDIA:
					itemClickListener.onClick(message, OnMessageClickListener.MEDIA, extras);
					break;

				case OnItemClickListener.MESSAGE_PROFILE:
					itemClickListener.onClick(message, OnMessageClickListener.PROFILE);
					break;
			}
		}
	}


	@Override
	public void onTextClick(String text, boolean isLink) {
		if (isLink) {
			itemClickListener.onLinkClick(text);
		} else {
			itemClickListener.onTagClick(text);
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		boolean success = itemClickListener.onPlaceholderClick(messages.getNextCursor(), index);
		if (success) {
			loadingIndex = index;
		}
		return success;
	}

	/**
	 * get all adapter items
	 */
	public Messages getItems() {
		return new Messages(messages);
	}

	/**
	 * set messages
	 *
	 * @param newMessages new message list
	 */
	public void addItems(Messages newMessages, int index) {
		disableLoading();
		if (index < 0) {
			messages.replaceAll(newMessages);
			if (newMessages.getNextCursor() != null && !newMessages.getNextCursor().isEmpty()) {
				// add placeholder
				messages.add(null);
			}
			notifyDataSetChanged();
		} else {
			messages.addAll(index, newMessages);
			if (newMessages.getNextCursor() != null && !newMessages.getNextCursor().isEmpty() && messages.peekLast() != null) {
				messages.add(null);
				notifyItemRangeInserted(index, newMessages.size() + 1);
			} else if (newMessages.getNextCursor() == null && !newMessages.getNextCursor().isEmpty() && messages.peekLast() == null) {
				messages.pollLast();
				notifyItemRangeInserted(index, newMessages.size() - 1);
			} else {
				notifyItemRangeInserted(index, newMessages.size());
			}
		}
	}

	/**
	 * replace all adapter items
	 *
	 * @param newMessages new adapter items
	 */
	public void replaceItems(Messages newMessages) {
		messages.clear();
		messages.replaceAll(newMessages);
		if (newMessages.getNextCursor() != null && !newMessages.getNextCursor().isEmpty() && messages.peekLast() != null) {
			// add placeholder
			messages.add(null);
		}
		notifyDataSetChanged();
	}

	/**
	 * Remove a single item from list if found
	 *
	 * @param id message ID
	 */
	public void removeItem(long id) {
		int pos = messages.removeItem(id);
		if (pos >= 0) {
			notifyItemRemoved(pos);
		}
	}

	/**
	 * clear adapter data
	 */
	public void clear() {
		messages.clear();
		notifyDataSetChanged();
	}

	/**
	 * disable placeholder view loading animation
	 */
	private void disableLoading() {
		if (loadingIndex != NO_LOADING) {
			int oldIndex = loadingIndex;
			loadingIndex = NO_LOADING;
			notifyItemChanged(oldIndex);
		}
	}

	/**
	 * listener for directmessage items
	 */
	public interface OnMessageClickListener extends OnTagClickListener {

		/**
		 * indicates that the "answer" button was clicked
		 */
		int ANSWER = 1;

		/**
		 * indicates that the "delete" button was clicked
		 */
		int DELETE = 2;

		/**
		 * indicates that the profile image was clicked
		 */
		int PROFILE = 3;

		/**
		 * indicates that the media button was clicked
		 */
		int MEDIA = 4;

		/**
		 * called when a button was clicked
		 *
		 * @param message Message information
		 * @param action  what button was clicked {@link #ANSWER,#DELETE,#PROFILE,#MEDIA}
		 * @param extras  additional parameter
		 */
		void onClick(Message message, int action, int... extras);

		/**
		 * called when the placeholder was clicked
		 *
		 * @param cursor message cursor
		 * @param index  index of the placeholder
		 * @return true if task was started
		 */
		boolean onPlaceholderClick(String cursor, int index);
	}
}