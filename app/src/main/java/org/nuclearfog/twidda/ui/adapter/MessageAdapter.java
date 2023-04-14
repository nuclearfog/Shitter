package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.backend.helper.Messages;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Message;
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

	private OnMessageClickListener itemClickListener;
	private GlobalSettings settings;
	private Picasso picasso;

	private Messages messages;
	private int loadingIndex;

	/**
	 * @param itemClickListener click listener
	 */
	public MessageAdapter(Context context, OnMessageClickListener itemClickListener) {
		messages = new Messages(null, null);
		loadingIndex = NO_LOADING;
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		this.itemClickListener = itemClickListener;
	}


	@Override
	public long getItemId(int index) {
		Message message = messages.get(index);
		if (message != null)
			return message.getId();
		return 0L;
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
			return new MessageHolder(parent, settings, picasso, this);
		} else {
			return new PlaceHolder(parent, settings, false, this);
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